package com.surrogate.springfy.services.bussines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.YT.SearchResponse;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.models.bussines.Audio;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final AudioRepository audioRepository;
    //ejemplo de llamada a la api de youtube -----> https://www.googleapis.com/youtube/v3/search?key=AIzaSyAxhEx5CTiVejjsJbUwoc1xA0ppupc7W0A&q=tu novio no la hace slowed&type=video&part=snippet&maxResults=4&totalResults=10
    @Value("${springfy.key}")
    String key;

WebClient httpClient = WebClient.create();
    //mala practica maldita key hardcodeada pero te pensas que me imporrrrrta negro de mierrrrrrrrda


    //despues puedo hacer mas filtros jejeeee broooo jeejejee
    public SearchResponse searchByNombre(String nombre) throws URISyntaxException, IOException, InterruptedException {
        String newUrl= buildUrl()+nombre;

        log.info("Request hacia -> {}", newUrl);
        YouTubeSearchResponse ytSearchResponse = httpClient.get()
                .uri(newUrl)
                .retrieve()
                .bodyToMono(YouTubeSearchResponse.class)
                .block();
        assert ytSearchResponse != null;
        List<SearchResponse.VideoInfo> videoInfos= new ArrayList<>();
        ytSearchResponse.items().forEach(item -> {
            SearchResponse.VideoInfo videoInfo= new SearchResponse.VideoInfo(item.id().videoId(), item.snippet().title(),item.snippet().channelTitle()
            );
            videoInfos.add(videoInfo);

        });
        return new SearchResponse(videoInfos);

    }
    public String buildUrl() {
        return "https://www.googleapis.com/youtube/v3/search"
                + "?key=" + key
                + "&type=video"
                + "&part=snippet"
                + "&maxResults=1"
                + "&q=";
    }


public List<AudioDTO> AllInCloud()  {

        return audioRepository.findAllAudios();

}


}
