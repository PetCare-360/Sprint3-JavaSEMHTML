package br.com.fiap.petcare360_java.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.ActivitySummaryResponse;
import br.com.fiap.petcare360_java.dto.InitialSensorDataRequest;
import br.com.fiap.petcare360_java.dto.PetHealthStatusResponse;
import br.com.fiap.petcare360_java.dto.PetPageResponse;
import br.com.fiap.petcare360_java.dto.PetRequest;
import br.com.fiap.petcare360_java.dto.PetResponse;
import br.com.fiap.petcare360_java.dto.QuickAlertPetResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.AlertTypeEnum;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.DeviceStatusEnum;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.DeviceRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;

@Service
public class PetService {

	private static final java.math.BigDecimal TEMPERATURE_ALERT = new java.math.BigDecimal("39.0");
	private static final java.math.BigDecimal TEMPERATURE_CRITICAL = new java.math.BigDecimal("41.0");

	private final PetRepository petRepository;
	private final DeviceRepository deviceRepository;
	private final SensorDataRepository sensorDataRepository;
	private final AlertRepository alertRepository;
	private final PetVeterinarianRepository petVeterinarianRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetMapper mapper;

	public PetService(PetRepository petRepository, DeviceRepository deviceRepository,
			SensorDataRepository sensorDataRepository, AlertRepository alertRepository,
			PetVeterinarianRepository petVeterinarianRepository, CurrentUserService currentUserService,
			ClinicalAccessService clinicalAccessService, PetMapper mapper) {
		this.petRepository = petRepository;
		this.deviceRepository = deviceRepository;
		this.sensorDataRepository = sensorDataRepository;
		this.alertRepository = alertRepository;
		this.petVeterinarianRepository = petVeterinarianRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.mapper = mapper;
	}

