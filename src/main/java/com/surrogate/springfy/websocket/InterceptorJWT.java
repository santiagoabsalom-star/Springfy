package com.surrogate.springfy.websocket;


import com.surrogate.springfy.services.auth.JWT.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.http.WebSocketHandshakeException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class InterceptorJWT implements HandshakeInterceptor {
    private final JWTService jwtService;
    //todo : implementar la validacion del token JWT en el beforeHandshake
    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) throws WebSocketHandshakeException {

        String anfitrion= request.getHeaders().getFirst("Anfitrion");
        String usuario= request.getHeaders().getFirst("Usuario");
        String seguidor= request.getHeaders().getFirst("Seguidor");
        assert anfitrion != null;
        if(anfitrion.equals(usuario)){
        String cancion_id= request.getHeaders().getFirst("Cancion_id");
        attributes.put("Cancion_id", cancion_id);
        }
        attributes.put("Usuario", usuario);
        attributes.put("Seguidor", seguidor);
        attributes.put("Anfitrion", anfitrion);

        return true;


    }
    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {

        log.info("Handshake hecho para la Ip: {}", request.getRemoteAddress().getHostName());
    }
}
