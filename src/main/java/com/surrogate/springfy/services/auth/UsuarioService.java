package com.surrogate.springfy.services.auth;

import com.surrogate.springfy.models.login.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
//todo: completar metodos de busqueda
















    private Long getIdFromSecurityContext(){

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userPrincipal.getId();
    }
    private String getEmailFromSecurityContext(){
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userPrincipal.getEmail();
    }




}
