package com.surrogate.springfy.models.login;


import com.surrogate.springfy.models.peticiones.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class LoginResponse extends Response {


    public LoginResponse(String status, Integer httpError, String response) {
        super(status, httpError, response);
    }


    public LoginResponse(String status, String message) {
        super(status, message);
    }

    public LoginResponse(String status, int httpCode, String token, String username, long id) {
        super(status, httpCode, token, username, id);
    }
}




