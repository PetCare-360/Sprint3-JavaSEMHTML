package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.DeviceStatusEnum;

public record DeviceResponse(String deviceId, DeviceStatusEnum status, Integer battery, OffsetDateTime lastSeen) {
}
