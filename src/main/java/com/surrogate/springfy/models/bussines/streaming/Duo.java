package com.surrogate.springfy.models.bussines.streaming;

import com.surrogate.springfy.models.bussines.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Duo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duo", nullable = false)
    Long idDuo;


    @JoinColumn(name="id_usuario1", nullable = false)

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario id_usuario1;
    @JoinColumn(name="id_usuario2", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario id_usuario2;

}
