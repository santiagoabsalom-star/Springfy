package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.DTO.DuoRequest;
import com.surrogate.springfy.models.bussines.streaming.Duo;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.repositories.bussines.DuoRepository;
import com.surrogate.springfy.repositories.bussines.UsuarioRepository;
import com.surrogate.springfy.services.auth.JWT.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {
    private final DuoRepository duoRepository;
    private final UsuarioRepository usuarioRepository;
   private final  JWTService jwtService;


    public String getDuoByUsername(String token){
    String username= getUsernameFromToken(token);
    if(username==null){
        return null;
    }

        return duoRepository.getOtherUsername(username);
    }
    private String getUsernameFromToken(String token) {

        return jwtService.extractUsername(token);

    }

    public Response createDuo(DuoRequest duoRequest) {
        if(usuarioRepository.existsByNombre(duoRequest.username1()) && usuarioRepository.existsByNombre(duoRequest.username2())) {
            Duo duo = new Duo();
            duo.setId_usuario1(usuarioRepository.findUsuarioByNombre(duoRequest.username1()));

            duo.setId_usuario2(usuarioRepository.findUsuarioByNombre(duoRequest.username2()));
            duoRepository.save(duo);
            return new Response("success", 200, "Duo creado con exito");
        }else{
return new Response("error",404, "No existe alguno de los dos usuarios");}
    }
    public List<String> getAllNombres(String token){
        String username = getUsernameFromToken(token);
        return usuarioRepository.getAllNombres(username);
    }


}
