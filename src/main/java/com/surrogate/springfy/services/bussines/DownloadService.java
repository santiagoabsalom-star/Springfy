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
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {
    private final AudioRepository audioRepository;
    String yturl = "https://www.youtube.com/watch?v=";
    String ruta = "/home/santi/springfyCloud";
    ProcessBuilder processBuilder = new ProcessBuilder();

    public Response downloadOnCloud(String videoId) {
        if (audioRepository.existsAudioByAudioId(videoId)) {
            return new Response("error", 409, "El audio ya existe en la nube, no lo descargues devuelta");
        }

        String url = yturl + videoId;
        processBuilder.command(
                "yt-dlp",
                "-f", "bestaudio",
                "--external-downloader", "aria2c",
                "--external-downloader-args", "-x 32 -k 512M",
                "-P", ruta,
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

                                guardarAudio(videoId);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            log.info("YTDLP MODEFOCA FALLO");
                        }
                    })
                    .join();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return new Response("success", 200, "Guardado en cloud con exito");


    }


    public Resource downloadOnApp(String videoId) {
        String path = audioRepository.findByAudioId(videoId).getPath();
        if (path == null) {

            log.error("No se puede obtener el archivo de audio");
            return null;
        } else {
            Path audio = Paths.get(path);
            return new FileSystemResource(audio);
        }

    }

    private void guardarAudio(String videoId) throws IOException {
        File file = buscarAudio(new File(ruta), videoId);
        if (file == null) {
            log.error("Error guardando audio");
        } else {
            String rutaAudio = file.getAbsolutePath();
            String nombre = file.getName();
            Audio audio = new Audio();

            audio.setPath(rutaAudio);
            audio.setNombreaudio(nombre);
            audio.setAudioId(videoId);

            audioRepository.save(audio);
        }


    }

    private File buscarAudio(final File carpeta, String videoId) {


        // se puede optimizar con un hashmap, tipo clave valor, clave=videoId, valor=File ... mas consumo de ram pero menos tiempo de overhead, pq el hashmap se construye al principio de la aplicacion
        for (final File audio : Objects.requireNonNull(carpeta.listFiles())) {
            String name = audio.getName();
            if ((name.endsWith(".mp3") || name.endsWith(".webm") || name.endsWith(".m4a")) && name.contains(videoId)) {
                return audio;
            }
        }

        log.info("No se puede obtener el archivo de audio");
        return null;
    }


}
