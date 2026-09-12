package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.dto.RecommendationResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.CareRecommendation;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.CareRecommendationRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;

@Service
public class RecommendationService {

	private final CareRecommendationRepository recommendationRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetVeterinarianRepository petVeterinarianRepository;

	public RecommendationService(CareRecommendationRepository recommendationRepository,
			CurrentUserService currentUserService, ClinicalAccessService clinicalAccessService,
			PetVeterinarianRepository petVeterinarianRepository) {
		this.recommendationRepository = recommendationRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.petVeterinarianRepository = petVeterinarianRepository;
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> listMine() {
		String email = currentUserService.email();
		return recommendationRepository.findVisibleForUser(email).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> listAll() {
		return recommendationRepository.findAllVisible().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public RecommendationResponse create(RecommendationRequest request) {
		if (!currentUserService.hasRole("ROLE_VETERINARIO") && !currentUserService.hasRole("ROLE_ADMIN")) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas veterinários podem enviar recomendações");
		}

		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser veterinarian = currentUserService.user();

		if (!currentUserService.hasRole("ROLE_ADMIN")
				&& !petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Veterinário não vinculado ao pet");
		}

		CareRecommendation recommendation = new CareRecommendation();
		recommendation.setPet(pet);
		recommendation.setTutor(pet.getUser());
		recommendation.setVeterinarian(veterinarian);
		recommendation.setTitle(request.title().trim());
		recommendation.setInstructions(request.instructions().trim());
		recommendation.setPriority(request.priority());

		return toResponse(recommendationRepository.save(recommendation));
	}

	private RecommendationResponse toResponse(CareRecommendation recommendation) {
		return new RecommendationResponse(
				recommendation.getId(),
				recommendation.getPet().getName(),
				recommendation.getTutor().getName(),
				recommendation.getVeterinarian().getName(),
				recommendation.getTitle(),
				recommendation.getInstructions(),
				recommendation.getPriority(),
				recommendation.getStatus(),
				recommendation.getCreatedAt());
	}
}
