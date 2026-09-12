package br.com.fiap.petcare360_java.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertLevelEnum;

public interface AlertRepository extends JpaRepository<Alert, Long> {
	List<Alert> findByPetIdOrderByCreatedAtDesc(Long petId);
	Page<Alert> findByPetId(Long petId, Pageable pageable);
	Page<Alert> findByPetIdAndLevel(Long petId, AlertLevelEnum level, Pageable pageable);
	Optional<Alert> findByIdAndPetId(Long id, Long petId);
	long countByPetId(Long petId);
}
