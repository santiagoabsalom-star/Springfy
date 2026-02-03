package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.bussines.Audio;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {
    private final AudioRepository audioRepository;
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        String yturl="https://www.youtube.com/watch?v=";
        String ruta="/home/santi/springfyCloud";
        
        ProcessBuilder processBuilder = new ProcessBuilder();
public Response downloadOnCloud(String videoId) {
    if(audioRepository.existsAudioByAudioId(videoId)) {
        return new Response("error", 409, "El audio ya existe en la nube, no lo descargues devuelta");
    }

            String url = yturl + videoId;
            processBuilder.redirectErrorStream(true);

            processBuilder.command(
                    "yt-dlp",
                    "-x",
                    "--audio-format", " mp3", "--audio-quality", "0",
                    "-P", ruta,
                    url
            );
    Process process = null;
    try {
        process = processBuilder.start();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    executor.submit(() -> {
        try {
            guardarAudio(videoId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });


    return new Response("success", 200, "Guardado en cloud con exito");


}


public Resource downloadOnApp(String videoId) {
    String path=audioRepository.findByAudioId(videoId).getPath();
    if(path==null){

        log.error("No se puede obtener el archivo de audio");
    return null;
    }else {
        Path audio = Paths.get(path);
        return new FileSystemResource(audio);
    }

    }

private void guardarAudio(String videoId) throws IOException {
    File file =buscarAudio(new File(ruta), videoId);
    if( file==null){
        log.error("Error guardando audio");
    }



    else {
            String rutaAudio = file.getAbsolutePath();
            String nombre = file.getName();
            Audio audio = new Audio();
            audio.setPath(rutaAudio);
            audio.setNombreaudio(nombre);
            audio.setAudioId(videoId);
            audioRepository.save(audio);
        }


}
private File buscarAudio(final File carpeta,String videoId) throws IOException {
    for (final File audio : Objects.requireNonNull(carpeta.listFiles())) {
        if (audio.isDirectory()) {
            log.info("{} es un directorio", audio.getName());
        } else {
           if (audio.getName().endsWith(".mp3") && audio.getName().contains(videoId)) {
               return audio;

           }
        }
    }
    return null;
}

}
