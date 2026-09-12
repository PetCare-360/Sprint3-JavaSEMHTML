package br.com.fiap.petcare360_java.service;

import org.springframework.stereotype.Component;

import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.DeviceResponse;
import br.com.fiap.petcare360_java.dto.PetResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.dto.UserResponse;
import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.Role;
import br.com.fiap.petcare360_java.model.SensorData;

@Component
public class PetMapper {

	public UserResponse toUserResponse(AppUser user) {
		String role = user.getRoles().stream()
				.map(Role::getName)
				.findFirst()
				.orElse("ROLE_CLIENTE");
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), role);
	}

	public PetResponse toPetResponse(Pet pet) {
		return toPetResponse(pet, null);
	}

	public PetResponse toPetResponse(Pet pet, MonitoringStatusEnum currentStatus) {
		Device device = pet.getDevice();
		DeviceResponse deviceResponse = null;
		if (device != null) {
			deviceResponse = new DeviceResponse(
					device.getDeviceId(),
					device.getStatus(),
					device.getBattery(),
					device.getLastSeen());
		}

		return new PetResponse(
				pet.getId(),
				pet.getName(),
				pet.getAge(),
				pet.getWeight(),
				pet.getBreed(),
				pet.getSpecies(),
				device == null ? null : device.getDeviceCode(),
				currentStatus,
				deviceResponse,
				pet.getCreatedAt());
	}

	public SensorDataResponse toSensorDataResponse(SensorData data) {
		if (data == null) {
			return null;
		}

		return new SensorDataResponse(
				data.getId(),
				data.getTimestamp(),
				data.getTemperature(),
				data.getHeartRate(),
				data.getActivityLevel(),
				data.getLatitude(),
				data.getLongitude(),
				data.getBattery(),
				data.getStatus());
	}

	public AlertResponse toAlertResponse(Alert alert) {
		return new AlertResponse(
				alert.getId(),
				alert.getType(),
				alert.getMessage(),
				alert.getLevel(),
				alert.getCreatedAt());
	}
}
