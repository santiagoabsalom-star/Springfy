package com.surrogate.springfy;

import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.services.bussines.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootTest
class SpringfyApplicationTests {
    SearchService searchService = new SearchService();

    @Test
    void contextLoads() throws URISyntaxException, IOException, InterruptedException {
        SearchService searchService = new SearchService();
        YouTubeSearchResponse t= searchService.searchByNombre("holey");

        System.out.println(t.toString());
    }

}
