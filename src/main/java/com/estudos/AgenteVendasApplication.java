package com.estudos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgenteVendasApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgenteVendasApplication.class, args);
    }
}