package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
		@NotNull(message = "O pet é obrigatório")
		Long petId,

		@NotNull(message = "O destinatário é obrigatório")
		Long receiverId,

		@NotBlank(message = "O assunto é obrigatório")
		@Size(max = 120, message = "O assunto deve ter no máximo 120 caracteres")
		String subject,

		@NotBlank(message = "A mensagem é obrigatória")
		@Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres")
		String message) {
}
