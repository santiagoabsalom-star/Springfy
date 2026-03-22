package com.surrogate.springfy.utils;

import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.bussines.Audio;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
import com.surrogate.springfy.services.auth.AuthService;
import com.surrogate.springfy.services.bussines.DownloadService;
import com.surrogate.springfy.services.bussines.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener {

    private final AuthService authService;
//    private final DownloadService downloadService;
//    private final SearchService searchService;
//    private final AudioRepository audioRepository;

    @EventListener(ApplicationReadyEvent.class)
    void onAppReady() {
//        HashMap<String, Integer> duration = new HashMap<>();

        authService.createSystemAdminIfNotExists();
//        File carpetaWavs = new File(rutaWav);
//        File carpetaMp3 = new File(rutaMp3);
//        Pattern pattern = Pattern.compile("\\[([a-zA-Z0-9_-]{11})]");
//        audioRepository.deleteAll();
//
//        for(File file : Objects.requireNonNull(carpetaWavs.listFiles())) {
//        Matcher matcher = pattern.matcher(file.getName());
//
//        if(matcher.find()) {
//
//            String videoId= matcher.group(1);
//            log.info("Video id: {}", videoId);
//            String rutaAudio = file.getAbsolutePath();
//            String nombre = file.getName();
//            Audio audio = new Audio();
//            audio.setPath(rutaAudio);
//            audio.setNombreaudio(nombre);
//            Integer duracion= (int)getDurationSeconds(file);
//            duration.put(videoId, duracion);
//            log.info("Duracion de esta maldita cancion es de {}", duracion);
//            audio.setDuration((int )getDurationSeconds(file));
//
//            audio.setAudioId(videoId);
//            audio.setTipo("wav");
//            log.info("Duracion en audio es {}", audio.getDuration());
//            audioRepository.save(audio);
//        }
//        }
//        for(File file : Objects.requireNonNull(carpetaMp3.listFiles())) {
//            Matcher matcher = pattern.matcher(file.getName());
//            if(matcher.find()) {
//            String videoId= matcher.group(1);
//                String rutaAudio = file.getAbsolutePath();
//                String nombre = file.getName();
//                Audio audio = new Audio();
//
//                audio.setPath(rutaAudio);
//                audio.setDuration(duration.get(videoId));
//                audio.setNombreaudio(nombre);
//                audio.setAudioId(videoId);
//                audio.setTipo("mp3");
//                audioRepository.save(audio);
//            }
//        }
    }

}

