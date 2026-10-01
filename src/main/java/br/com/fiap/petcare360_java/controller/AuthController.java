package br.com.fiap.petcare360_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.AuthRequest;
import br.com.fiap.petcare360_java.dto.AuthResponse;
import br.com.fiap.petcare360_java.dto.MessageOnlyResponse;
import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.dto.UserResponse;
import br.com.fiap.petcare360_java.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Operation(
			summary = "Registrar usuário",
			description = "Cria uma conta de usuário cliente para acesso ao sistema.")
	@PostMapping("/register")
	@SecurityRequirements
	public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@Operation(
			summary = "Login",
			description = "Valida e-mail e senha usando Spring Security.")
	@PostMapping("/login")
	@SecurityRequirements
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@Operation(
			summary = "Usuário autenticado",
			description = "Retorna o usuario identificado pelo JWT enviado em Authorization: Bearer.")
	@GetMapping("/me")
	public UserResponse me() {
		return authService.me();
	}

	@Operation(
			summary = "Logout",
			description = "Confirma a saida. O cliente deve apagar o JWT; o token emitido continua valido ate expirar.")
	@PostMapping("/logout")
	public ResponseEntity<MessageOnlyResponse> logout() {
		SecurityContextHolder.clearContext();

		return ResponseEntity.ok(new MessageOnlyResponse("Remova o token do aplicativo para concluir o logout."));
	}
}
