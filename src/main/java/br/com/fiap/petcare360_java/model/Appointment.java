package br.com.fiap.petcare360_java.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "SJ_APPOINTMENTS")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PET_ID")
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TUTOR_ID")
	private AppUser tutor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "VETERINARIAN_ID")
	private AppUser veterinarian;

	@Column(name = "SCHEDULED_AT", nullable = false)
	private OffsetDateTime scheduledAt;

	@Column(nullable = false, length = 255)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AppointmentStatusEnum status = AppointmentStatusEnum.REQUESTED;

	@Column(name = "CREATED_AT", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public AppUser getTutor() {
		return tutor;
	}

	public void setTutor(AppUser tutor) {
		this.tutor = tutor;
	}

	public AppUser getVeterinarian() {
		return veterinarian;
	}

	public void setVeterinarian(AppUser veterinarian) {
		this.veterinarian = veterinarian;
	}

	public OffsetDateTime getScheduledAt() {
		return scheduledAt;
	}

	public void setScheduledAt(OffsetDateTime scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public AppointmentStatusEnum getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatusEnum status) {
		this.status = status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
