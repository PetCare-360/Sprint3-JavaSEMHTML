package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.MessageStatusEnum;

public record MessageResponse(
		Long id,
		Long petId,
		String petName,
		String senderName,
		String receiverName,
		String subject,
		String message,
		MessageStatusEnum status,
		OffsetDateTime createdAt) {
}
