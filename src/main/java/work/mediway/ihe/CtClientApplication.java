package work.mediway.ihe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CtClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CtClientApplication.class, args);
    }

}
