package br.com.fiap.petcare360_java.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import br.com.fiap.petcare360_java.dto.UserResponse;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.PetVeterinarian;
import br.com.fiap.petcare360_java.model.PetVeterinarianId;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;

@Service
public class VeterinarianLinkService {

	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetVeterinarianRepository petVeterinarianRepository;
	private final AppUserRepository userRepository;
	private final PetMapper mapper;

	public VeterinarianLinkService(CurrentUserService currentUserService, ClinicalAccessService clinicalAccessService,
			PetVeterinarianRepository petVeterinarianRepository, AppUserRepository userRepository, PetMapper mapper) {
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.petVeterinarianRepository = petVeterinarianRepository;
		this.userRepository = userRepository;
		this.mapper = mapper;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> listVeterinarians() {
		return userRepository.findByRolesName("ROLE_VETERINARIO").stream()
				.filter(user -> Boolean.TRUE.equals(user.getEnabled()))
				.map(mapper::toUserResponse)
				.toList();
	}

	@Transactional
	public VeterinarianLinkResponse link(VeterinarianLinkRequest request) {
		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser tutor = currentUserService.user();
		if (!currentUserService.hasRole("ROLE_CLIENTE") || !pet.getUser().getId().equals(tutor.getId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas o tutor pode vincular veterinário ao pet");
		}

		AppUser veterinarian = clinicalAccessService.userById(request.veterinarianId());
		boolean isVet = veterinarian.getRoles().stream()
				.anyMatch(role -> "ROLE_VETERINARIO".equals(role.getName()));
		if (!isVet) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Usuário selecionado não possui perfil de veterinário");
		}

		PetVeterinarianId linkId = new PetVeterinarianId();
		linkId.setPetId(pet.getId());
		linkId.setVeterinarianId(veterinarian.getId());
		if (petVeterinarianRepository.existsById(linkId)
				|| petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.CONFLICT, "Veterinário já vinculado a este pet");
		}

		PetVeterinarian link = new PetVeterinarian();
		link.setId(linkId);
		link.setPet(pet);
		link.setVeterinarian(veterinarian);
		link.setActive(true);
		PetVeterinarian saved = petVeterinarianRepository.save(link);
		return toResponse(saved);
	}

	private VeterinarianLinkResponse toResponse(PetVeterinarian link) {
		return new VeterinarianLinkResponse(
				link.getPet().getId(),
				link.getPet().getName(),
				link.getVeterinarian().getId(),
				link.getVeterinarian().getName(),
				link.getAssignedAt(),
				link.getActive());
	}
}
