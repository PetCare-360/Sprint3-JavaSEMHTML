package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.NotNull;

public record VeterinarianLinkRequest(
		@NotNull(message = "O pet é obrigatório")
		Long petId,

		@NotNull(message = "O veterinário é obrigatório")
		Long veterinarianId) {
}
