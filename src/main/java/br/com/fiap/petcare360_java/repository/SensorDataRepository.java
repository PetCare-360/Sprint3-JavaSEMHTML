package br.com.fiap.petcare360_java.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.SensorData;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
	Optional<SensorData> findFirstByDevicePetIdOrderByTimestampDesc(Long petId);
	Page<SensorData> findByDevicePetId(Long petId, Pageable pageable);
	Page<SensorData> findByDevicePetIdAndStatus(Long petId, MonitoringStatusEnum status, Pageable pageable);
	List<SensorData> findByDevicePetIdAndTimestampAfter(Long petId, OffsetDateTime timestamp);
	List<SensorData> findTop50ByDevicePetIdOrderByTimestampDesc(Long petId);
	List<SensorData> findTop30ByDevicePetIdOrderByTimestampDesc(Long petId);
	long countByDevicePetId(Long petId);
	long countByDeviceDeviceCode(String deviceCode);
	void deleteByDeviceDeviceCode(String deviceCode);
}
