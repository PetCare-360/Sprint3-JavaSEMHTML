package br.com.fiap.petcare360_java.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.IotDataRequest;
import br.com.fiap.petcare360_java.dto.IotDataResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.AlertTypeEnum;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.DeviceRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;

@Service
public class IotProcessingService {

	private static final BigDecimal TEMPERATURE_ALERT = new BigDecimal("39.0");
	private static final BigDecimal TEMPERATURE_CRITICAL = new BigDecimal("41.0");

	private final DeviceRepository deviceRepository;
	private final SensorDataRepository sensorDataRepository;
	private final AlertRepository alertRepository;
	private final PetMapper mapper;

	public IotProcessingService(DeviceRepository deviceRepository, SensorDataRepository sensorDataRepository,
			AlertRepository alertRepository, PetMapper mapper) {
		this.deviceRepository = deviceRepository;
		this.sensorDataRepository = sensorDataRepository;
		this.alertRepository = alertRepository;
		this.mapper = mapper;
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public IotDataResponse process(IotDataRequest request) {
		Device device = deviceRepository.findByDeviceCode(request.deviceId().trim())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Device não encontrado"));

		if (!device.isActive()) {
			throw new ApiException(HttpStatus.CONFLICT, "Device inativo para recebimento de telemetria");
		}

		Pet pet = device.getPet();
		if (pet == null) {
			throw new ApiException(HttpStatus.CONFLICT, "Device não está vinculado a um pet");
		}

		MonitoringStatusEnum status = calculateStatus(request);
		SensorData sensorData = new SensorData();
		sensorData.setDevice(device);
		sensorData.setTimestamp(request.timestamp());
		sensorData.setTemperature(request.temperature());
		sensorData.setHeartRate(request.heartRate());
		sensorData.setActivityLevel(request.activityLevel());
		sensorData.setLatitude(request.latitude());
		sensorData.setLongitude(request.longitude());
		sensorData.setBattery(request.battery());
		sensorData.setStatus(status);
		sensorDataRepository.save(sensorData);

		device.updateTelemetry(request.battery(), request.timestamp());
		deviceRepository.save(device);

		List<AlertResponse> alerts = createAlerts(pet, request).stream()
				.map(mapper::toAlertResponse)
				.toList();

		return new IotDataResponse(
				sensorData.getId(),
				device.getDeviceCode(),
				pet.getId(),
				status,
				OffsetDateTime.now(),
				alerts);
	}

	@Transactional(readOnly = true)
	public List<SensorDataResponse> listReadings() {
		return sensorDataRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream()
				.map(mapper::toSensorDataResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SensorDataResponse findReading(Long id) {
		return mapper.toSensorDataResponse(findSensorData(id));
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public SensorDataResponse updateReading(Long id, IotDataRequest request) {
		SensorData sensorData = findSensorData(id);
		Device device = deviceRepository.findByDeviceCode(request.deviceId().trim())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Device não encontrado"));

		sensorData.setDevice(device);
		sensorData.setTimestamp(request.timestamp());
		sensorData.setTemperature(request.temperature());
		sensorData.setHeartRate(request.heartRate());
		sensorData.setActivityLevel(request.activityLevel());
		sensorData.setLatitude(request.latitude());
		sensorData.setLongitude(request.longitude());
		sensorData.setBattery(request.battery());
		sensorData.setStatus(calculateStatus(request));

		device.updateTelemetry(request.battery(), request.timestamp());
		deviceRepository.save(device);

		return mapper.toSensorDataResponse(sensorDataRepository.save(sensorData));
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public void deleteReading(Long id) {
		sensorDataRepository.delete(findSensorData(id));
	}

	private SensorData findSensorData(Long id) {
		return sensorDataRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Leitura IoT não encontrada"));
	}

	private MonitoringStatusEnum calculateStatus(IotDataRequest request) {
		boolean critical = request.temperature().compareTo(TEMPERATURE_CRITICAL) >= 0
				|| request.heartRate() >= 160;

		if (critical) {
			return MonitoringStatusEnum.CRITICAL;
		}

		boolean alert = request.temperature().compareTo(TEMPERATURE_ALERT) > 0
				|| request.heartRate() > 130
				|| request.activityLevel() < 15
				|| request.battery() < 20;

		return alert ? MonitoringStatusEnum.WARNING : MonitoringStatusEnum.NORMAL;
	}

	private List<Alert> createAlerts(Pet pet, IotDataRequest request) {
		List<Alert> alerts = new ArrayList<>();

		if (request.temperature().compareTo(TEMPERATURE_CRITICAL) >= 0) {
			alerts.add(alert(pet, AlertTypeEnum.TEMPERATURE, AlertLevelEnum.CRITICAL,
					"Temperatura crítica detectada: " + request.temperature() + " °C"));
		} else if (request.temperature().compareTo(TEMPERATURE_ALERT) > 0) {
			alerts.add(alert(pet, AlertTypeEnum.TEMPERATURE, AlertLevelEnum.WARNING,
					"Temperatura acima do ideal: " + request.temperature() + " °C"));
		}

		if (request.heartRate() >= 160) {
			alerts.add(alert(pet, AlertTypeEnum.HEART_RATE, AlertLevelEnum.CRITICAL,
					"Batimentos cardíacos em nível crítico: " + request.heartRate() + " bpm"));
		} else if (request.heartRate() > 130) {
			alerts.add(alert(pet, AlertTypeEnum.HEART_RATE, AlertLevelEnum.WARNING,
					"Batimentos cardíacos elevados: " + request.heartRate() + " bpm"));
		}

		if (request.activityLevel() < 15) {
			alerts.add(alert(pet, AlertTypeEnum.ACTIVITY, AlertLevelEnum.WARNING,
					"Nível de atividade muito baixo: " + request.activityLevel() + "%"));
		}

		if (request.battery() < 20) {
			alerts.add(alert(pet, AlertTypeEnum.BATTERY, AlertLevelEnum.WARNING,
					"Bateria baixa da coleira: " + request.battery() + "%"));
		}

		return alertRepository.saveAll(alerts);
	}

	private Alert alert(Pet pet, AlertTypeEnum type, AlertLevelEnum level, String message) {
		Alert alert = new Alert();
		alert.setPet(pet);
		alert.setType(type);
		alert.setLevel(level);
		alert.setMessage(message);
		return alert;
	}
}
