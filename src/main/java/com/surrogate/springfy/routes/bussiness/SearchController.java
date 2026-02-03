package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.YT.SearchRequest;
import com.surrogate.springfy.models.YT.SearchResponse;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.services.bussines.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/search")
public class SearchController {
private final SearchService searchService;



@PostMapping(value = "/by-name", produces = "application/json", consumes = "application/json")
public ResponseEntity<SearchResponse> searchByName(@RequestBody SearchRequest name) throws Exception {
        String nombre = name.name();

       SearchResponse response = searchService.searchByNombre(nombre);
return ResponseEntity.ok(response);}
}
