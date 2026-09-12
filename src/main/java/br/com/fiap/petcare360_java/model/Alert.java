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
@Table(name = "SJ_ALERTS")
public class Alert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PET_ID", nullable = false)
	private Pet pet;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AlertTypeEnum type;

	@Column(nullable = false, length = 255)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(name = "ALERT_LEVEL", nullable = false, length = 20)
	private AlertLevelEnum level;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AlertStatusEnum status = AlertStatusEnum.OPEN;

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

	public AlertTypeEnum getType() {
		return type;
	}

	public void setType(AlertTypeEnum type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AlertLevelEnum getLevel() {
		return level;
	}

	public void setLevel(AlertLevelEnum level) {
		this.level = level;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public AlertStatusEnum getStatus() {
		return status;
	}

	public void setStatus(AlertStatusEnum status) {
		this.status = status;
	}
}
