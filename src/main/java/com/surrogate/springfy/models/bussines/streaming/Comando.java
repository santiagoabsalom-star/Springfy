package com.surrogate.springfy.models.bussines.streaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.Setter;

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
    String seguidor, anfitrion;
@JsonCreator
    public Comando(@JsonProperty("comando") String comando,
                   @JsonProperty("isPlaying") boolean playing,
                   @JsonProperty("duration") int duration,
                   @JsonProperty("currentPosition") int currentPosition,
                   @JsonProperty("segundosToMove") int segundos,
                   @JsonProperty("musicId") String musicId,
                   @JsonProperty("anfitrion") String anfitrion,
                   @JsonProperty("seguidor") String seguidor) {
        this.anfitrion = anfitrion;
        this.seguidor = seguidor;
    this.comando = comando;
    this.duration = duration;
    this.segundosToMove =  segundos;
    this.playing = playing;
    this.currentPosition = currentPosition;
    this.musicId = musicId;
}

    public Comando() {}




}
