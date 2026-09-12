package br.com.fiap.petcare360_java.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.repository.AppUserRepository;

@Service
public class CurrentUserService {

	private final AppUserRepository userRepository;

	public CurrentUserService(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String email() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
		}
		return authentication.getName();
	}

	public AppUser user() {
		return userRepository.findByEmail(email())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
	}

	public boolean hasRole(String role) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(role::equals);
	}
}
