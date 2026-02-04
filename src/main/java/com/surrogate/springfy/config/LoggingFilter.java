package com.surrogate.springfy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    @Value("${springfy.app.id}")
    private String AppHeader;
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull @org.jetbrains.annotations.NotNull HttpServletResponse response,
                                    @NotNull @org.jetbrains.annotations.NotNull FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String appHeader= request.getHeader("Application-id");
        String useragent = request.getHeader("User-Agent");
        if (useragent.toLowerCase().contains("mozilla") || useragent.toLowerCase().contains("chrome") || useragent.toLowerCase().contains("firefox") || useragent.toLowerCase().contains("safari")) {
            logger.info("Intento de request desde el navegador: {}", useragent);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        }
        if(!appHeader.equals(AppHeader)) {
            sendError(response);
        }
        else {


            logger.info("Incoming request: {} {} from IP {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                logger.error("Exception during request: {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
                throw e;
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Completed request: {} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }
        }
    }
    private void sendError(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + "App-id no encontrada o no es valida, vuelva por donde volvio:)" + "\"}");
        response.getWriter().flush();
    }
}
