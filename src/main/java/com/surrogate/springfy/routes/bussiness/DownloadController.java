package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.YT.DonwloadRequest;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
import com.surrogate.springfy.services.bussines.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/download")
public class DownloadController {
    private final DownloadService downloadService;

    @GetMapping(value = "/downloadOnCloud", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> downloadOnCloud(@RequestBody DonwloadRequest request) throws IOException {
        String videoId= request.videoId();
        Response response= downloadService.downloadOnCloud(videoId);
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
    @GetMapping(value="/downloadOnApp", produces="audio/mpeg", consumes = "application/json")
    public ResponseEntity<Resource> downloadOnApp(@RequestBody DonwloadRequest request) throws IOException {
        String videoId= request.videoId();

        Resource resource= downloadService.downloadOnApp(videoId);
        if(resource!=null && resource.exists() ){
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return new ResponseEntity<>(resource, HttpStatus.NOT_FOUND);
    }

}
