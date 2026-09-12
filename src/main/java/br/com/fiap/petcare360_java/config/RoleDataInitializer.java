package br.com.fiap.petcare360_java.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.fiap.petcare360_java.model.Role;
import br.com.fiap.petcare360_java.repository.RoleRepository;

@Configuration
public class RoleDataInitializer {

	@Bean
	CommandLineRunner createDefaultRoles(RoleRepository roleRepository) {
		return args -> {
			createRole(roleRepository, "ROLE_ADMIN", "Acesso administrativo ao sistema");
			createRole(roleRepository, "ROLE_CLIENTE", "Acesso do tutor aos próprios pets");
			createRole(roleRepository, "ROLE_VETERINARIO", "Acesso médico para acompanhamento dos pets");
		};
	}

	private void createRole(RoleRepository roleRepository, String name, String description) {
		if (roleRepository.findByName(name).isPresent()) {
			return;
		}

		Role role = new Role();
		role.setName(name);
		role.setDescription(description);
		roleRepository.save(role);
	}
}
