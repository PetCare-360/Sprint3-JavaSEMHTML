package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ActivitySummaryResponse(
		Long petId,
		String name,
		OffsetDateTime periodStart,
		OffsetDateTime periodEnd,
		Integer readings,
		BigDecimal averageTemperature,
		BigDecimal averageHeartRate,
		BigDecimal averageActivityLevel) {
}
