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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
	public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@Operation(
			summary = "Login",
			description = "Valida e-mail e senha usando Spring Security.")
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
	}

	@Operation(
			summary = "Usuário autenticado",
			description = "Retorna os dados do usuário logado quando a sessão JSESSIONID ainda está válida.")
	@GetMapping("/me")
	public UserResponse me() {
		return authService.me();
	}

	@Operation(
			summary = "Logout",
			description = "Invalida a sessão atual e expira o cookie JSESSIONID no servidor.")
	@PostMapping("/logout")
	public ResponseEntity<MessageOnlyResponse> logout(HttpServletRequest request, HttpServletResponse response) {
		SecurityContextHolder.clearContext();

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		Cookie cookie = new Cookie("JSESSIONID", null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		return ResponseEntity.ok(new MessageOnlyResponse("Logout realizado com sucesso."));
	}
}
