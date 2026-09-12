package br.com.fiap.petcare360_java.dto;

import java.util.List;

public record PetPageResponse(
		List<PetResponse> pets,
		Integer page,
		Integer size,
		Long totalElements,
		Integer totalPages) {
}
