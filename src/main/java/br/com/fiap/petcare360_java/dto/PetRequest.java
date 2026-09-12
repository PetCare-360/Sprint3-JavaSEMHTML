package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

public record PetRequest(
		@NotBlank(message = "O nome do pet é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		String name,

		@NotNull(message = "A idade é obrigatória")
		@Min(value = 0, message = "A idade não pode ser negativa")
		@Max(value = 40, message = "A idade informada está fora do esperado")
		Integer age,

		@NotNull(message = "O peso é obrigatório")
		@DecimalMin(value = "0.10", message = "O peso deve ser maior que zero")
		@DecimalMax(value = "200.00", message = "O peso informado está fora do esperado")
		BigDecimal weight,

		@NotBlank(message = "A raça é obrigatória")
		@Size(max = 120, message = "A raça deve ter no máximo 120 caracteres")
		String breed,

		@NotBlank(message = "A espécie é obrigatória")
		@Size(max = 40, message = "A espécie deve ter no máximo 40 caracteres")
		String species,

		@NotBlank(message = "O identificador da coleira é obrigatório")
		@Size(max = 80, message = "O identificador do device deve ter no máximo 80 caracteres")
		String deviceId,

		@Valid
		@NotNull(message = "A primeira leitura da coleira é obrigatória")
		InitialSensorDataRequest initialSensorData) {
}
