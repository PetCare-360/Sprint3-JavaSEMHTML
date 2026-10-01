package br.com.fiap.petcare360_java.dto;

public record AuthResponse(String message, UserResponse user, String token, String tokenType, Long expiresIn) {
	public AuthResponse(String message, UserResponse user) {
		this(message, user, null, null, null);
	}
}
