package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.YT.SearchRequest;
import com.surrogate.springfy.models.YT.SearchResponse;
import com.surrogate.springfy.services.bussines.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;


    @PostMapping(value = "/by-name", produces = "application/json", consumes = "application/json")
    public ResponseEntity<SearchResponse> searchByName(@RequestBody SearchRequest name) throws Exception {
        String nombre = name.name();

        SearchResponse response = searchService.searchByNombre(nombre);
        if(response == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(response);

    }

    @GetMapping(value = "/search-all-mp3", produces = "application/json")
    public ResponseEntity<List<AudioDTO>> searchAll() {
        List<AudioDTO> audioDTOS = searchService.AllInCloudMp3();
        return ResponseEntity.of(Optional.ofNullable(audioDTOS));
    }
    @GetMapping(value="search-all-wav", produces = "application/json")
    public ResponseEntity<List<AudioDTO>> searchAllWav() {
        List<AudioDTO> audioDTOS = searchService.AllInCloudWav();
        return ResponseEntity.of(Optional.ofNullable(audioDTOS));
    }
}
