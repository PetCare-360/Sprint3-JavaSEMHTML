package br.com.fiap.petcare360_java.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PetVeterinarianId implements Serializable {

	@Column(name = "PET_ID")
	private Long petId;

	@Column(name = "VETERINARIAN_ID")
	private Long veterinarianId;

	public Long getPetId() {
		return petId;
	}

	public void setPetId(Long petId) {
		this.petId = petId;
	}

	public Long getVeterinarianId() {
		return veterinarianId;
	}

	public void setVeterinarianId(Long veterinarianId) {
		this.veterinarianId = veterinarianId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PetVeterinarianId that)) {
			return false;
		}
		return Objects.equals(petId, that.petId) && Objects.equals(veterinarianId, that.veterinarianId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(petId, veterinarianId);
	}
}
