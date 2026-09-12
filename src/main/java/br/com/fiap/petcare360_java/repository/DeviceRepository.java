package br.com.fiap.petcare360_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
	Optional<Device> findByDeviceCode(String deviceCode);
	boolean existsByDeviceCode(String deviceCode);
}
