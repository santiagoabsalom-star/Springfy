package com.surrogate.springfy.models.bussines;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Audio {
    @Id
    @Column(name = "id_audio", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="tipo", nullable = false)
    private String tipo;
    @Column(name = "nombre_audio", nullable = false)
    private String nombreaudio;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "audio_Id", nullable = false)
    private String audioId;

}
