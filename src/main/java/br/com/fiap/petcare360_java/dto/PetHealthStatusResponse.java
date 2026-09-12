package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record PetHealthStatusResponse(
		Long petId,
		String name,
		String deviceId,
		MonitoringStatusEnum currentStatus,
		String message,
		OffsetDateTime lastSeen,
		SensorDataResponse latestData) {
}
