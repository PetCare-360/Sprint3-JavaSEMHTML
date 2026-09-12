package br.com.fiap.petcare360_java.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
	Page<Pet> findByUserEmail(String email, Pageable pageable);

	List<Pet> findByUserEmail(String email);

	List<Pet> findByUserEmailAndActiveTrue(String email);

	@EntityGraph(attributePaths = { "user", "device" })
	List<Pet> findAll();

	Page<Pet> findByUserEmailAndNameContainingIgnoreCaseAndBreedContainingIgnoreCase(
			String email,
			String name,
			String breed,
			Pageable pageable);

	Optional<Pet> findByIdAndUserEmail(Long id, String email);
}
