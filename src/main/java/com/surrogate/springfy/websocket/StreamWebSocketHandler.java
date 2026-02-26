package com.surrogate.springfy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.bussines.streaming.ClientState;
import com.surrogate.springfy.models.bussines.streaming.Comando;
import com.surrogate.springfy.models.bussines.streaming.Control;
import com.surrogate.springfy.models.bussines.streaming.WavInfo;
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
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
    private static final int BYTES_PER_SECOND = 192000;
    private final DuoRepository duoRepository;
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws IOException {
        String usuario = (String) session.getAttributes().get("Usuario");
        log.info("Usuario en sesion es: {} ",  usuario);
        if(Objects.nonNull(usuario)) {

            sessions.put(usuario, session);

            if(Objects.isNull(pair.get(usuario))) {
                String duoUsername = duoRepository.getOtherUsername(usuario);
                if (duoUsername != null) {


                    WebSocketSession duo = sessions.get(duoUsername);

                    if(Objects.nonNull(duo) && duo.isOpen()) {

                        Comando comando = new Comando();
                        comando.setComando("duo-connected");
                        String json = mapper.writeValueAsString(comando);
                            duo.sendMessage(new TextMessage(json));
                        ClientState state = pair.get(duoUsername);
                        if (Objects.nonNull(state)) {
                            if (state.started) {
                                log.info("Avisando a {} que ya esta iniciada la musica" ,usuario);
                                Comando duration = new Comando();
                                duration.setComando("duration");
                                duration.setDuration(state.currentSongDuration);
                                String jsonDuration = mapper.writeValueAsString(duration);

                                session.sendMessage(new TextMessage(jsonDuration));

                                Comando command = new Comando();
                                command.setComando("start");
                                log.info("isPlaying? {}", !state.control.pausado);
                                command.setPlaying(!state.control.pausado);
                                command.setAnfitrion(state.anfitrion);
                                command.setSeguidor(state.seguidor);
                                command.setMusicId(state.currentSongId);
                                command.setCurrentPosition(state.currentPosition);
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



                        ClientState state = new ClientState(session);



                        state.change= false;
                        state.setStopped(false);
                        state.seguidor= comando.getSeguidor();
                        state.anfitrion= comando.getAnfitrion();
                        state.usuario= usuario;
                        state.control= new Control();
                        state.started=true;
                        state.currentSongId=comando.getMusicId();
                        state.repeating= false;

                        pair.put(usuario, state);
                        WebSocketSession seguidorSession = sessions.get(state.seguidor);
                        log.info("Es null? {}", Objects.isNull(seguidorSession) ? "Si" : "No");

                        if(Objects.nonNull(seguidorSession)) {
                            seguidorSession.sendMessage(message);
                        }

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
                                if(state.s!=null) {
                                    state.s.sendMessage(message);
                                }else{
                                    WebSocketSession seguidorSession = sessions.get(state.seguidor);
                                    if(Objects.nonNull(seguidorSession)) {
                                        seguidorSession.sendMessage(message);
                                    }
                                }
                                state.control.stop();
                            } else {
                                log.info("Ya esta pausado mongoloid");
                            }

                        } else if (command.equals("resume")) {

                            if (state.control.pausado) {
                                state.startTime = System.nanoTime() -
                                        ((state.bytesSentTotal * 1_000_000_000L) / BYTES_PER_SECOND);
                                if(state.s!=null) {
                                    state.s.sendMessage(message);
                                }else{
                                    WebSocketSession seguidorSession = sessions.get(state.seguidor);
                                    if(Objects.nonNull(seguidorSession)) {
                                        seguidorSession.sendMessage(message);
                                    }
                                }
                                state.control.reanudar();
                            } else {
                                log.info("Ya esta reanudado mongoloid");
                            }

                        } else if (command.equals("change")) {

                            if(state.s!=null){
                                state.currentPosition=0;
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
                            int position= comando.getSegundosToMove();
                            if(position>=0 && position<=state.currentSongDuration){

                                boolean despausarIfNotPausado=state.control.pausado;
                                state.control.stop();
                                state.byteOffset = position * BYTES_PER_SECOND;

                                if(state.s!=null){
                                    state.s.sendMessage(message);
                                }
                                else{
                                    WebSocketSession seguidorSession = sessions.get(comando.getSeguidor());
                                    if (Objects.nonNull(seguidorSession)) {
                                        seguidorSession.sendMessage(message);
                                    }

                                }
                                if(!despausarIfNotPausado){
                                    state.control.reanudar();
                                }

                            }
                            else{
                                log.info("La cantidad de segundos no es valida");
                            }

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

            Comando comando= new Comando();
            comando.setComando("duo-disconnected");
            String co= mapper.writeValueAsString(comando);

            TextMessage message2 = new TextMessage(co);
            String duo= duoRepository.getOtherUsername(usuario);
            WebSocketSession duoSession = sessions.get(duo);
            if (Objects.nonNull(duoSession)) {

                duoSession.sendMessage(message2);
                sessions.remove(usuario);
            }
            ClientState cs= pair.get(usuario);
            if( Objects.nonNull(cs) && cs.anfitrion.equals(usuario)) {
                Comando comand = new Comando();

                comand.setComando("disconnect");
                String kd = mapper.writeValueAsString(comand);
                TextMessage message = new TextMessage(kd);

                if(cs.s!=null && cs.s.isOpen()) {
                    log.info("Avisando a follower conectado ");

                    try {


                        cs.s.sendMessage(message);
                    }catch (Exception e){
                        log.error("Error sending a follower message {}", e.getMessage());
                    }
                    cs.s = null;
                    try {
                        log.info("Intentando remover el clientState con usuario {}", usuario);
                        pair.remove(usuario);
                        log.info("Removido con exito");
                        log.info("Intentando remover sesion de usuario {}", usuario);
                        sessions.remove(usuario);
                        log.info("Removido con exito");
                    }catch (Exception e){
                        log.error("Error remover usuario {} con stack stacktrace {}", usuario, e.getMessage());
                    }
                }else {

                    WebSocketSession seguidorSession = sessions.get(cs.seguidor);
                    if (Objects.nonNull(seguidorSession) && seguidorSession.isOpen()) {

                        seguidorSession.sendMessage(message2);
                        seguidorSession.sendMessage(message);
                        log.info("Avisando a follower desconectado ");
                        try {
                            log.info("Intentando remover el clientState con usuario {}", usuario);
                            pair.remove(usuario);
                            log.info("Removido con exito");
                            log.info("Intentando remover sesion de usuario {}", usuario);
                            sessions.remove(usuario);
                            log.info("Removido con exito");
                        }catch (Exception e){
                            log.error("Error remover usuario {} con stack stacktrace {}", usuario, e.getMessage());
                        }
                    }
                        pair.remove(usuario);
                    sessions.remove(usuario);

                }
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
        state.currentSongId = songId;
        File song = buscarMusica(songId);
// se puede dividir el file en las partes, con algoritmo de que acepte peso y duracion de la song:D por ejemplo 90 segundos y 30 megas, aprox 9000 partes
        //refactor metiendo todo el archivo en memoria: antes= disco->kernel->jvm->enviar, ahora disco->kernel->memoria(guardandotodo el archivo en memoria)->jvm->enviar
        WavInfo wavInfo= parseWav(song);
        int duracion=(int) wavInfo.getDurationSeconds();

        if(duracion>0){
            state.currentSongDuration=duracion;
            log.info("La duracion de esta maldita cancion es de {}", duracion);
            Comando comando = new Comando();
            comando.setComando("duration");
            comando.setDuration(duracion);
            TextMessage message = new TextMessage(mapper.writeValueAsString(comando));
            state.a.sendMessage(message);
            if(state.s!=null) {
                state.s.sendMessage(message);
            }
            WebSocketSession seguidorSession = sessions.get(state.seguidor);
            if (Objects.nonNull(seguidorSession)) {
                seguidorSession.sendMessage(message);
            }

        }

try (RandomAccessFile raf = new RandomAccessFile(song, "r")) {




            state.bytesSentTotal=0;
            log.info("Streaming the fukin song: {},{}",song.getName(), song.getPath());

                state.setChange(false);
            state.startTime=System.nanoTime();
    final long MAPPED_THRESHOLD = 200L * 1024 * 1024;
    final int DEFAULT_CHUNK = 4096;

    long fileSize = raf.length();

    if (fileSize > MAPPED_THRESHOLD) {


        while ( state.a.isOpen() && !state.isChange()) {

            state.control.esperarSiEstaPausado();
            if (state.change){
                break;}
            if (state.isStopped()) {
                break;}
            state.currentPosition = state.byteOffset / BYTES_PER_SECOND;




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
                    Comando comando = new Comando();
                    comando.setComando("finished");
                    String json = mapper.writeValueAsString(comando);
                    state.a.sendMessage(new TextMessage(json));
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

            state.control.esperarSiEstaPausado();
            if (state.change){
                break;}
            if (state.isStopped()) {
                break;}
            if(state.byteOffset>0) {
                state.currentPosition = state.byteOffset / BYTES_PER_SECOND;
            }


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
                log.info("No se pudo enviar musica: {}", e.getMessage());
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

        } catch (Exception e) {
    log.error(e.getMessage());
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

    public static WavInfo parseWav(File file) throws IOException {
//en un archivo wav normalmente tengo 44 bytes de informacion. si no esta la informacion busco en otras partes del archivo wav
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(12);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            channel.read(buffer);
            buffer.flip();

            byte[] riff = new byte[4];
            buffer.get(riff);
            if (!new String(riff).equals("RIFF")) {
                throw new IllegalArgumentException("No es RIFF");
            }

            buffer.getInt();

            byte[] wave = new byte[4];
            buffer.get(wave);
            if (!new String(wave).equals("WAVE")) {
                throw new IllegalArgumentException("No es WAVE");
            }

            Integer sampleRate = null;
            Integer channels = null;
            Integer bitsPerSample = null;
            Long dataSize = null;

            while (channel.position() < channel.size()) {

                ByteBuffer header = ByteBuffer.allocate(8);
                header.order(ByteOrder.LITTLE_ENDIAN);
                channel.read(header);
                header.flip();

                byte[] chunkIdBytes = new byte[4];
                header.get(chunkIdBytes);
                String chunkId = new String(chunkIdBytes);

                int chunkSize = header.getInt();

                if (chunkId.equals("fmt ")) {

                    ByteBuffer fmtBuffer = ByteBuffer.allocate(chunkSize);
                    fmtBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    channel.read(fmtBuffer);
                    fmtBuffer.flip();

                    int audioFormat = fmtBuffer.getShort() & 0xFFFF;
                    channels = fmtBuffer.getShort() & 0xFFFF;
                    sampleRate = fmtBuffer.getInt();
                    fmtBuffer.getInt();
                    fmtBuffer.getShort();
                    bitsPerSample = fmtBuffer.getShort() & 0xFFFF;

                    if (audioFormat != 1) {
                        throw new UnsupportedOperationException("Solo PCM soportado");
                    }

                } else if (chunkId.equals("data")) {

                    dataSize = (long) chunkSize;
                    break;

                } else {
                    channel.position(channel.position() + chunkSize);
                }

                if ((chunkSize & 1) == 1) {
                    channel.position(channel.position() + 1);
                }
            }

            if (sampleRate == null || channels == null || bitsPerSample == null || dataSize == null) {
                throw new IllegalStateException("WAV incompleto o corrupto");
            }

            return new WavInfo(sampleRate, channels, bitsPerSample, dataSize);
        }
    }


}

