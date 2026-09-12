package br.com.fiap.petcare360_java.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "SJ_PET_VETERINARIANS")
public class PetVeterinarian {

	@EmbeddedId
	private PetVeterinarianId id = new PetVeterinarianId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("petId")
	@JoinColumn(name = "PET_ID")
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("veterinarianId")
	@JoinColumn(name = "VETERINARIAN_ID")
	private AppUser veterinarian;

	@Column(name = "ASSIGNED_AT", nullable = false)
	private OffsetDateTime assignedAt;

	@Column(nullable = false)
	private Boolean active = true;

	@PrePersist
	public void prePersist() {
		if (assignedAt == null) {
			assignedAt = OffsetDateTime.now();
		}
	}

	public PetVeterinarianId getId() {
		return id;
	}

	public void setId(PetVeterinarianId id) {
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

	public OffsetDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(OffsetDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
