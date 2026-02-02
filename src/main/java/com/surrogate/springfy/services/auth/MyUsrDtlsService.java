package com.surrogate.springfy.services.auth;


import com.surrogate.springfy.models.bussines.Usuario;
import com.surrogate.springfy.models.login.UserPrincipal;
import com.surrogate.springfy.repositories.bussines.UsuarioRepository;
import com.surrogate.springfy.utils.UserDetailsServiceWithId;
import com.surrogate.springfy.utils.UserDetailsWithId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUsrDtlsService implements UserDetailsServiceWithId {

    private final UsuarioRepository usuariorepository;

    public MyUsrDtlsService(UsuarioRepository usuariorepository) {
        this.usuariorepository = usuariorepository;
    }

    /*
     * Este metodo es el que se encarga de cargar el usuario desde la base de datos
     * y devolverlo a Spring Security
     * */
    @Override
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetailsWithId loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuariorepository.findUsuarioByNombre(username);


        // Si no se encuentra el usuario, lanzamos una fuckin excepción

        if (usuario == null) {
            throw new UsernameNotFoundException("Nombre no encontrado: " + username);
        }
        return new UserPrincipal(usuario);
    }
}
