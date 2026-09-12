package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.dto.AppointmentResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Appointment;
import br.com.fiap.petcare360_java.model.AppointmentStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AppointmentRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetVeterinarianRepository petVeterinarianRepository;

	public AppointmentService(AppointmentRepository appointmentRepository, CurrentUserService currentUserService,
			ClinicalAccessService clinicalAccessService, PetVeterinarianRepository petVeterinarianRepository) {
		this.appointmentRepository = appointmentRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.petVeterinarianRepository = petVeterinarianRepository;
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listMine() {
		String email = currentUserService.email();
		return appointmentRepository.findVisibleForUser(email).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listAll() {
		return appointmentRepository.findAllVisible().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public AppointmentResponse request(AppointmentRequest request) {
		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser veterinarian = clinicalAccessService.userById(request.veterinarianId());

		if (!pet.getUser().getId().equals(currentUserService.user().getId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas o tutor pode solicitar consulta para este pet");
		}

		if (!petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "O veterinário não está vinculado a este pet");
		}

		Appointment appointment = new Appointment();
		appointment.setPet(pet);
		appointment.setTutor(currentUserService.user());
		appointment.setVeterinarian(veterinarian);
		appointment.setScheduledAt(request.scheduledAt());
		appointment.setReason(request.reason().trim());
		appointment.setStatus(AppointmentStatusEnum.REQUESTED);

		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentResponse update(Long id, AppointmentRequest request) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Consulta não encontrada"));

		if (AppointmentStatusEnum.DONE.equals(appointment.getStatus())
				|| AppointmentStatusEnum.CANCELED.equals(appointment.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "Consultas concluídas ou canceladas não podem ser editadas");
		}

		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser veterinarian = clinicalAccessService.userById(request.veterinarianId());
		boolean isVet = veterinarian.getRoles().stream()
				.anyMatch(role -> "ROLE_VETERINARIO".equals(role.getName()));
		if (!isVet) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Usuário selecionado não possui perfil de veterinário");
		}

		if (!currentUserService.hasRole("ROLE_ADMIN")) {
			AppUser currentUser = currentUserService.user();
			if (!currentUserService.hasRole("ROLE_CLIENTE") || !appointment.getTutor().getId().equals(currentUser.getId())
					|| !pet.getUser().getId().equals(currentUser.getId())) {
				throw new ApiException(HttpStatus.FORBIDDEN, "Apenas o tutor da consulta pode editar este agendamento");
			}
		}

		if (!petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "O veterinário não está vinculado a este pet");
		}

		appointment.setPet(pet);
		appointment.setTutor(pet.getUser());
		appointment.setVeterinarian(veterinarian);
		appointment.setScheduledAt(request.scheduledAt());
		appointment.setReason(request.reason().trim());

		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentResponse finish(Long id) {
		Appointment appointment = findManageableAppointment(id);
		appointment.setStatus(AppointmentStatusEnum.DONE);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public void delete(Long id) {
		Appointment appointment = findManageableAppointment(id);
		appointmentRepository.delete(appointment);
	}

	private Appointment findManageableAppointment(Long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Consulta não encontrada"));

		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return appointment;
		}

		if (currentUserService.hasRole("ROLE_VETERINARIO")
				&& appointment.getVeterinarian().getId().equals(currentUserService.user().getId())) {
			return appointment;
		}

		throw new ApiException(HttpStatus.FORBIDDEN, "Apenas veterinário responsável ou administrador pode alterar esta consulta");
	}

	public AppointmentResponse toResponse(Appointment appointment) {
		return new AppointmentResponse(
				appointment.getId(),
				appointment.getPet().getName(),
				appointment.getTutor().getName(),
				appointment.getVeterinarian().getName(),
				appointment.getScheduledAt(),
				appointment.getReason(),
				appointment.getStatus());
	}
}
