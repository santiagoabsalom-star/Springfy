package com.surrogate.springfy;


import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.services.bussines.SearchService;

class SpringfyApplicationTests {
    private final SearchService searchService;
    SpringfyApplicationTests(SearchService searchService) {
        this.searchService = searchService;
    }
    public static void main(String[] args) throws Exception {
        SpringfyApplicationTests tests = new SpringfyApplicationTests(new SearchService());
        YouTubeSearchResponse response = tests.searchService.searchByNombre("tunovionolahaceslowed");
        System.out.println("Total Results: " + response.pageInfo().totalResults());
        response.items().forEach(item -> {
            System.out.println("Title: " + item.snippet().title());
            System.out.println("Video ID: " + item.id().videoId());
            System.out.println("Published At: " + item.snippet().publishedAt());
            System.out.println("Description: " + item.snippet().description());
            System.out.println("-----");
        });
    }


}

