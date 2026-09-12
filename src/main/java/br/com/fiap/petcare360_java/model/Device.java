package br.com.fiap.petcare360_java.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "SJ_DEVICES")
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "DEVICE_CODE", nullable = false, unique = true, length = 80)
	private String deviceCode;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PET_ID", nullable = false, unique = true)
	private Pet pet;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DeviceStatusEnum status = DeviceStatusEnum.ACTIVE;

	private Integer battery;

	@Column(name = "LAST_SEEN")
	private OffsetDateTime lastSeen;

	@OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SensorData> sensorData = new ArrayList<>();

	public void updateTelemetry(Integer battery, OffsetDateTime lastSeen) {
		this.battery = battery;
		this.lastSeen = lastSeen;
	}

	public boolean isActive() {
		return DeviceStatusEnum.ACTIVE.equals(status);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceCode;
	}

	public void setDeviceId(String deviceId) {
		this.deviceCode = deviceId;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public DeviceStatusEnum getStatus() {
		return status;
	}

	public void setStatus(DeviceStatusEnum status) {
		this.status = status;
	}

	public Integer getBattery() {
		return battery;
	}

	public void setBattery(Integer battery) {
		this.battery = battery;
	}

	public OffsetDateTime getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(OffsetDateTime lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<SensorData> getSensorData() {
		return sensorData;
	}

	public void setSensorData(List<SensorData> sensorData) {
		this.sensorData = sensorData;
	}
}
