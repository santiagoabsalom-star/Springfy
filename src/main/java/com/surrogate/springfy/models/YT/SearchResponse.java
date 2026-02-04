package com.surrogate.springfy.models.YT;

import java.util.List;

public record SearchResponse(List<VideoInfo> videosInfo) {


    public record VideoInfo(String videoId, String title, String channelTitle) {
    }

}
