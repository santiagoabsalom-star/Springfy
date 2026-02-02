package com.surrogate.springfy.models.register;


import com.surrogate.springfy.models.peticiones.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)

public class RegisterResponse extends Response {
    public RegisterResponse(String message, String status) {
        super(message, status);

    }

    public RegisterResponse(String status, Integer httpError, String message) {
        super(status, httpError, message);
    }

    public RegisterResponse() {

    }
}

