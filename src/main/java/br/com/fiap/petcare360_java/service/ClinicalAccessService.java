package br.com.fiap.petcare360_java.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;

@Service
public class ClinicalAccessService {

	private final CurrentUserService currentUserService;
	private final PetRepository petRepository;
	private final AppUserRepository userRepository;
	private final PetVeterinarianRepository petVeterinarianRepository;

	public ClinicalAccessService(CurrentUserService currentUserService, PetRepository petRepository,
			AppUserRepository userRepository, PetVeterinarianRepository petVeterinarianRepository) {
		this.currentUserService = currentUserService;
		this.petRepository = petRepository;
		this.userRepository = userRepository;
		this.petVeterinarianRepository = petVeterinarianRepository;
	}

	public Pet accessiblePet(Long petId) {
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return petRepository.findById(petId)
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
		}

		String email = currentUserService.email();
		if (currentUserService.hasRole("ROLE_VETERINARIO")
				&& petVeterinarianRepository.existsActiveLink(petId, email)) {
			return petRepository.findById(petId)
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
		}

		return petRepository.findByIdAndUserEmail(petId, email)
				.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Você não tem acesso a este pet"));
	}

	public AppUser userById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	public void validateConversation(Pet pet, AppUser otherUser) {
		AppUser current = currentUserService.user();
		if (current.getId().equals(otherUser.getId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Remetente e destinatário devem ser diferentes");
		}

		boolean tutorToVet = pet.getUser().getId().equals(current.getId())
				&& petVeterinarianRepository.existsActiveLink(pet.getId(), otherUser.getEmail());
		boolean vetToTutor = pet.getUser().getId().equals(otherUser.getId())
				&& petVeterinarianRepository.existsActiveLink(pet.getId(), current.getEmail());

		if (!currentUserService.hasRole("ROLE_ADMIN") && !tutorToVet && !vetToTutor) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Mensagem permitida apenas entre tutor e veterinário vinculado");
		}
	}
}
