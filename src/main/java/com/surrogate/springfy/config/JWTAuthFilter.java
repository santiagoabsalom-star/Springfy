package com.surrogate.springfy.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.surrogate.springfy.services.auth.JWT.JWTService;
import com.surrogate.springfy.utils.UserDetailsServiceWithId;
import com.surrogate.springfy.utils.UserDetailsWithId;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsServiceWithId userDetailsService;
    private Cache<String, UserDetailsWithId> userDetailsCache;


    @PostConstruct
    public void init() {
        userDetailsCache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();

    }

    @Override
    protected boolean shouldNotFilter(@org.jetbrains.annotations.NotNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register") || path.startsWith("/api/search")
                || path.startsWith("/api/download");
    }


    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws IOException {

        try {




                String path = request.getServletPath();
            log.info("Path requested: {}", path);
            log.info("Request host: {}", request.getRemoteHost());
            if (path.startsWith("/swagger-ui") || path.contains("solicitudInstitucion") || path.startsWith("/post/") || path.startsWith("/web") || path.startsWith("/v3/api-docs") || path.startsWith("/VAADIN/") || path.startsWith("/src/main/frontend/") || path.startsWith("/webjars/") || path.startsWith("/public/") || path.startsWith("/chat") || path.startsWith("/api/publicacion/obtenerPublicacionesPublicas")) {

                filterChain.doFilter(request, response);
                return;
            }


            if (path.startsWith("/favicon.ico") || path.startsWith("/actuator") || path.startsWith("/favicon") || path.startsWith("/web/**")) {

                filterChain.doFilter(request, response);
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            final Cookie[] authCookie = request.getCookies();


            String authTokenFromCookie = authCookie != null ? authCookie[0].getValue() : null;

            if (authTokenFromCookie == null) {

                sendError(response, "El header tiene que venir con el token malparido", HttpServletResponse.SC_UNAUTHORIZED);

                return;
            }
            String jwt = null;
            try {

                if (authHeader == null) {
                    jwt = authTokenFromCookie;

                } else {
                    jwt = authHeader.substring(7);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }


            assert jwt != null;
            if (jwt.isBlank()) {
                sendError(response, "El header no puede estar vacio", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!jwtService.isValidTokenFormat(jwt) || jwtService.isTokenStored(jwt)) {
                sendError(response, "Token invalidado", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
//            if (!jwtService.isTokenFromIp(request.getRemoteAddr(), jwt)) {
//                jwtService.InvalidateToken(jwt);
//                sendError(response, "Distinta ip para token", HttpServletResponse.SC_UNAUTHORIZED);
//            }

            String username = jwtService.extractUsername(jwt);

            if (username != null) {
                UserDetailsWithId userDetails = userDetailsCache.getIfPresent(username);
                if (userDetails == null) {
                    userDetails = userDetailsService.loadUserByUsername(username);
                    userDetailsCache.put(username, userDetails);
                }


                if (!jwtService.validateToken(jwt, userDetails)) {
                    sendError(response, "Token expirado o invalidado", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                setAuthentication(userDetails, request);

            }

            filterChain.doFilter(request, response);
        } catch (JwtException | AuthenticationException e) {
            sendError(response, "Authentication error: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            sendError(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }



    private void setAuthentication(UserDetailsWithId userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}
