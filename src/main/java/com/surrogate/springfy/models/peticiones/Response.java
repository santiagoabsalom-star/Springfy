package com.surrogate.springfy.models.peticiones;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer httpCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String username;

    public Response(String status, String token, String username, Long id) {
        this.username = username;
        this.status = status;
        this.token = token;
        this.id = id;
    }

    public Response() {
    }

    public Response(String status) {
        this.status = status;
    }

    public Response(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }


    public Response(String status, Integer httpCode, String message) {
        this.status = status;
        this.httpCode = httpCode;
        this.message = message;


    }

    public Response(String status, int httpCode, String token, String username, long id) {
        this.status = status;
        this.httpCode = httpCode;
        this.token = token;
        this.username = username;
        this.id = id;
    }
}
