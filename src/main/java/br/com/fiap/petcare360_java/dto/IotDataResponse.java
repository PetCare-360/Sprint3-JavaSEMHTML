package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record IotDataResponse(
		Long sensorDataId,
		String deviceId,
		Long petId,
		MonitoringStatusEnum status,
		OffsetDateTime processedAt,
		List<AlertResponse> alerts) {
}
