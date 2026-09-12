package br.com.fiap.petcare360_java.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.PetSummaryResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;

@Service
public class MonitoringService {

	private final PetService petService;
	private final SensorDataRepository sensorDataRepository;
	private final AlertRepository alertRepository;
	private final PetMapper mapper;

	public MonitoringService(PetService petService, SensorDataRepository sensorDataRepository,
			AlertRepository alertRepository, PetMapper mapper) {
		this.petService = petService;
		this.sensorDataRepository = sensorDataRepository;
		this.alertRepository = alertRepository;
		this.mapper = mapper;
	}

	@Cacheable(value = "petSummary", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #petId")
	@Transactional(readOnly = true)
	public PetSummaryResponse summary(Long petId) {
		Pet pet = petService.findAccessiblePet(petId);
		SensorData latest = sensorDataRepository.findFirstByDevicePetIdOrderByTimestampDesc(pet.getId()).orElse(null);
		MonitoringStatusEnum currentStatus = latest == null ? MonitoringStatusEnum.NORMAL : latest.getStatus();

		return new PetSummaryResponse(
				mapper.toPetResponse(pet),
				mapper.toSensorDataResponse(latest),
				currentStatus,
				sensorDataRepository.countByDevicePetId(pet.getId()),
				alertRepository.countByPetId(pet.getId()));
	}

	@Cacheable(value = "petMonitoring", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #petId + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
	@Transactional(readOnly = true)
	public Page<SensorDataResponse> monitoring(Long petId, MonitoringStatusEnum status, Pageable pageable) {
		petService.findAccessiblePet(petId);
		if (status == null) {
			return sensorDataRepository.findByDevicePetId(petId, pageable)
					.map(mapper::toSensorDataResponse);
		}
		return sensorDataRepository.findByDevicePetIdAndStatus(petId, status, pageable)
				.map(mapper::toSensorDataResponse);
	}

	@Cacheable(value = "petActivity", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #petId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
	@Transactional(readOnly = true)
	public Page<SensorDataResponse> activity(Long petId, Pageable pageable) {
		petService.findAccessiblePet(petId);
		return sensorDataRepository.findByDevicePetId(petId, pageable)
				.map(mapper::toSensorDataResponse);
	}

	@Cacheable(value = "petLocation", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #petId")
	@Transactional(readOnly = true)
	public SensorDataResponse location(Long petId) {
		petService.findAccessiblePet(petId);
		return sensorDataRepository.findFirstByDevicePetIdOrderByTimestampDesc(petId)
				.map(mapper::toSensorDataResponse)
				.orElse(null);
	}

	@Cacheable(value = "petAlerts", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #petId + ':' + #level + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
	@Transactional(readOnly = true)
	public Page<AlertResponse> alerts(Long petId, AlertLevelEnum level, Pageable pageable) {
		petService.findAccessiblePet(petId);
		if (level == null) {
			return alertRepository.findByPetId(petId, pageable)
					.map(mapper::toAlertResponse);
		}
		return alertRepository.findByPetIdAndLevel(petId, level, pageable)
				.map(mapper::toAlertResponse);
	}

}
