package com.surrogate.springfy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull @org.jetbrains.annotations.NotNull HttpServletResponse response,
                                    @NotNull @org.jetbrains.annotations.NotNull FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();
        if (path.startsWith("/web/") || path.startsWith("/VAADIN/") ) {
            filterChain.doFilter(request, response);
            return;
        }


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
