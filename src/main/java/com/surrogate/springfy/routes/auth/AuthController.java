package com.surrogate.springfy.routes.auth;


import com.surrogate.springfy.models.login.LoginRequest;
import com.surrogate.springfy.models.login.LoginResponse;
import com.surrogate.springfy.models.register.RegisterRequest;
import com.surrogate.springfy.models.register.RegisterResponse;
import com.surrogate.springfy.services.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final String error;


    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);

            if (response.getHttpCode() == 200) {

                return ResponseEntity.ok()
                        .header("X-Content-Type-Options", "nosniff")
                        .header("X-Frame-Options", "DENY")
                        .header("X-XSS-Protection", "1; mode=block")
                        .body(response);
            } else {
                return ResponseEntity.status(response.getHttpCode())
                        .header("X-Content-Type-Options", "nosniff")
                        .header("X-Frame-Options", "DENY")
                        .header("X-XSS-Protection", "1; mode=block")
                        .body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new LoginResponse(error, 500, "Error interno del servidor"));
        }
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {

            RegisterResponse response = authService.register(registerRequest);
            return getRegisterResponseResponseEntity(response);
        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(new RegisterResponse(error, "Error interno del servidor"));

        }
    }

    @PostMapping(value = "/admin/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse response = authService.registerAdmin(registerRequest);
            return getRegisterResponseResponseEntity(response);
        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(new RegisterResponse(error, "Error interno del servidor" + e.getMessage()));

        }
    }


    private ResponseEntity<RegisterResponse> getRegisterResponseResponseEntity(RegisterResponse response) {
        if (200 == (response.getHttpCode())) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")
                    .body(response);

        } else {
            return ResponseEntity.status(response.getHttpCode())
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")
                    .body(response);
        }
    }


    @PostMapping(value = "/logout", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            String token = Objects.requireNonNull(Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("AUTH_TOKEN")).findFirst().orElse(null)).getValue();

            String response = authService.logout(token);


            if (response.equals("success")) {
                return ResponseEntity.ok()
                        .header("X-Content-Type-Options", "nosniff")
                        .header("X-Frame-Options", "DENY")
                        .header("X-XSS-Protection", "1; mode=block")
                        .body("Logout success");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)

                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")

                    .body("Logout fallido: Token inválido o no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")
                    .body("Error interno del servidor");

        }
    }

//    @GetMapping(value = "/me", produces = "application/json")
//    public ResponseEntity<Optional<UsuarioDto>> me(HttpServletRequest request) {
//
//        String token = getTokenFromRequest(request);
//
//
//        if (token == null) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//        Optional<UsuarioDto> usuarioDto = usuarioService.me(token);
//        if (usuarioDto.isEmpty()) {
//
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT) ;
//
//        }
//
//        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
//    }
//
//    @GetMapping("/accounts")
//    public ResponseEntity<List<UsuarioDto>> getAccounts() {
//
//
//
//        List<UsuarioDto> usuarioDto = usuarioService.findAllByEmail();
//        if (usuarioDto == null) {
//
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//
//        }
//        return new ResponseEntity<>(usuarioDto, HttpStatus.OK);
//    }
//    @PostMapping("/updateCurrentUser")
//    public ResponseEntity<Response> updateCurrentUser(HttpServletRequest request, @RequestBody Usuario usuario) {
//
//            String token = getTokenFromRequest(request);
//            if (token == null) {
//                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//            }
//            Response response = usuarioService.updateCurrentUser(token, usuario);
//            return ResponseEntity.status(response.getHttpCode()).body(response);
//        }
//
//    private String getTokenFromRequest(HttpServletRequest request) {
//        if (request.getCookies() == null || request.getCookies().length == 0) {
//
//        return null;
//        }
//
//        return Objects.requireNonNull(Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("AUTH_TOKEN")).findFirst().orElse(null)).getValue();
//    }
}

