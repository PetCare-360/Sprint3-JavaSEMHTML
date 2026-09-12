package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record SensorDataResponse(
		Long id,
		OffsetDateTime timestamp,
		BigDecimal temperature,
		Integer heartRate,
		Integer activityLevel,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer battery,
		MonitoringStatusEnum status) {
}
