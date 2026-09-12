package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.dto.RecommendationResponse;
import br.com.fiap.petcare360_java.service.RecommendationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	public RecommendationController(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}

	@GetMapping
	public List<RecommendationResponse> listMine() {
		return recommendationService.listMine();
	}

	@PostMapping
	public ResponseEntity<RecommendationResponse> create(@RequestBody @Valid RecommendationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(recommendationService.create(request));
	}
}
