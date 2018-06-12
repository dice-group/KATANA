package org.aksw.katana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KatanaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KatanaApplication.class, args);
    }

}
