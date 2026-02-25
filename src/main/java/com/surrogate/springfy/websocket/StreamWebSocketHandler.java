package com.surrogate.springfy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.bussines.streaming.ClientState;
import com.surrogate.springfy.models.bussines.streaming.Comando;
import com.surrogate.springfy.models.bussines.streaming.Control;
import com.surrogate.springfy.repositories.bussines.DuoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;

import static com.surrogate.springfy.services.bussines.DownloadService.rutaWav;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class StreamWebSocketHandler implements WebSocketHandler {
    private final ConcurrentMap<String, ClientState> pair = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Long> lastPong = new ConcurrentHashMap<>();
    private static final int BYTES_PER_SECOND = 192000;
    private final DuoRepository duoRepository;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws IOException {
        String usuario = (String) session.getAttributes().get("Usuario");
        log.info("Usuario en sesion es: {} ",  usuario);
        if(Objects.nonNull(usuario)) {

            lastPong.put(session.getId(), System.currentTimeMillis());
            sessions.put(usuario, session);

            if(Objects.isNull(pair.get(usuario))) {
                String duoUsername = duoRepository.getOtherUsername(usuario);
                if (duoUsername != null) {


                    WebSocketSession duo = sessions.get(duoUsername);

                    if(Objects.nonNull(duo)) {
                        Comando comando = new Comando();
                        comando.setComando("duo-connected");
                        String json = mapper.writeValueAsString(comando);
                        duo.sendMessage(new TextMessage(json));

                        ClientState state = pair.get(duoUsername);
                        if (Objects.nonNull(state)) {
                            if (state.started) {
                                log.info("Avisando a {} que ya esta iniciado el musica" ,usuario);

                                Comando command = new Comando();
                                command.setComando("start");
                                command.setAnfitrion(state.anfitrion);
                                command.setSeguidor(state.seguidor);
                                command.setMusicId(state.currentSongId);
                                String sw = mapper.writeValueAsString(command);
                                session.sendMessage(new TextMessage(sw));
                            }
                        }
                    }
                }
            }
        }

        else{
            session.close(new CloseStatus(404, "Usuario no encontrado"));
        }
    }
//refactor a como se inicia el stream, para poder ver in real time (osea disculpame) que usuario inicio y que usuario es el anfitrion
    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws IOException {
        String usuario = (String) session.getAttributes().get("Usuario");
                if (message instanceof TextMessage) {
                    String json = (String) message.getPayload();
                    Comando comando = mapper.readValue(json, Comando.class);
                    String command = comando.getComando();
                    if(command.equals("is-duo-connected")) {
                        log.info("Recibida la verificacion de conexion de duo");
                        String duoUsername = duoRepository.getOtherUsername(usuario);
                        if (duoUsername != null) {
                            if (sessions.containsKey(duoUsername)) {
                                Comando commando = new Comando();
                                commando.setComando("duo-connected");
                                String js = mapper.writeValueAsString(commando);
                                log.info("Enviando verificacion de conexion de duo");
                                log.info(comando.getComando());
                                session.sendMessage(new TextMessage(js));
                            }
                            return;
                        }
                        return;

                    }
                    if(command.equals("start") && Objects.isNull(pair.get(usuario))){
                        WebSocketSession seguidorSession = sessions.get(comando.getSeguidor());
                        log.info("Es null? {}", Objects.isNull(seguidorSession) ? "Si" : "No");
                        if(Objects.nonNull(seguidorSession)) {
                            seguidorSession.sendMessage(message);
                        }
                        ClientState state = new ClientState(session);




                        state.change= false;
                        state.setStopped(false);
                        state.seguidor= comando.getSeguidor();
                        state.anfitrion= comando.getAnfitrion();
                        state.usuario= usuario;

                        state.started=true;
                        state.currentSongId=comando.getMusicId();
                        state.repeating= false;
                        state.control= new Control();
                        pair.put(usuario, state);

                        state.executorService.submit(() -> {
                            try {

                                Stream(state, comando.getMusicId() );
                            } catch (LineUnavailableException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        });


                    }
                    else{
                        if(comando.getComando().equals("follower-connect")){
                        ClientState state = pair.get(comando.getAnfitrion());
                        if(state.seguidor.equals(usuario)){
                            state.a.sendMessage(message);
                            state.s=session;
                        }
                        } else if (comando.getComando().equals("follower-disconnect")) {
                            ClientState state = pair.get(comando.getAnfitrion());
                            if(state.seguidor.equals(usuario)){
                                state.a.sendMessage(message);
                                state.s=null;

                            }
                        }
                        ClientState state = pair.get(usuario);
                        //Como lo voy a guardar con el anfitrion (primero que se conecte), si seguidor manda mensaje no va a hacer nada pq es null:D
                        if(state != null && state.anfitrion.equals(usuario)){
                        if (command.equals("stop")) {

                            if (!state.control.pausado) {

                                state.control.stop();

                            } else {
                                log.info("Ya esta pausado mongoloid");
                            }

                        } else if (command.equals("resume")) {

                            if (state.control.pausado) {
                                state.startTime = System.nanoTime() -
                                        ((state.bytesSentTotal * 1_000_000_000L) / BYTES_PER_SECOND);

                                state.control.reanudar();
                            } else {
                                log.info("Ya esta reanudado mongoloid");
                            }

                        } else if (command.equals("change")) {
                            if(state.s!=null){

                                state.s.sendMessage(message);
                            }
                            else {
                                WebSocketSession seguidorSession = sessions.get(comando.getSeguidor());
                                if (Objects.nonNull(seguidorSession)) {
                                    seguidorSession.sendMessage(message);
                                }
                            }
                            if (state.control.pausado) {
                                state.startTime = System.nanoTime();


                                state.control.reanudar();
                            }
                            state.setChange(true);


                            state.byteOffset = 0;


                            // break en el while y comienza una nueva musica con el mismo metodo;D

                            //mucho mas facil y rapido  que hacer un puto evento y conectar los eventos,
                            // pero creo que es mas propenso a fallos, si uso bien la logica puede ser potencialmente mas rapido que eventos:D


                            state.executorService.submit(() -> {
                                try {

                                    Stream(state, comando.getMusicId());
                                } catch (LineUnavailableException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else if (comando.getComando().equals("move")) {

//......position esta entre los extremos de la musica, cuando...mover-> position en clientState cambia, siempre entre los valores de la cancion
                            state.byteOffset = comando.getSegundos() * BYTES_PER_SECOND;

                        } else if (comando.getComando().equals("repeat")) {


                            state.repeating = !state.repeating;

                            log.info("Modo repetir = {}", state.repeating);

                        } else if (comando.getComando().equals("disconnect")) {
                        if(state.s==null){

                            WebSocketSession seguidorSession = sessions.get(comando.getSeguidor());
                            if(Objects.nonNull(seguidorSession)){
                                seguidorSession.sendMessage(message);
                            }
                        }else{
                            if(state.s.isOpen()) {
                                state.s.sendMessage(message);
                            }
                        }

                        state.s=null;
                        state.a=null;
                        state.anfitrion=null;
                        state.seguidor=null;
                        state.setStopped(true);
                        pair.remove(usuario);
                    }
                    else {
                            log.info("Comando desconocido {}", command);
                        }
                        }
                    }
                }
//si se desconecta ahora ya no se desconecta de la sesion si no del clientstate



    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {

    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        try {

            String usuario = (String) session.getAttributes().get("Usuario");
            log.info("Cerrando sesion de usuario {}", usuario);
            ClientState cs= pair.get(usuario);
            Comando comando= new Comando();
            comando.setComando("duo-disconnected");
            String co= mapper.writeValueAsString(comando);

            TextMessage message2 = new TextMessage(co);
            String duo= duoRepository.getOtherUsername(usuario);
            WebSocketSession duoSession = sessions.get(duo);
            if (Objects.nonNull(duoSession)) {
                duoSession.sendMessage(message2);
            }
            if( Objects.nonNull(cs) && cs.anfitrion.equals(usuario)) {
                Comando comand = new Comando();

                comand.setComando("disconnect");
                String kd = mapper.writeValueAsString(comand);
                TextMessage message = new TextMessage(kd);

                if(cs.s!=null) {
                    log.info("Avisando a follower conectado ");


                    cs.s.sendMessage(message);

                    cs.s = null;
                    pair.remove(usuario);
                    sessions.remove(usuario);

                }

                WebSocketSession seguidorSession = sessions.get(cs.seguidor);
                if (Objects.nonNull(seguidorSession)) {
                    seguidorSession.sendMessage(message2);
                seguidorSession.sendMessage(message);
                    log.info("Avisando a follower desconectado ");

                }
                pair.remove(usuario);
                sessions.remove(usuario);
            }

            else {
                    String name= duoRepository.getOtherUsername(usuario);
                ClientState state=pair.get(name);
                if(state!=null && state.a.isOpen()){
                    Comando comand = new Comando();

                    log.info("Avisando a anfitrion");
                    comand.setComando("follower-disconnect");
                    String kd = mapper.writeValueAsString(comand);
                    TextMessage message = new TextMessage(kd);
                    state.a.sendMessage(message);

                }


                    sessions.remove(usuario);

                }

        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
//funcion que tengo que cambiar y optimizar-> abrir el archivo y enviarlo en bytes
    private void Stream(ClientState state, String songId) throws LineUnavailableException, IOException {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        File song = buscarMusica(songId);
// se puede dividir el file en las partes, con algoritmo de que acepte peso y duracion de la song:D por ejemplo 90 segundos y 30 megas, aprox 9000 partes
        //refactor metiendo todo el archivo en memoria: antes= disco->kernel->jvm->enviar, ahora disco->kernel->memoria(guardandotodo el archivo en memoria)->jvm->enviar

try (RandomAccessFile raf = new RandomAccessFile(song, "r")) {




            state.bytesSentTotal=0;
            log.info("Streaming the fukin song: {},{}",song.getName(), song.getPath());

                state.setChange(false);
            state.startTime=System.nanoTime();
    final long MAPPED_THRESHOLD = 200L * 1024 * 1024; // 200MB
    final int DEFAULT_CHUNK = 4096;

    long fileSize = raf.length();

    if (fileSize > MAPPED_THRESHOLD) {



        while (state.a.isOpen() && !state.isChange()) {

            if (state.change) break;
            if (state.isStopped()) break;

            state.control.esperarSiEstaPausado();

            raf.seek(state.byteOffset);

            byte[] buffer = new byte[DEFAULT_CHUNK];
            int read = raf.read(buffer);

            if (read <= 0) {

                if (state.repeating) {
                    log.info("Repitiendo musica");
                    state.byteOffset = 0;
                    state.bytesSentTotal = 0;
                    state.startTime = System.nanoTime();
                    continue;
                } else {
                    break;
                }
            }

            state.bytesSentTotal += read;
            state.byteOffset += read;

            BinaryMessage message =
                    new BinaryMessage(ByteBuffer.wrap(buffer, 0, read));

            try {
                if (state.s == null || !state.s.isOpen()) {
                    if(state.a.isOpen()){
                    state.a.sendMessage(message);
                    }
                } else {
                    if(state.a.isOpen()) {
                        state.a.sendMessage(message);
                        state.s.sendMessage(message);
                    }
                    }
            } catch (IOException e) {

                state.a.close();
                state.a = null;

                if (state.s != null) {
                    state.s.close();
                    state.s = null;
                    Thread.currentThread().interrupt();
                    pair.remove(state.anfitrion);
                    break;

                }
            }
            long expectedTimeNs =
                    (state.bytesSentTotal * 1_000_000_000L) / BYTES_PER_SECOND;

            long realTimeNs = System.nanoTime() - state.startTime;
            long delayNs = expectedTimeNs - realTimeNs;

            if (delayNs > 0) {
                LockSupport.parkNanos(delayNs);
            }
        }

    } else {


        MappedByteBuffer mapperBuffer =
                raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

        while (state.a.isOpen() && !state.isChange()) {

            if (state.change) break;
            if (state.isStopped()) break;
            state.control.esperarSiEstaPausado();

            if (state.byteOffset >= fileSize) {


                if (state.repeating) {
                    log.info("Repitiendo musica");
                    state.byteOffset = 0;
                    state.bytesSentTotal = 0;
                    state.startTime = System.nanoTime();
                } else {
                    Comando comando = new Comando();
                    comando.setComando("finished");
                    String json = mapper.writeValueAsString(comando);
                    state.a.sendMessage(new TextMessage(json));
                    break;
                }
            }

            int remaining = (int)(fileSize - state.byteOffset);
            int chunkSize = Math.min(DEFAULT_CHUNK, remaining);

            ByteBuffer slice = mapperBuffer
                    .duplicate()
                    .position( state.byteOffset)
                    .limit( state.byteOffset + chunkSize)
                    .slice()
                    .asReadOnlyBuffer();

            state.bytesSentTotal += chunkSize;
            state.byteOffset += chunkSize;

            BinaryMessage message = new BinaryMessage(slice);

            try {
                if (state.s == null || !state.s.isOpen()) {
                    state.a.sendMessage(message);
                } else {
                    state.a.sendMessage(message);
                    state.s.sendMessage(message);
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
                break;
            }

            long expectedTimeNs =
                    (state.bytesSentTotal * 1_000_000_000L) / BYTES_PER_SECOND;

            long realTimeNs = System.nanoTime() - state.startTime;
            long delayNs = expectedTimeNs - realTimeNs;

            if (delayNs > 0) {
                LockSupport.parkNanos(delayNs);
            }
        }
    }try {

                Thread.currentThread().interrupt();
            }catch (RuntimeException e) {
                    log.error(e.getMessage());

            }
        } catch(FileNotFoundException e){
                    log.error("Error al leer el archivo de musica INFO={}.", e.getMessage());
                } catch(IOException e){
                    throw new RuntimeException(e);
                } finally{


                    log.info("Se ha cerrado el archivo de musica.");

                }






    }


    private File buscarMusica( String videoId) {


//        // se puede optimizar con un hashmap, tipo clave valor, clave=videoId, valor=File ... mas consumo de ram pero menos tiempo de overhead, pq el hashmap se construye al principio de la aplicacion
        return getFile(new File(rutaWav), videoId, log);
    }

    @Nullable
    public static File getFile(File carpeta, String videoId, Logger log) {
        for (final File audio : Objects.requireNonNull(carpeta.listFiles())) {
            String name = audio.getName();
            if ((name.endsWith(".mp3") || name.endsWith(".webm") || name.endsWith(".m4a")|| name.endsWith(".wav")) && name.contains(videoId)) {
                return audio;
            }
        }

        log.info("No se puede obtener el archivo de audio");
        return null;
    }


}

