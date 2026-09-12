package br.com.fiap.petcare360_java.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByEmail(String email);
	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = "roles")
	List<AppUser> findAll();

	@EntityGraph(attributePaths = "roles")
	List<AppUser> findByRolesName(String roleName);
}
