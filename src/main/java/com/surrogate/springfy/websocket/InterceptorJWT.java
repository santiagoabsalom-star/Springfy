//package com.surrogate.springfy.websocket;
//
//
//import com.surrogate.numy.services.auth.JWT.JWTService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import java.net.URI;
//import java.net.http.WebSocketHandshakeException;
//import java.util.Map;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class InterceptorJWT implements HandshakeInterceptor {
//    private final JWTService jwtService;
//    //todo : implementar la validacion del token JWT en el beforeHandshake
//    @Override
//    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) throws WebSocketHandshakeException {
//
//        if(request.getURI().getPath().contains("/post")) {return true;
//        }
//
//        URI uri = request.getURI();
//
//        String[] atributos = uri.getQuery().split("&");
//
//        String nombreChatParam=atributos[0].split("=")[1] != null ? atributos[0].split("=")[1] : null;
//        String nombreUsuarioParam=atributos[1].split("=")[1] != null ? atributos[1].split("=")[1] : null;
//
//        log.info("Parametros recibidos en el handshake: NombreChat={}, NombreUsuario={}", nombreChatParam, nombreUsuarioParam);
//
//
//        if(nombreUsuarioParam!=null && nombreChatParam!=null){
//            attributes.put("NombreChat", nombreChatParam);
//            attributes.put("Nombre", nombreUsuarioParam);
//        }
//
//        return true;
//
//    }
//    @Override
//    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {
//
//        log.info("Handshake hecho para la Ip: {}", request.getRemoteAddress().getHostName());
//    }
//}
