package br.com.fiap.petcare360_java.dto;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record PetSummaryResponse(
		PetResponse pet,
		SensorDataResponse latestData,
		MonitoringStatusEnum currentStatus,
		long totalReadings,
		long totalAlerts) {
}
