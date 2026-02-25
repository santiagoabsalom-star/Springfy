package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.YT.SearchResponse;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.models.YT.YouTubeVideosResponse;
import com.surrogate.springfy.repositories.bussines.AudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public SearchResponse searchByNombre(String nombre) {
        String newUrl = buildUrl() + nombre;

        YouTubeSearchResponse ytSearchResponse = httpClient.get()
                .uri(newUrl)
                .retrieve()
                .bodyToMono(YouTubeSearchResponse.class)
                .onErrorResume(e -> Mono.empty())
                .block();

        if (ytSearchResponse == null) {
            return null;
        }

        List<String> ids = ytSearchResponse.items().stream()
                .map(item -> item.id().videoId())
                .toList();

        if (ids.isEmpty()) {
            return new SearchResponse(List.of());
        }

        String detailsUrl = buildVideoDetailsUrl(ids);


        YouTubeVideosResponse detailsResponse = httpClient.get()
                .uri(detailsUrl)
                .retrieve()
                .bodyToMono(YouTubeVideosResponse.class)
                .block();

        if (detailsResponse == null) {
            return null;
        }

        Map<String, Long> durationMap = new HashMap<>();

        detailsResponse.items().forEach(item -> {
            String videoId = item.id();
            String isoDuration = item.contentDetails().duration();

            long seconds = Duration.parse(isoDuration).getSeconds();
            durationMap.put(videoId, seconds);
        });

        List<SearchResponse.VideoInfo> videoInfos = ytSearchResponse.items().stream()
                .filter(item -> {
                    Long duration = durationMap.get(item.id().videoId());
                    return duration != null && duration <= 5400;
                })
                .map(item -> new SearchResponse.VideoInfo(
                        item.id().videoId(),
                        item.snippet().title(),
                        item.snippet().channelTitle()
                ))
                .toList();

        return new SearchResponse(videoInfos);
    }

    public String buildVideoDetailsUrl(List<String> ids) {

        String idsString = String.join(",", ids);

        return "https://www.googleapis.com/youtube/v3/videos"
                + "?key=" + key
                + "&part=contentDetails"
                + "&id=" + idsString;
    }

    public String buildUrl() {
        return "https://www.googleapis.com/youtube/v3/search"
                + "?key=" + key
                + "&type=video"
                + "&part=snippet"
                + "&maxResults=13"
                + "&q=";
    }


    public List<AudioDTO> AllInCloudMp3() {
        return audioRepository.findAllAudiosMp3();

    }
    public List<AudioDTO> AllInCloudWav() {
        return audioRepository.findAllAudiosWav();
    }


}
