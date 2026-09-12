package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.MessageResponse;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.PetMessage;
import br.com.fiap.petcare360_java.repository.PetMessageRepository;
import jakarta.validation.Valid;

@Service
public class MessageService {

	private final PetMessageRepository messageRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;

	public MessageService(PetMessageRepository messageRepository, CurrentUserService currentUserService,
			ClinicalAccessService clinicalAccessService) {
		this.messageRepository = messageRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
	}

	@Transactional(readOnly = true)
	public List<MessageResponse> listMine() {
		String email = currentUserService.email();
		return messageRepository.findVisibleForUser(email).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MessageResponse> listAll() {
		return messageRepository.findAllVisible().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public MessageResponse send(@Valid MessageRequest request) {
		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser receiver = clinicalAccessService.userById(request.receiverId());
		clinicalAccessService.validateConversation(pet, receiver);

		PetMessage message = new PetMessage();
		message.setPet(pet);
		message.setSender(currentUserService.user());
		message.setReceiver(receiver);
		message.setSubject(request.subject().trim());
		message.setMessage(request.message().trim());

		return toResponse(messageRepository.save(message));
	}

	private MessageResponse toResponse(PetMessage message) {
		return new MessageResponse(
				message.getId(),
				message.getPet().getId(),
				message.getPet().getName(),
				message.getSender().getName(),
				message.getReceiver().getName(),
				message.getSubject(),
				message.getMessage(),
				message.getStatus(),
				message.getCreatedAt());
	}
}
