package com.surrogate.springfy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import static com.surrogate.springfy.services.bussines.DownloadService.rutaMp3;
import static com.surrogate.springfy.services.bussines.DownloadService.rutaWav;
import static com.surrogate.springfy.websocket.StreamWebSocketHandler.getFile;

class SpringfyApplicationTests {
   private static final Logger log = LoggerFactory.getLogger(SpringfyApplicationTests.class);
    public static void main(String[] args)  {




    }
public static void virtualThreadtest() throws InterruptedException {
    for (int i = 0; i <= 10000; i++) {

        int finalI1 = i;
        Thread.ofVirtual().start(() -> {
            for (int j = 0; j < 1000; j++) {
                log.info("Hola desde virtual thread #{} esta es la iteracion #{}", finalI1, j);
            }
        }).join();


    }
}
public static void speedtestsVideoDownloading() {



        String yturl = "https://www.youtube.com/watch?v=";String videoId="Fk2yV_1VUmU";
    String url = yturl + videoId;
    File mp3= getFile(new File(rutaMp3), videoId, log);
    File wav = getFile(new File(rutaWav), videoId, log);
    if (mp3 != null && wav != null) {
        if(mp3.exists()) {
            if (wav.exists()) {
                log.info("Deleting both files: {} and {}", wav.getAbsolutePath(), mp3.getAbsolutePath());
                if(!mp3.delete()) throw new RuntimeException("Failed to delete mp3");
                if(!wav.delete()) throw new RuntimeException("Failed to delete wav");
            }
        }
    }
    Runtime rn = Runtime.getRuntime();
    long start=System.currentTimeMillis();
    RecursiveTask<Integer> task = new RecursiveTask<>() {
        @Override
        protected Integer compute() {

            String[] command = {"yt-dlp",
                    "-x",
                    "--audio-format", " mp3", "--audio-quality", "0",
                    "--no-playlist", "--no-warnings",
                    "-P", rutaMp3,
                    url};
            try {
                Process process=rn.exec(command);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Process Output: " + line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return 0;
        }
    };
    RecursiveAction action = new RecursiveAction() {

        @Override
        protected void compute() {

            String[] command = {  "yt-dlp",
                    "-x",
                    "--audio-format", "wav",
                    "--no-playlist", "--no-warnings",
                    "-P", rutaWav,
                    url};
            try {
                Process process=rn.exec(command);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Process Output: " + line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    action.fork();
    task.fork().join();
    long end=System.currentTimeMillis();
    log.info("Total time: {}ms", end - start);
}
}

