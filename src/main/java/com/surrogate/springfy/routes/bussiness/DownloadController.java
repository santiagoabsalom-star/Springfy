package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.YT.DownloadRequest;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.services.bussines.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/download")
public class DownloadController {
    private final DownloadService downloadService;

    @PostMapping(value = "/downloadOnCloud", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> downloadOnCloud(@RequestBody DownloadRequest request)  {
        String videoId = request.videoId();
        Response response = downloadService.downloadOnCloud(videoId);
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    @PostMapping(value = "/downloadOnApp", produces = "audio/mpeg", consumes = "application/json")
    public ResponseEntity<Resource> downloadOnApp(@RequestBody DownloadRequest request)  {
        String videoId = request.videoId();
        log.info("Intentando descargar el audio con id: {}", videoId);

        Resource resource = downloadService.downloadOnApp(videoId);
        if (resource != null && resource.exists()) {
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return new ResponseEntity<>(resource, HttpStatus.NOT_FOUND);
    }

}
