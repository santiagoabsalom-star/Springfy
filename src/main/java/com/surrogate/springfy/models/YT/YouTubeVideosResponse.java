package com.surrogate.springfy.models.YT;

import java.util.List;

public record YouTubeVideosResponse(
        List<Item> items
) {

    public record Item(
            String id,
            ContentDetails contentDetails
    ) {}

    public record ContentDetails(
            String duration
    ) {}
}
