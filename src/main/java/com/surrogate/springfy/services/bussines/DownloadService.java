package com.surrogate.springfy.services.bussines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.peticiones.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DownloadService {
private final ObjectMapper mapper;

public Response downloadOnCloud(String nombre) {
    //Todo: make this method:D
    return new Response();
}

public Response downloadOnApp(String nombre){
    //todo: make this another method shiitt:D
    return new Response();
}


}
