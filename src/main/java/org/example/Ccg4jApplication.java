package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@MapperScan({"org.example.user.mapper", "org.example.system.mapper"})
public class Ccg4jApplication  {
    public static void main(String[] args) {
        SpringApplication.run(Ccg4jApplication.class, args);
    }
}
