package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO;
import com.surrogate.springfy.models.DTO.Uso.UsoSemanalDTO;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.services.bussines.UsageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static com.surrogate.springfy.routes.bussiness.StreamingController.getTokenFromRequest;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/usage")
public class UsageController {
    private final UsageService usageService;
    @GetMapping("/uso_semanal_primer_plano")
    public ResponseEntity<UsoSemanalDTO> usoSemanalPrimerPlano(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        UsoSemanalDTO usoSemanalDTO= usageService.usoSemanalPrimerPlanoDTO(token);
        if(usoSemanalDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usoSemanalDTO);
    }
    @GetMapping("/uso_semanal_segundo_plano")
    public ResponseEntity<UsoSemanalDTO> usoSemanalSegundoPlano(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        UsoSemanalDTO usoSemanalDTO= usageService.usoSemanalSegundoPlanoDTO(token);
        if(usoSemanalDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usoSemanalDTO);
    }
    @GetMapping("/uso_diario_primer_plano")
    public ResponseEntity<UsoDiarioDTO> usoDiarioPrimerPlano(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        UsoDiarioDTO usoDiarioDTO= usageService.usoDiarioPrimerPlanoDTO(token);
        if(usoDiarioDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usoDiarioDTO);
    }
    @GetMapping("/uso_diario_segundo_plano")
    public ResponseEntity<UsoDiarioDTO> usoDiarioSegundoPlano(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        UsoDiarioDTO usoDiarioDTO= usageService.usoDiarioSegundoPlanoDTO(token);
        if(usoDiarioDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usoDiarioDTO);
    }

    @PostMapping("/iniciar_primer_plano/{nombre}")
    public ResponseEntity<Response> iniciarPrimerPlano(@PathVariable String nombre) {
        Response response = usageService.iniciar_primer_plano(nombre);

        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
    @PostMapping("/terminar_primer_plano/{nombre}")
    public ResponseEntity<Response> terminarPrimerPlano(@PathVariable String nombre) {
        Response response = usageService.terminar_primer_plano(nombre);

        return ResponseEntity.status(response.getHttpCode()).body(response);
    } @PostMapping("/iniciar_segundo_plano/{nombre}")
    public ResponseEntity<Response> iniciarSegundoPlano(@PathVariable String nombre) {
        Response response = usageService.iniciar_segundo_plano(nombre);

        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
    @PostMapping("/terminar_segundo_plano/{nombre}")
    public ResponseEntity<Response> terminarSegundoPlano(@PathVariable String nombre) {
        Response response = usageService.terminar_segundo_plano(nombre);

        return ResponseEntity.status(response.getHttpCode()).body(response);
    }


}
