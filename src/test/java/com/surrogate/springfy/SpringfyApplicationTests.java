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


    }


}

