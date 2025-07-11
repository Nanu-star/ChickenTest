package com.chickentest;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.chickentest.domain")
@EnableJpaRepositories(basePackages = "com.chickentest.repository")
@EnableScheduling
public class ChickenTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChickenTestApplication.class, args);
    }
}
