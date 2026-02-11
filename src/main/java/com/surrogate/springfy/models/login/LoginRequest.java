package com.surrogate.springfy.models.login;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.surrogate.springfy.models.peticiones.Request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest extends Request {

    private String username;


    private String password;

    @JsonCreator
    public LoginRequest(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password
    ) {
        this.username = username;
        this.password = password;
    }


}


