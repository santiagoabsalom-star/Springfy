package com.surrogate.springfy.models.DTO.Uso;
import java.util.List;
public record UsoSemanalDTO(List<UsoDiarioDTO> usoDiario) {
}
record UsoMensualDTO(List<UsoDiarioDTO> usoDiario) {}