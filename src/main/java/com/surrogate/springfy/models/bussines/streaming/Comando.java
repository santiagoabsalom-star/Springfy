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
    int segundos;
    @JsonSetter(nulls = Nulls.SKIP)
    String musicId;
    String seguidor, anfitrion;
@JsonCreator
    public Comando(@JsonProperty("comando") String comando,
                   @JsonProperty("segundos") int segundos,
                   @JsonProperty("musicId") String musicId,
                   @JsonProperty("anfitrion") String anfitrion,
                   @JsonProperty("seguidor") String seguidor) {
        this.anfitrion = anfitrion;
        this.seguidor = seguidor;
    this.comando = comando;
    this.segundos =  segundos;
    this.musicId = musicId;
}
public Comando() {}




}
