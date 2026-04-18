package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO;
import com.surrogate.springfy.models.bussines.Usage.Tipo;
import com.surrogate.springfy.models.bussines.Usage.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface UsageRepository extends JpaRepository<Usage, Long> {
    @Query("Select new com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO(u.tiempo,u.timestampRealizado) from Usage u where ((u.timestampRealizado between :inicioPeriodo AND :finPeriodo) AND u.usuario.nombre = :nombre) AND u.tipo = :tipo")
    List<UsoDiarioDTO> usoBetween(String nombre, LocalDateTime inicioPeriodo, LocalDateTime finPeriodo, Tipo tipo);
    @Query("Select new com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO(u.tiempo,u.timestampRealizado) from Usage u where ((u.timestampRealizado between :inicioDia AND :finDia) AND u.usuario.nombre = :nombre) AND u.tipo = :tipo")
    List<UsoDiarioDTO> usoDiarioDTO(String nombre, Tipo tipo, LocalDateTime inicioDia, LocalDateTime finDia);
    @Query("Select sum(u.tiempo) from Usage u where u.tipo = :tipo")
    int countByTipo(Tipo tipo);


}
