package com.miru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class MiruBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiruBackendApplication.class, args);
	}

}
