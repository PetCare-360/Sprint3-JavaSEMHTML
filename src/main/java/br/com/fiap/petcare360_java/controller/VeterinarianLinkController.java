package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.UserResponse;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkResponse;
import br.com.fiap.petcare360_java.service.VeterinarianLinkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/veterinarians")
public class VeterinarianLinkController {

	private final VeterinarianLinkService veterinarianLinkService;

	public VeterinarianLinkController(VeterinarianLinkService veterinarianLinkService) {
		this.veterinarianLinkService = veterinarianLinkService;
	}

	@Operation(
			summary = "Listar veterinários",
			description = "Retorna os usuários ativos com perfil ROLE_VETERINARIO para vínculo com pets.")
	@GetMapping
	public List<UserResponse> listVeterinarians() {
		return veterinarianLinkService.listVeterinarians();
	}

	@Operation(
			summary = "Vincular veterinário ao pet",
			description = "Cria um vínculo entre o pet e um veterinário. Apenas o tutor dono do pet pode criar o vínculo.")
	@PostMapping("/link")
	public ResponseEntity<VeterinarianLinkResponse> link(@RequestBody @Valid VeterinarianLinkRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(veterinarianLinkService.link(request));
	}
}