	@Cacheable(value = "pets", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
	@Transactional(readOnly = true)
	public PetPageResponse list(Pageable pageable) {
		Page<PetResponse> pets = petRepository.findByUserEmail(currentUserService.email(), pageable)
				.map(this::toResponseWithCurrentStatus);

		return new PetPageResponse(
				pets.getContent(),
				pets.getNumber(),
				pets.getSize(),
				pets.getTotalElements(),
				pets.getTotalPages());
	}

	@Cacheable(value = "pets", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':all'")
	@Transactional(readOnly = true)
	public List<PetResponse> listAll() {
		return petRepository.findByUserEmailAndActiveTrue(currentUserService.email()).stream()
				.map(this::toResponseWithCurrentStatus)
				.toList();
	}

	@Cacheable(value = "pet", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #id")
	@Transactional(readOnly = true)
	public PetResponse find(Long id) {
		return toResponseWithCurrentStatus(findAccessiblePet(id));
	}

	@Cacheable(value = "pets", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':patients'")
	@Transactional(readOnly = true)
	public List<PetResponse> listByVeterinarian() {
		if (!currentUserService.hasRole("ROLE_VETERINARIO") && !currentUserService.hasRole("ROLE_ADMIN")) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas veterinário ou administrador pode listar pacientes");
		}

		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return petRepository.findAll().stream()
					.filter(pet -> Boolean.TRUE.equals(pet.getActive()))
					.map(this::toResponseWithCurrentStatus)
					.toList();
		}

		return petVeterinarianRepository.findActiveLinksByVeterinarianEmail(currentUserService.email()).stream()
				.map(link -> link.getPet())
				.map(this::toResponseWithCurrentStatus)
				.toList();
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public PetResponse create(PetRequest request) {
		AppUser user = currentUserService.user();
		String deviceId = request.deviceId().trim();

		if (deviceRepository.existsByDeviceCode(deviceId)) {
			throw new ApiException(HttpStatus.CONFLICT, "Este device já está vinculado a outro pet");
		}

		Pet pet = new Pet();
		pet.setUser(user);
		pet.update(request.name().trim(), request.age(), request.weight(), request.breed().trim(), request.species().trim());

		Device device = createDevice(deviceId, pet, request.initialSensorData());
		pet.setDevice(device);
		Pet savedPet = petRepository.save(pet);

		saveInitialSensorData(device, savedPet, request.initialSensorData());

		return toResponseWithCurrentStatus(savedPet);
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public PetResponse update(Long id, PetRequest request) {
		Pet pet = findEditablePet(id);
		String deviceId = request.deviceId().trim();
		pet.update(request.name().trim(), request.age(), request.weight(), request.breed().trim(), request.species().trim());

		Device currentDevice = pet.getDevice();
		if (currentDevice == null) {
			if (deviceRepository.existsByDeviceCode(deviceId)) {
				throw new ApiException(HttpStatus.CONFLICT, "Este device já está vinculado a outro pet");
			}
			currentDevice = createDevice(deviceId, pet, request.initialSensorData());
			pet.setDevice(currentDevice);
		} else if (!currentDevice.getDeviceCode().equals(deviceId)) {
			if (deviceRepository.existsByDeviceCode(deviceId)) {
				throw new ApiException(HttpStatus.CONFLICT, "Este device já está vinculado a outro pet");
			}
			currentDevice.setDeviceCode(deviceId);
			currentDevice.setStatus(DeviceStatusEnum.ACTIVE);
		}

		Pet savedPet = petRepository.save(pet);
		saveInitialSensorData(currentDevice, savedPet, request.initialSensorData());

		return toResponseWithCurrentStatus(savedPet);
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public PetResponse updateBasic(Long id, String name, Integer age, BigDecimal weight, String breed, String species,
			String deviceCode) {
		Pet pet = findEditablePet(id);
		String cleanDeviceCode = deviceCode.trim();
		pet.update(name.trim(), age, weight, breed.trim(), species.trim());

		Device device = pet.getDevice();
		if (device != null && !device.getDeviceCode().equals(cleanDeviceCode)) {
			if (deviceRepository.existsByDeviceCode(cleanDeviceCode)) {
				throw new ApiException(HttpStatus.CONFLICT, "Este device já está vinculado a outro pet");
			}
			device.setDeviceCode(cleanDeviceCode);
			device.setStatus(DeviceStatusEnum.ACTIVE);
		}

		return toResponseWithCurrentStatus(petRepository.save(pet));
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public void deactivate(Long id) {
		Pet pet = findEditablePet(id);
		pet.setActive(false);
		if (pet.getDevice() != null) {
			pet.getDevice().setStatus(DeviceStatusEnum.INACTIVE);
		}
		petRepository.save(pet);
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public void delete(Long id) {
		Pet pet = findOwnedPet(id);
		petRepository.delete(pet);
	}

	@Cacheable(value = "petHealth", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #id")
	@Transactional(readOnly = true)
	public PetHealthStatusResponse healthStatus(Long id) {
		Pet pet = findAccessiblePet(id);
		SensorData latest = latestData(pet);
		MonitoringStatusEnum status = currentStatus(latest);

		return new PetHealthStatusResponse(
				pet.getId(),
				pet.getName(),
				pet.getDevice() == null ? null : pet.getDevice().getDeviceCode(),
				status,
				statusMessage(status),
				latest == null ? null : latest.getTimestamp(),
				mapper.toSensorDataResponse(latest));
	}

	@Cacheable(value = "petQuickAlerts", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
	@Transactional(readOnly = true)
	public List<QuickAlertPetResponse> quickAlerts() {
		List<Pet> pets;
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			pets = petRepository.findAll();
		} else if (currentUserService.hasRole("ROLE_VETERINARIO")) {
			pets = petVeterinarianRepository.findActiveLinksByVeterinarianEmail(currentUserService.email()).stream()
					.map(link -> link.getPet())
					.toList();
		} else {
			pets = petRepository.findByUserEmail(currentUserService.email());
		}

		return pets.stream()
				.map(this::toQuickAlert)
				.filter(alert -> !MonitoringStatusEnum.NORMAL.equals(alert.currentStatus()))
				.toList();
	}

	@Cacheable(value = "petActivitySummary", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #id")
	@Transactional(readOnly = true)
	public ActivitySummaryResponse activitySummary(Long id) {
		Pet pet = findAccessiblePet(id);
		OffsetDateTime periodEnd = OffsetDateTime.now();
		OffsetDateTime periodStart = periodEnd.minusHours(24);
		List<SensorData> readings = sensorDataRepository.findByDevicePetIdAndTimestampAfter(pet.getId(), periodStart);

		return new ActivitySummaryResponse(
				pet.getId(),
				pet.getName(),
				periodStart,
				periodEnd,
				readings.size(),
				averageTemperature(readings),
				averageHeartRate(readings),
				averageActivity(readings));
	}

	@Transactional(readOnly = true)
	public Pet findOwnedPet(Long id) {
		return petRepository.findByIdAndUserEmail(id, currentUserService.email())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
	}

	@Transactional(readOnly = true)
	public Pet findAccessiblePet(Long id) {
		return clinicalAccessService.accessiblePet(id);
	}

	@Transactional(readOnly = true)
	public Pet findEditablePet(Long id) {
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return petRepository.findById(id)
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
		}

		if (currentUserService.hasRole("ROLE_VETERINARIO")
				&& petVeterinarianRepository.existsActiveLink(id, currentUserService.email())) {
			return petRepository.findById(id)
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
		}

		return findOwnedPet(id);
	}

	private Device createDevice(String rawDeviceId, Pet pet, InitialSensorDataRequest sensorData) {
		String deviceId = rawDeviceId.trim();
		Device device = new Device();
		device.setDeviceCode(deviceId);
		device.setPet(pet);
		device.setStatus(DeviceStatusEnum.ACTIVE);
		device.updateTelemetry(sensorData.battery(), sensorData.timestamp());
		return device;
	}

	private void saveInitialSensorData(Device device, Pet pet, InitialSensorDataRequest request) {
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
		createAlerts(pet, request);
	}

	private MonitoringStatusEnum calculateStatus(InitialSensorDataRequest request) {
		boolean critical = request.temperature().compareTo(TEMPERATURE_CRITICAL) >= 0
				|| request.heartRate() >= 160;

		if (critical) {
			return MonitoringStatusEnum.CRITICAL;
		}

		boolean alert = request.temperature().compareTo(TEMPERATURE_ALERT) > 0
				|| request.heartRate() > 130
				|| request.activityLevel() < 15
				|| (request.battery() != null && request.battery() < 20);

		return alert ? MonitoringStatusEnum.WARNING : MonitoringStatusEnum.NORMAL;
	}

	private void createAlerts(Pet pet, InitialSensorDataRequest request) {
		java.util.List<Alert> alerts = new java.util.ArrayList<>();

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

		if (request.battery() != null && request.battery() < 20) {
			alerts.add(alert(pet, AlertTypeEnum.BATTERY, AlertLevelEnum.WARNING,
					"Bateria baixa da coleira: " + request.battery() + "%"));
		}

		alertRepository.saveAll(alerts);
	}

	private Alert alert(Pet pet, AlertTypeEnum type, AlertLevelEnum level, String message) {
		Alert alert = new Alert();
		alert.setPet(pet);
		alert.setType(type);
		alert.setLevel(level);
		alert.setMessage(message);
		return alert;
	}

	private PetResponse toResponseWithCurrentStatus(Pet pet) {
		return mapper.toPetResponse(pet, currentStatus(latestData(pet)));
	}

	private QuickAlertPetResponse toQuickAlert(Pet pet) {
		SensorData latest = latestData(pet);
		MonitoringStatusEnum status = currentStatus(latest);
		SensorDataResponse latestResponse = mapper.toSensorDataResponse(latest);

		return new QuickAlertPetResponse(
				pet.getId(),
				pet.getName(),
				pet.getDevice() == null ? null : pet.getDevice().getDeviceCode(),
				status,
				statusMessage(status),
				latestResponse);
	}

	private SensorData latestData(Pet pet) {
		return sensorDataRepository.findFirstByDevicePetIdOrderByTimestampDesc(pet.getId()).orElse(null);
	}

	private MonitoringStatusEnum currentStatus(SensorData latest) {
		return latest == null ? MonitoringStatusEnum.NORMAL : latest.getStatus();
	}

	private String statusMessage(MonitoringStatusEnum status) {
		if (MonitoringStatusEnum.CRITICAL.equals(status)) {
			return "Sinais vitais em estado crítico";
		}
		if (MonitoringStatusEnum.WARNING.equals(status)) {
			return "Sinais vitais fora do padrão normal";
		}
		return "Tudo bem com o pet";
	}

	private BigDecimal averageTemperature(List<SensorData> readings) {
		return average(readings.stream().map(SensorData::getTemperature).toList());
	}

	private BigDecimal averageHeartRate(List<SensorData> readings) {
		return average(readings.stream()
				.map(reading -> BigDecimal.valueOf(reading.getHeartRate()))
				.toList());
	}

	private BigDecimal averageActivity(List<SensorData> readings) {
		return average(readings.stream()
				.map(reading -> BigDecimal.valueOf(reading.getActivityLevel()))
				.toList());
	}

	private BigDecimal average(List<BigDecimal> values) {
		if (values.isEmpty()) {
			return BigDecimal.ZERO.setScale(2);
		}

		BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
	}
}
