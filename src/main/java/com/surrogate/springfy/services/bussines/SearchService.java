package com.surrogate.springfy.services.bussines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class SearchService {
//ejemplo de llamada a la api de youtube -----> https://www.googleapis.com/youtube/v3/search?key=AIzaSyAxhEx5CTiVejjsJbUwoc1xA0ppupc7W0A&q=tu novio no la hace slowed&type=video&part=snippet&maxResults=4&totalResults=10
    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper mapper;
    @Value("${SpringfyKey}")
    String key;
    String url = "https://www.googleapis.com/youtube/v3/search?key=" +key+"&type=video&part=snippet&maxResults=4&totalResults=10"+"&q=";

    public YouTubeSearchResponse searchByNombre(String nombre) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(new URI(url+nombre)).build();

        HttpResponse<String> response= client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), YouTubeSearchResponse.class);


    }



}
