package com.surrogate.springfy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.bussines.streaming.ClientState;
import com.surrogate.springfy.models.bussines.streaming.Comando;
import com.surrogate.springfy.models.bussines.streaming.Control;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.concurrent.locks.LockSupport;

import static com.surrogate.springfy.services.bussines.DownloadService.rutaWav;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamWebSocketHandler implements WebSocketHandler {
    private final ConcurrentMap<String, ClientState> pair = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();
    private static final long BYTES_PER_SECOND = 192000;
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws IOException {
        String usuario = (String) session.getAttributes().get("Usuario");
        String seguidor = (String) session.getAttributes().get("Seguidor");
        String anfitrion= (String) session.getAttributes().get("Anfitrion");
        String cancion_id= (String) session.getAttributes().get("Cancion_id");

            if(pair.get(anfitrion)==null && usuario.equals(anfitrion)) {
                ClientState state = new ClientState(session);
                state.change= false;
                state.setStopped(false);
                state.seguidor= seguidor;
                state.anfitrion= anfitrion;
                state.usuario= usuario;
                state.repeating= false;
                state.control= new Control();
                pair.put(usuario, state);

                state.executorService.submit(() -> {
                    try {

                        Stream(state,cancion_id );
                    } catch (LineUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else if(pair.get(anfitrion).seguidor.equals(seguidor)) {
                pair.get(anfitrion).setS(session);
            }
            else{
                log.info("No se ha encontrado ninguno de las mierdas que se necesitan");
                session.close();
            }



    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws IOException, InterruptedException {

        String anfitrion = (String) session.getAttributes().get("Anfitrion");
        String usuario = (String) session.getAttributes().get("Usuario");
        if (usuario.equals(anfitrion)) {

            ClientState state = pair.get(usuario);
            if (state != null) {

                if (message instanceof TextMessage) {
                    String json = (String) message.getPayload();
                    Comando comando = mapper.readValue(json, Comando.class);
                    String command = comando.getComando();


                    if (command.equals("stop")) {

                        if (!state.control.pausado) {
                            state.control.stop();
                        } else {
                            log.info("Ya esta pausado mongoloid");
                        }

                    } else if (command.equals("start")) {

                        if (state.control.pausado) {
                            state.control.reanudar();
                        } else {
                            log.info("Ya esta reanudado mongoloid");
                        }

                    } else if (command.equals("change")) {
                        if (state.control.pausado) {

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
                            } catch (LineUnavailableException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else if (comando.getComando().equals("move")) {

//......position esta entre los extremos de la musica, cuando...mover-> position en clientState cambia, siempre entre los valores de la cancion
                        state.byteOffset = comando.getSegundos() * BYTES_PER_SECOND;

                    } else if (comando.getComando().equals("repeat")) {


                            state.repeating = !state.repeating;

                        log.info("Modo repetir = {}", state.repeating);

                    } else {
                        log.info("Comando desconocido {}", command);
                    }
                }
            }
        }
        else{
            session.close();
        }


    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {

    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        try {
            String usuario = (String) session.getAttributes().get("Usuario");

            String anfitrion= (String) session.getAttributes().get("Anfitrion");

            log.info("Se ha cerrado el usuario {}", usuario);
            if(anfitrion.equals(usuario)) {
                if(pair.get(anfitrion).s!=null){
                pair.get(usuario).s.close();
                pair.get(usuario).s=null;}
                pair.remove(anfitrion);
            }
            else {
                pair.get(anfitrion).s=null;
                pair.remove(usuario);


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
    private void Stream(ClientState state, String songId) throws LineUnavailableException {


        File song = buscarMusica(songId);

        try (RandomAccessFile raf = new RandomAccessFile(song, "r")) {
            long bytesSentTotal=0;
            log.info("Streaming the fukin song: {},{}",song.getName(), song.getPath());

                state.setChange(false);
            long startTime=System.nanoTime();

                while (state.a.isOpen() && !state.isChange()) {
                    if(state.change){
                        break;
                    }


                    state.control.esperarSiEstaPausado();

                        raf.seek(state.byteOffset);
                        byte[] buffer = new byte[4096];

                        int read = raf.read(buffer);
                        if (read <= 0) {

                            if (state.repeating) {
                                log.info("Repitiendo musica");
                                state.byteOffset = 0;
                                raf.seek(state.byteOffset);
                                read = raf.read(buffer);
                                bytesSentTotal = 0;
                                startTime = System.nanoTime();
                                // si esta repitiendo, la musica termina y se vuelve a reproducir--> caposki

                            } else {
                                break;
                            }

                        }

                        bytesSentTotal+=read;
                        state.byteOffset += read;

                        BinaryMessage message = new BinaryMessage(Arrays.copyOf(buffer, read));
                        try {

                            if (state.s == null) {
                                state.a.sendMessage(message);
                            } else {

                                state.a.sendMessage(message);
                                state.s.sendMessage(message);
                            }
                        } catch (IOException e) {
                            Thread.sleep(30);
                        }
                        long expectedTimeNs = (bytesSentTotal * 1_000_000_000) / BYTES_PER_SECOND;


                        long realTimeNs = System.nanoTime() - startTime;
                        long delayNs = expectedTimeNs - realTimeNs;
                        if (delayNs > 0) {
                            LockSupport.parkNanos(delayNs);
                        }
                }try {
                Thread.currentThread().interrupt();
            }catch (RuntimeException e) {
                    log.error(e.getMessage());
            }
        } catch(FileNotFoundException e){
                    log.error("Error al leer el archivo de musica INFO={}.", e.getMessage());
                } catch(InterruptedException | IOException e){
                    throw new RuntimeException(e);
                } finally{


                    log.info("Se ha cerrado el archivo de musica.");
                }



        // Aca el audio y lo mandamos como bytes mientras la musica no haya terminado con PartialMessages, algo como: si el usuario pausa o se para la cancion(En este caso el cliente pide otra cancion y se reproduce, igual que cunado pide otra cancion), isLastMessage=true
        //luego para los distintos movimientos, por ejemplo adelantar la musica o atrasarla, aca el mensaje va a tener el byte, la opcion y la cantidad de segundos que se adelanto o se atraso
        // es decir partecancion= cancion + o - segundos que se adelanto o se atraso, - para atrasar y + mas para adelantar:D
        //
        // }


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

