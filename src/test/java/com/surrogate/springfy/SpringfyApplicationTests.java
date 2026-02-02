package com.surrogate.springfy;


import com.surrogate.springfy.models.YT.SearchResponse;
import com.surrogate.springfy.models.YT.YouTubeSearchResponse;
import com.surrogate.springfy.services.bussines.SearchService;

class SpringfyApplicationTests {
    private final SearchService searchService;
    SpringfyApplicationTests(SearchService searchService) {
        this.searchService = searchService;
    }
    public static void main(String[] args) throws Exception {
        SpringfyApplicationTests tests = new SpringfyApplicationTests(new SearchService());
        SearchResponse response = tests.searchService.searchByNombre("tunovionolahaceslowed");

    }


}

