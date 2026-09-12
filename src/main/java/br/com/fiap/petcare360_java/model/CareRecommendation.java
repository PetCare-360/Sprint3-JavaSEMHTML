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
@Table(name = "SJ_CARE_RECOMMENDATIONS")
public class CareRecommendation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PET_ID")
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "VETERINARIAN_ID")
	private AppUser veterinarian;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TUTOR_ID")
	private AppUser tutor;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(nullable = false, length = 1000)
	private String instructions;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecommendationPriorityEnum priority = RecommendationPriorityEnum.NORMAL;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecommendationStatusEnum status = RecommendationStatusEnum.ACTIVE;

	@Column(name = "CREATED_AT", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "COMPLETED_AT")
	private OffsetDateTime completedAt;

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

	public AppUser getVeterinarian() {
		return veterinarian;
	}

	public void setVeterinarian(AppUser veterinarian) {
		this.veterinarian = veterinarian;
	}

	public AppUser getTutor() {
		return tutor;
	}

	public void setTutor(AppUser tutor) {
		this.tutor = tutor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public RecommendationPriorityEnum getPriority() {
		return priority;
	}

	public void setPriority(RecommendationPriorityEnum priority) {
		this.priority = priority;
	}

	public RecommendationStatusEnum getStatus() {
		return status;
	}

	public void setStatus(RecommendationStatusEnum status) {
		this.status = status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(OffsetDateTime completedAt) {
		this.completedAt = completedAt;
	}
}
