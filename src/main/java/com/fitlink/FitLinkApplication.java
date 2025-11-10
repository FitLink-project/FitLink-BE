package com.fitlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FitLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitLinkApplication.class, args);
	}

}

