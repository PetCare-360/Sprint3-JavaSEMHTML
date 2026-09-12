package br.com.fiap.petcare360_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public OpenAPI petCareOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("PetCare360 API")
						.description("API para pets, coleiras inteligentes, telemetria IoT, alertas e atendimento veterinário")
						.version("1.0.0"));
	}
}
