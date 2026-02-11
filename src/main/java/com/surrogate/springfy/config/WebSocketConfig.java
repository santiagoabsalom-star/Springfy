package com.surrogate.springfy.config;
//

import com.surrogate.springfy.websocket.InterceptorJWT;
import com.surrogate.springfy.websocket.StreamWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final InterceptorJWT interceptorJWT;
    private final StreamWebSocketHandler streamWebSocketHandler;
    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(streamWebSocketHandler
                , "/stream/**")
                .addInterceptors(interceptorJWT)
                .setAllowedOrigins("*");


    }
}
