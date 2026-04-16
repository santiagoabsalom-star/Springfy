package com.surrogate.springfy.models.bussines.Usage;

import com.surrogate.springfy.models.bussines.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "uso")
public class Usage {
    @Id
    @Column(name="id_usage", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsage;
    @JoinColumn(name = "id_usuario", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
    @Column(name="tipo")
    private Tipo tipo;
    @Column(name="tiempo")
    private Integer tiempo;
    @Column(name="timestamp_realizado")
    private LocalDateTime timestampRealizado;



}
