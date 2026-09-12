package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InitialSensorDataRequest(
		@NotNull(message = "O timestamp da primeira leitura é obrigatório")
		OffsetDateTime timestamp,

		@NotNull(message = "A temperatura é obrigatória")
		@DecimalMin(value = "30.0", message = "A temperatura deve estar entre 30 e 45 graus")
		@DecimalMax(value = "45.0", message = "A temperatura deve estar entre 30 e 45 graus")
		BigDecimal temperature,

		@NotNull(message = "Os batimentos são obrigatórios")
		@Min(value = 40, message = "Os batimentos devem estar entre 40 e 200 bpm")
		@Max(value = 200, message = "Os batimentos devem estar entre 40 e 200 bpm")
		Integer heartRate,

		@NotNull(message = "O nível de atividade é obrigatório")
		@Min(value = 0, message = "A atividade deve estar entre 0 e 100")
		@Max(value = 100, message = "A atividade deve estar entre 0 e 100")
		Integer activityLevel,

		@DecimalMin(value = "-90.0", message = "A latitude deve estar entre -90 e 90")
		@DecimalMax(value = "90.0", message = "A latitude deve estar entre -90 e 90")
		BigDecimal latitude,

		@DecimalMin(value = "-180.0", message = "A longitude deve estar entre -180 e 180")
		@DecimalMax(value = "180.0", message = "A longitude deve estar entre -180 e 180")
		BigDecimal longitude,

		@NotNull(message = "A bateria é obrigatória")
		@Min(value = 0, message = "A bateria deve estar entre 0 e 100")
		@Max(value = 100, message = "A bateria deve estar entre 0 e 100")
		Integer battery) {
}
