package com.surrogate.springfy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class SpringfyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringfyApplication.class, args);

}
}
