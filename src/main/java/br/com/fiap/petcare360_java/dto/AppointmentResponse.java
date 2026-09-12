package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.AppointmentStatusEnum;

public record AppointmentResponse(
		Long id,
		String petName,
		String tutorName,
		String veterinarianName,
		OffsetDateTime scheduledAt,
		String reason,
		AppointmentStatusEnum status) {
}
