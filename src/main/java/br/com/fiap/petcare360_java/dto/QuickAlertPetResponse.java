package br.com.fiap.petcare360_java.dto;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record QuickAlertPetResponse(
		Long petId,
		String name,
		String deviceId,
		MonitoringStatusEnum currentStatus,
		String reason,
		SensorDataResponse latestData) {
}
