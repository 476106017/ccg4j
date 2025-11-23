package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
@MapperScan({ "org.example.user.mapper", "org.example.system.mapper", "org.example.card.data.mapper",
        "org.example.community.mapper", "org.example.workshop.mapper" })
public class Ccg4jApplication {
    public static void main(String[] args) {
        SpringApplication.run(Ccg4jApplication.class, args);
    }
}
