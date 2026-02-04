package com.surrogate.springfy.models.bussines;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor

public class Audio {
    @Id
    @Column(name = "id_audio", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_audio", nullable = false)
    private String nombreaudio;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "audio_Id", nullable = false, unique = true)
    private String audioId;

}
