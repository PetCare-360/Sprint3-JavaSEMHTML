package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppointmentRequest(
		@NotNull(message = "O pet é obrigatório")
		Long petId,

		@NotNull(message = "O veterinário é obrigatório")
		Long veterinarianId,

		@NotNull(message = "A data da consulta é obrigatória")
		@Future(message = "A consulta deve ser marcada para uma data futura")
		OffsetDateTime scheduledAt,

		@NotBlank(message = "O motivo é obrigatório")
		@Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
		String reason) {
}
