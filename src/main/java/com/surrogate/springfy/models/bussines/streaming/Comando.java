package com.surrogate.springfy.models.bussines.streaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;

@Getter

public class Comando {
    String comando;
    @JsonSetter(nulls = Nulls.SKIP)
    long segundos;
    @JsonSetter(nulls = Nulls.SKIP)
    String musicId;
    String seguidor, anfitrion;
@JsonCreator
    public Comando(@JsonProperty("comando") String comando,
                   @JsonProperty("segundos") long segundos,
                   @JsonProperty("musicId") String musicId,
                   @JsonProperty("anfitrion") String anfitrion,
                   @JsonProperty("seguidor") String seguidor) {
        this.anfitrion = anfitrion;
        this.seguidor = seguidor;
    this.comando = comando;
    this.segundos = segundos;
    this.musicId = musicId;
}
public Comando() {}




}
