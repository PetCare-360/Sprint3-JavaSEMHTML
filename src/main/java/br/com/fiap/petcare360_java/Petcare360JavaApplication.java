package br.com.fiap.petcare360_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Petcare360JavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(Petcare360JavaApplication.class, args);
	}

}
