package com.surrogate.springfy.models.bussines;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "usuarios", schema = "springfy", uniqueConstraints = {
        @UniqueConstraint(name = "usuario", columnNames = {"nombre"})
})


public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name="imagen_url")
    private String imagenUrl;
    @Lob
    @Column(name = "rol", nullable = false)
    private String rol;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name="biografia")
    private String biografia;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name= "uuid", nullable = false)
    private String uuid;


}

