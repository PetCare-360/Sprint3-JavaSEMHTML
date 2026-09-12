package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

public record VeterinarianLinkResponse(
		Long petId,
		String petName,
		Long veterinarianId,
		String veterinarianName,
		OffsetDateTime assignedAt,
		Boolean active) {
}
