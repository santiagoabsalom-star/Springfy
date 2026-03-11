package com.surrogate.springfy.models.bussines.streaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.bussines.Audio;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Comando {
    String comando;
    @JsonSetter(nulls = Nulls.SKIP)
    int segundosToMove;
    @JsonSetter(nulls = Nulls.SKIP)
    int currentPosition;
    @JsonSetter(nulls = Nulls.SKIP)
    String musicId;
    @JsonSetter(nulls = Nulls.SKIP)
            int duration;

    @JsonSetter(nulls = Nulls.SKIP)
            @JsonProperty("isPlaying")
            boolean playing;
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty("isRepeating")
    boolean repeating;
    String seguidor, anfitrion;

    @JsonSetter(nulls = Nulls.SKIP)
    List<String> currentPlaylist;



    public Comando() {}




}
