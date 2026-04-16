package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.DTO.Uso.UsoDiarioDTO;
import com.surrogate.springfy.models.DTO.Uso.UsoSemanalDTO;
import com.surrogate.springfy.models.bussines.Usage.Tipo;
import com.surrogate.springfy.models.bussines.Usage.Usage;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.repositories.bussines.UsageRepository;
import com.surrogate.springfy.repositories.bussines.UsuarioRepository;
import com.surrogate.springfy.services.auth.JWT.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;


@Service
@RequiredArgsConstructor
@Log4j2
public class UsageService {

    private final HashMap<String, Long> segundo_plano= new HashMap<>();
    private final HashMap<String, Long> primer_plano= new HashMap<>();
    private final JWTService jwtService;
    private final UsageRepository usageRepository;
    private final UsuarioRepository usuarioRepository;
    private final String success;
    private final String error;


    public Response iniciar_segundo_plano(String nombre){

        segundo_plano.put(nombre,System.currentTimeMillis());
        return new Response(success,200,"Segundo plano iniciado");

    }
    public Response terminar_segundo_plano(String nombre){
        if(segundo_plano.containsKey(nombre)){
        Usage usage=new Usage();
        usage.setTimestampRealizado(LocalDateTime.now());
        usage.setTipo(Tipo.SEGUNDO_PLANO);
        usage.setUsuario(usuarioRepository.findUsuarioByNombre(nombre));
        int conteo = (int) ((System.currentTimeMillis() - segundo_plano.get(nombre)) / 1000);
        usage.setTiempo(conteo == 0 ? 1: conteo);

        usageRepository.save(usage);
        segundo_plano.remove(nombre);


        return new Response(success,200,"Segundo plano terminado");
        }

        return new Response(error,404,"No encontrado en map");

    }
    public Response iniciar_primer_plano(String nombre){
        primer_plano.put(nombre,System.currentTimeMillis());
        return new Response(success,200,"Primer plano iniciado");
    }
    public Response terminar_primer_plano(String nombre){
        if(primer_plano.containsKey(nombre)) {
            Usage usage = new Usage();
            ZoneId uruguay= ZoneId.of("America/Montevideo");

            usage.setTimestampRealizado(LocalDateTime.now(uruguay));
            usage.setTipo(Tipo.PRIMER_PLANO);
            usage.setUsuario(usuarioRepository.findUsuarioByNombre(nombre));
            int conteo =  (int) ((System.currentTimeMillis() - primer_plano.get(nombre)) / 1000);
            usage.setTiempo(conteo == 0 ? 1 : conteo);
            usageRepository.save(usage);
            primer_plano.remove(nombre);
            return new Response(success,200,"Primer plano terminado");
        }
        return new Response(error,404,"No encontrado en map");
    }
    public UsoSemanalDTO usoSemanalSegundoPlanoDTO(String token){
        String nombre= jwtService.extractUsername(token);
        if(LocalDateTime.now().getDayOfWeek()== DayOfWeek.SUNDAY) {
            List<UsoDiarioDTO> usoDiario = usageRepository.usoSemanalByNombre(nombre, LocalDateTime.now().minusWeeks(1), LocalDateTime.now(), Tipo.SEGUNDO_PLANO);
            int uso_semanal = 0;
            for (UsoDiarioDTO dto : usoDiario) {
                uso_semanal = uso_semanal + dto.usoDiario();
            }
            if (uso_semanal == 0) {
                return null;
            }
            return new UsoSemanalDTO(uso_semanal);
        }return null;
        //sumar tiempos y devolver el usoDto, tambien podemos usar un dto con mas parametros, uso semanal, uso diario promedio, horas en las cuales se usa mas etc etc.
        //por ahora hacemos usoSemanal solo
    }
    public UsoSemanalDTO usoSemanalPrimerPlanoDTO(String token){
        String nombre= jwtService.extractUsername(token);
        if(LocalDateTime.now().getDayOfWeek()== DayOfWeek.SUNDAY) {
            List<UsoDiarioDTO> usoDiario = usageRepository.usoSemanalByNombre(nombre, LocalDateTime.now().minusWeeks(1), LocalDateTime.now(), Tipo.PRIMER_PLANO);
            int uso_semanal = 0;
            for (UsoDiarioDTO dto : usoDiario) {
                uso_semanal = uso_semanal + dto.usoDiario();
            }
            if (uso_semanal == 0) {
                return null;
            }
            return new UsoSemanalDTO(uso_semanal);
        }return null;
        //sumar tiempos y devolver el usoDto, tambien podemos usar un dto con mas parametros, uso semanal, uso diario promedio, horas en las cuales se usa mas etc etc.
        //por ahora hacemos usoSemanal solo
    }
    public UsoDiarioDTO usoDiarioPrimerPlanoDTO(String token){
        String nombre= jwtService.extractUsername(token);
        return usageRepository.usoDiarioDTO(nombre,Tipo.PRIMER_PLANO);
    }
    public UsoDiarioDTO usoDiarioSegundoPlanoDTO(String token){
        String nombre= jwtService.extractUsername(token);
        return usageRepository.usoDiarioDTO(nombre,Tipo.SEGUNDO_PLANO);
    }


}
