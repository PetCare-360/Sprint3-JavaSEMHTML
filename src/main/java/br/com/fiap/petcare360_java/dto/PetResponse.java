package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record PetResponse(
		Long id,
		String name,
		Integer age,
		BigDecimal weight,
		String breed,
		String species,
		String deviceId,
		MonitoringStatusEnum currentStatus,
		DeviceResponse device,
		OffsetDateTime createdAt) {
}
