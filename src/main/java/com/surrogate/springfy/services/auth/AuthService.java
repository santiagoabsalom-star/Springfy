package com.surrogate.springfy.services.auth;


import com.surrogate.springfy.models.bussines.Usuario;
import com.surrogate.springfy.models.login.LoginRequest;
import com.surrogate.springfy.models.login.LoginResponse;
import com.surrogate.springfy.models.register.RegisterRequest;
import com.surrogate.springfy.models.register.RegisterResponse;
import com.surrogate.springfy.repositories.bussines.UsuarioRepository;
import com.surrogate.springfy.services.auth.JWT.JWTService;
import com.surrogate.springfy.utils.UserDetailsWithId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepository;

    private final String success;
    private final String error;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {



        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
                return new LoginResponse(error, 422, "El nombre de usuario es requerido");
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return new LoginResponse(error, 422, "La constrasenia es requerida");
            }

            if (!isValidLoginRequest(loginRequest)) {
                return new LoginResponse(error, 422, "Parámetros inválidos");
            }
            if (!usuarioRepository.existsByNombre(loginRequest.getUsername())) {
                return new LoginResponse(error, 404, "Usuario no encontrado");
            }


            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );


            if (authentication.isAuthenticated()) {

                UserDetailsWithId userDetails = (UserDetailsWithId) authentication.getPrincipal();


                assert userDetails != null;
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_USER");


                String token = jwtService.generateTokenWithId(userDetails.getUsername(), role, userDetails.getId(), userDetails.getEmail());


                return new LoginResponse(success, 200, token, userDetails.getUsername(), userDetails.getId());
            }

            return new LoginResponse(error, 401, "Autenticación fallida");
        } catch (BadCredentialsException e) {
            return new LoginResponse(error, 403, "Contrasenia incorrecta");
        } catch (Exception e) {
            return new LoginResponse(error, 500, "Error en el servidor: " + e.getMessage());
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        try {
            if (isValidRegisterRequest(registerRequest)) {
                return new RegisterResponse(error, 401, "La constrasenia tiene que tener 8 digitos o mas");
            }
            if (registerRequest.getRol() == null || registerRequest.getRol().isEmpty()) {
                return new RegisterResponse(error, 401, "El rol es requerido");
            }
            if (registerRequest.getRol().equals("ROLE_ADMINISTRADOR")) {
                return new RegisterResponse(error, 403, "El rol no puede ser administrador");
            }
            if (usuarioRepository.existsByNombre(registerRequest.getUsername())) {

                return new RegisterResponse(error, 409, "El nombre ya existe");

            }
            if(registerRequest.getUsername().contains("_")){
                return new RegisterResponse(error, 409, "El nombre no puede tener barra baja");
            }

            Usuario newUsuario = new Usuario();
            newUsuario.setNombre(registerRequest.getUsername());
            newUsuario.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            newUsuario.setEmail(registerRequest.getEmail());
            newUsuario.setRol(registerRequest.getRol() != null ? registerRequest.getRol() : "USER");
            newUsuario.setImagenUrl(registerRequest.getImagen_url() != null ? registerRequest.getImagen_url() : null);
            newUsuario.setBiografia(registerRequest.getBiografia()!= null ? registerRequest.getBiografia() : null);
            newUsuario.setUuid(UUID.randomUUID().toString());
        usuarioRepository.save(newUsuario);

            return new RegisterResponse(success, 200,"Registro exitoso");
        } catch (Exception e) {
            return new RegisterResponse(error, 200, "Error en el registro: " + e.getMessage());
        }
    }

    @Transactional
    public RegisterResponse registerAdmin(RegisterRequest registerRequest) {
        try {
            log.info(registerRequest.getUsername());
            if (isValidRegisterRequest(registerRequest)) {
                return new RegisterResponse(error, 401, "La constrasenia tiene que tener 8 digitos o mas");
            }
            if (registerRequest.getRol() == null || registerRequest.getRol().isEmpty()) {
                return new RegisterResponse(error, 401, "El rol es requerido");
            }
            if (!registerRequest.getRol().equals("ROLE_ADMINISTRADOR") ) {
                return new RegisterResponse(error, 403, "El rol tiene que ser administrador o system");
            }
            if(registerRequest.getUsername().contains("_")){
                return new RegisterResponse(error, 409, "El nombre no puede tener barra baja");
            }
            if (usuarioRepository.existsByNombre(registerRequest.getUsername())) {

                return new RegisterResponse(error, 409, "El nombre ya existe");

            }


            Usuario newUsuario = new Usuario();
            newUsuario.setNombre(registerRequest.getUsername());
            newUsuario.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

            newUsuario.setRol(registerRequest.getRol() != null ? registerRequest.getRol() : "USER");
            newUsuario.setImagenUrl(registerRequest.getImagen_url() != null ? registerRequest.getImagen_url() : null);
            newUsuario.setBiografia(registerRequest.getBiografia()!= null ? registerRequest.getBiografia() : null);
            newUsuario.setEmail(registerRequest.getEmail());

            newUsuario.setUuid(UUID.randomUUID().toString());
            usuarioRepository.save(newUsuario);
            return new RegisterResponse(success, 200, "Registro exitoso");
        } catch (Exception e) {
            return new RegisterResponse(error, 500, "Error en el registro: " + e.getMessage());
        }
    }


    public String logout(String token) {

        log.info(token);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token no puede ser nulo o vacío");
        }
        String response = jwtService.InvalidateToken(token);
        if (response.equals(success)) {
            return success;
        }

        return error;
    }

  

    private boolean isValidLoginRequest(LoginRequest request) {
        return request != null &&
                StringUtils.hasText(request.getUsername()) &&
                StringUtils.hasText(request.getPassword());
    }


    private boolean isValidRegisterRequest(RegisterRequest request) {
//        if (request == null) return true;
//        String username = request.getUsername();
//        String password = request.getPassword();
//        if (!StringUtils.hasText(username)) return false;
//        return StringUtils.hasText(password) && password.length() >= MIN_PASSWORD_LENGTH;
        return false;
    }


    public void createSystemAdminIfNotExists(){


        if(usuarioRepository.existsByNombre("SYSTEM")){
            log.info("Usuario SYSTEM ya existe");
        return ;

        }
        Usuario systemAdmin = new Usuario();
        systemAdmin.setNombre("SYSTEM");
        String systempassword = "HolaMundo12345$";
        systemAdmin.setPasswordHash(passwordEncoder.encode(systempassword));
        systemAdmin.setRol("ROLE_SYSTEM");

        String uuid= UUID.randomUUID().toString();
        log.info("UUID PARA SYSTEM: {}",uuid);
        systemAdmin.setUuid(uuid);

        log.info(uuid);
        systemAdmin.setEmail("santiagoabsalom@gmail.com");
        usuarioRepository.save(systemAdmin);
        log.info("Usuario SYSTEM creado");


    }
}


