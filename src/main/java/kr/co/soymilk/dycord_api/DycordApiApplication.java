package kr.co.soymilk.dycord_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DycordApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DycordApiApplication.class, args);
    }

}
