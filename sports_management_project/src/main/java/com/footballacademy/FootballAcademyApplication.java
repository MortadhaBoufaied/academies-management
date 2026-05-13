package com.footballacademy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableScheduling
@EnableJms
public
class FootballAcademyApplication {
    public static void main(String[] args) {
        SpringApplication.run(FootballAcademyApplication.
        class, args);
    }
}
