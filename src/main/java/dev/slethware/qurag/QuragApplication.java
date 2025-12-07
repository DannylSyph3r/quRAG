package dev.slethware.qurag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QuragApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuragApplication.class, args);
	}

}