package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		String name,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "Informe um e-mail válido")
		@Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 6, max = 80, message = "A senha deve ter entre 6 e 80 caracteres")
		String password,

		@Size(max = 40, message = "O perfil deve ter no máximo 40 caracteres")
		String role) {
}
