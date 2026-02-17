package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.bussines.Audio;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.surrogate.springfy.websocket.StreamWebSocketHandler.getFile;


@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AudioRepository audioRepository;
    String yturl = "https://www.youtube.com/watch?v=";
    public static String rutaMp3 = "/home/santi/springfyCloud/mp3/";
    public static String rutaWav = "/home/santi/springfyCloud/wav/";
    ProcessBuilder processBuilder = new ProcessBuilder();
    public Response downloadOnCloud(String videoId) {
        if (audioRepository.existsAudioByAudioId(videoId)) {
            return new Response("error", 409, "El audio ya existe en la nube, no lo descargues devuelta");
        }



        String url = yturl + videoId;

        executor.execute(() -> {processBuilder.command(
                "yt-dlp",
                "-x",
                "--audio-format", "wav",
                "--external-downloader", "aria2c",
                "--external-downloader-args", "aria2c:-x 24 -k 512K",
                "-P", rutaWav,
                url
        );

            try {
                processBuilder.start()
                        .onExit()
                        .thenAccept(process -> {
                            if (process.exitValue() == 0) {
                                try {

                                    guardarAudioWav(videoId);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
                                log.info("YTDLP MODEFOCA FALLO");
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


    processBuilder.command(
            "yt-dlp",
            "-x",
            "--audio-format", " mp3", "--audio-quality", "0",
            "--external-downloader", "aria2c",
            "--external-downloader-args", "aria2c:-x 24 -k 512K",
            "-P", rutaMp3,
            url
    );


    //Process process = null;
    try {
        //    process = processBuilder.start();
//  BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //String line;
        //while ((line = reader.readLine()) != null) {
        //  System.out.println(line);
        //}
        processBuilder.start()
                .onExit()
                .thenAccept(process -> {
                    if (process.exitValue() == 0) {
                        try {

                            guardarAudioMp3(videoId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        log.info("Guardar en mp3 fallo");
                    }
                })
                .join();


    } catch (IOException e) {
        throw new RuntimeException(e);
    }


        return new Response("success", 200, "Guardado en cloud con exito");

    }


    public Resource downloadOnApp(String videoId) {
        long startTime = System.currentTimeMillis();
        log.info("Intentando descargar el audio con id: {}", videoId);
        Audio audio= audioRepository.findByAudioMp3Id(videoId);

        String path =audio.getPath();
        if (path == null) {

            log.error("No se puede obtener el archivo de audio");
            return null;
        } else {
            Path pathfile = Paths.get(path);
            FileSystemResource file = new FileSystemResource(pathfile);
            long stopTime = System.currentTimeMillis();
            log.info("Tiempo en buscar el archivo de audio fue {} ms", (stopTime - startTime));
            return file;

        }

    }

    private void guardarAudioMp3(String videoId) throws IOException {
        File file = buscarAudioMp3(new File(rutaMp3), videoId);
        if (file == null) {
            log.error("Error guardando audio");
        } else {
            String rutaAudio = file.getAbsolutePath();
            String nombre = file.getName();
            Audio audio = new Audio();

            audio.setPath(rutaAudio);
            audio.setNombreaudio(nombre);
            audio.setAudioId(videoId);
            audio.setTipo("mp3");
            audioRepository.save(audio);
        }


    }


    private void guardarAudioWav( String videoId) throws IOException {
        File file= buscarAudioWav(new File(rutaWav), videoId);
        if (file == null) {
            log.error("Error guardando audio");
        }
        else {
            String rutaAudio = file.getAbsolutePath();
            String nombre = file.getName();
            Audio audio = new Audio();
            audio.setPath(rutaAudio);
            audio.setNombreaudio(nombre);
            audio.setAudioId(videoId);
            audio.setTipo("wav");
            audioRepository.save(audio);
        }
    }
    private File buscarAudioWav(final File carpeta, String videoId) throws IOException {
        return getFile(carpeta,videoId,log);
    }
    private File buscarAudioMp3(final File carpeta, String videoId) {


        // se puede optimizar con un hashmap, tipo clave valor, clave=videoId, valor=File ... mas consumo de ram pero menos tiempo de overhead, pq el hashmap se construye al principio de la aplicacion
        return getFile(carpeta, videoId, log);
    }


}
