package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import br.com.fiap.petcare360_java.model.RecommendationStatusEnum;

public record RecommendationResponse(
		Long id,
		String petName,
		String tutorName,
		String veterinarianName,
		String title,
		String instructions,
		RecommendationPriorityEnum priority,
		RecommendationStatusEnum status,
		OffsetDateTime createdAt) {
}
