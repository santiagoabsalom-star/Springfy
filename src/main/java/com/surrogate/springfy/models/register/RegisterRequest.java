package com.surrogate.springfy.models.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.surrogate.springfy.models.peticiones.Request;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class RegisterRequest extends Request {

    private String rol;
    private String username;
    private String imagen_url;
    private String biografia;
    private String password;
    private String email;
    @JsonCreator
    public RegisterRequest(@JsonProperty("username") String username,
                           @JsonProperty("password") String password,
                           @JsonProperty("rol") String rol,
                           @JsonProperty("imagen_url") String imagen_url,
                           @JsonProperty("biografia") String biografia,
                           @JsonProperty("email") String email
    ) {
        this.email = email;
        this.imagen_url=imagen_url;
        this.biografia=biografia;
        this.username = username;
        this.password = password;
        this.rol = rol;

    }


}
