package dev.slethware.qurag;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
		info = @Info(
				contact = @Contact(
						name = "Slethware",
						email = "dev@slethware.com"
				),
				description = "AI-Powered Document Search & RAG Query Service with Vector Database",
				title = "Qurag API Documentation",
				version = "1.0"
		)
)
@SpringBootApplication
@EnableJpaAuditing
public class QuragApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuragApplication.class, args);
	}

}