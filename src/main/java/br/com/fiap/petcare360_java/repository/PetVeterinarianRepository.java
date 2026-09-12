package br.com.fiap.petcare360_java.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.fiap.petcare360_java.model.PetVeterinarian;
import br.com.fiap.petcare360_java.model.PetVeterinarianId;

public interface PetVeterinarianRepository extends JpaRepository<PetVeterinarian, PetVeterinarianId> {
	@Query("select count(pv) > 0 from PetVeterinarian pv where pv.pet.id = :petId and pv.veterinarian.email = :email and pv.active = true and pv.pet.active = true")
	boolean existsActiveLink(Long petId, String email);

	@EntityGraph(attributePaths = { "pet", "pet.user", "veterinarian" })
	@Query("select pv from PetVeterinarian pv where pv.veterinarian.email = :email and pv.active = true and pv.pet.active = true")
	List<PetVeterinarian> findActiveLinksByVeterinarianEmail(String email);

	@EntityGraph(attributePaths = { "pet", "pet.user", "veterinarian" })
	@Query("select pv from PetVeterinarian pv where pv.pet.user.email = :email and pv.active = true and pv.pet.active = true")
	List<PetVeterinarian> findActiveLinksByTutorEmail(String email);

	@EntityGraph(attributePaths = { "pet", "pet.user", "veterinarian" })
	List<PetVeterinarian> findAll();
}
