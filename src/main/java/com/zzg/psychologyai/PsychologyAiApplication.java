package com.zzg.psychologyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PsychologyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsychologyAiApplication.class, args);
    }
}
