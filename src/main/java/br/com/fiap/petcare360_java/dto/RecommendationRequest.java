package br.com.fiap.petcare360_java.dto;

import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecommendationRequest(
		@NotNull(message = "O pet é obrigatório")
		Long petId,

		@NotBlank(message = "O título é obrigatório")
		@Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
		String title,

		@NotBlank(message = "As orientações são obrigatórias")
		@Size(max = 1000, message = "As orientações devem ter no máximo 1000 caracteres")
		String instructions,

		@NotNull(message = "A prioridade é obrigatória")
		RecommendationPriorityEnum priority) {
}
