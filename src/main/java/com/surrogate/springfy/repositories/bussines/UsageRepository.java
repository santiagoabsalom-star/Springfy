package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO;
import com.surrogate.springfy.models.bussines.Usage.Tipo;
import com.surrogate.springfy.models.bussines.Usage.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface UsageRepository extends JpaRepository<Usage, Long> {
    @Query("Select new com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO(u.tiempo) from Usage u where ((u.timestampRealizado between :inicioSemana AND :finSemana) AND u.usuario.nombre = :nombre) AND u.tipo = :tipo")
    List<UsoDiarioDTO> usoSemanalByNombre(String nombre, LocalDateTime inicioSemana, LocalDateTime finSemana, Tipo tipo);
    @Query("Select new com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO(u.tiempo) from Usage u where u.usuario.nombre = :nombre AND u.tipo = :tipo")
    UsoDiarioDTO usoDiarioDTO(String nombre, Tipo tipo);


}
