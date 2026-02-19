package com.surrogate.springfy.routes.bussiness;

import com.surrogate.springfy.models.DTO.DuoRequest;
import com.surrogate.springfy.models.peticiones.Response;
import com.surrogate.springfy.services.bussines.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/streaming")
public class StreamingController {
    private final StreamingService streamingService;
        @GetMapping("/get-duo")
    public ResponseEntity<String> getDuoByUsername(HttpServletRequest request){
            String token = getTokenFromRequest(request);
            String username= streamingService.getDuoByUsername(token);
            if(Objects.isNull(username)){
                return ResponseEntity.notFound().build();

            }

            return ResponseEntity.ok().body(username);


        }

        @GetMapping("/get-all-usernames")
        public ResponseEntity<List<String>> getAllUsernames(HttpServletRequest request){
            List<String> usernames= streamingService.getAllNombres(getTokenFromRequest(request));
            if(Objects.isNull(usernames)){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(usernames);
        }

        @PostMapping("/create-duo")
        public ResponseEntity<Response> createDuo(@RequestBody DuoRequest duoRequest){
            Response response= streamingService.createDuo(duoRequest);

            return ResponseEntity.status(response.getHttpCode()).body(response);


        }


    private String getTokenFromRequest(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null ) {

            return null;
        }
        log.info(request.getHeader("Authorization"));

        return request.getHeader("Authorization").substring(7);
    }


}
