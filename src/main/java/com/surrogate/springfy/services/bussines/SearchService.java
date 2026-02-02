package com.surrogate.springfy.services.bussines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
//ejemplo de llamada a la api de youtube -----> https://www.googleapis.com/youtube/v3/search?key=AIzaSyAxhEx5CTiVejjsJbUwoc1xA0ppupc7W0A&q=tu novio no la hace slowed&type=video&part=snippet&maxResults=4&totalResults=10
    @Value("${SpringfyKey}")
    String key;
WebClient httpClient = WebClient.create();
    //mala practica maldita key hardcodeada pero te pensas que me imporrrrrta negro de mierrrrrrrrda
    String url = "https://www.googleapis.com/youtube/v3/search?key=AIzaSyAxhEx5CTiVejjsJbUwoc1xA0ppupc7W0A&type=video&part=snippet&maxResults=3&totalResults=2"+"&q=";

    public YouTubeSearchResponse searchByNombre(String nombre) throws URISyntaxException, IOException, InterruptedException {
        String newUrl= url+nombre;
        log.info("Request hacia -> {}", newUrl);

        return httpClient.get()
                .uri(url+nombre)
                .retrieve()
                .bodyToMono(YouTubeSearchResponse.class)
                .block();
    }



}
