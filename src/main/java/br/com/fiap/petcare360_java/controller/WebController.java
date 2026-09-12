package br.com.fiap.petcare360_java.controller;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.dto.InitialSensorDataRequest;
import br.com.fiap.petcare360_java.dto.PetRequest;
import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.AuthService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.MessageService;
import br.com.fiap.petcare360_java.service.PetService;
import br.com.fiap.petcare360_java.service.RecommendationService;
import br.com.fiap.petcare360_java.service.VeterinarianLinkService;

@Controller
public class WebController {

	private final PetService petService;
	private final AuthService authService;
	private final MessageService messageService;
	private final AppointmentService appointmentService;
	private final RecommendationService recommendationService;
	private final VeterinarianLinkService veterinarianLinkService;
	private final CurrentUserService currentUserService;
	private final AppUserRepository userRepository;
	private final PetRepository petRepository;
	private final PetVeterinarianRepository petVeterinarianRepository;
	private final SensorDataRepository sensorDataRepository;
	private final AlertRepository alertRepository;

	public WebController(PetService petService, AuthService authService, MessageService messageService,
			AppointmentService appointmentService, RecommendationService recommendationService,
			VeterinarianLinkService veterinarianLinkService, CurrentUserService currentUserService,
			AppUserRepository userRepository, PetRepository petRepository,
			PetVeterinarianRepository petVeterinarianRepository, SensorDataRepository sensorDataRepository,
			AlertRepository alertRepository) {
		this.petService = petService;
		this.authService = authService;
		this.messageService = messageService;
		this.appointmentService = appointmentService;
		this.recommendationService = recommendationService;
		this.veterinarianLinkService = veterinarianLinkService;
		this.currentUserService = currentUserService;
		this.userRepository = userRepository;
		this.petRepository = petRepository;
		this.petVeterinarianRepository = petVeterinarianRepository;
		this.sensorDataRepository = sensorDataRepository;
		this.alertRepository = alertRepository;
	}

	@GetMapping("/")
	public String home() {
		return "redirect:/dashboard";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(
			@RequestParam String name,
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam(defaultValue = "ROLE_CLIENTE") String role) {
		authService.register(new RegisterRequest(name, email, password, role));
		return "redirect:/login?registered";
	}

	@GetMapping("/dashboard")
	public String dashboard() {
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return "redirect:/admin";
		}
		if (currentUserService.hasRole("ROLE_VETERINARIO")) {
			return "redirect:/vet";
		}
		return "redirect:/tutor";
	}

	@GetMapping("/tutor")
	public String tutorDashboard(Model model) {
		addTutorData(model);
		return "tutor/index";
	}

	@GetMapping("/tutor/pets")
	public String tutorPets(Model model) {
		addTutorData(model);
		return "tutor/pets";
	}

	@GetMapping({ "/tutor/pets/{id}", "/vet/pets/{id}", "/admin/pets/{id}" })
	public String petDetail(@PathVariable Long id, Model model) {
		var pet = petService.findEditablePet(id);
		model.addAttribute("pet", pet);
		model.addAttribute("latestReading", sensorDataRepository.findFirstByDevicePetIdOrderByTimestampDesc(id).orElse(null));
		model.addAttribute("readings", sensorDataRepository.findTop30ByDevicePetIdOrderByTimestampDesc(id));
		model.addAttribute("alerts", alertRepository.findByPetIdOrderByCreatedAtDesc(id));
		model.addAttribute("links", petVeterinarianRepository.findAll().stream()
				.filter(link -> link.getPet().getId().equals(id))
				.toList());
		model.addAttribute("isAdmin", currentUserService.hasRole("ROLE_ADMIN"));
		model.addAttribute("isVet", currentUserService.hasRole("ROLE_VETERINARIO"));
		model.addAttribute("isTutor", currentUserService.hasRole("ROLE_CLIENTE"));
		return "pet-detail";
	}

	@GetMapping("/tutor/pets/novo")
	public String tutorNewPet() {
		return "tutor/pet-form";
	}

	@GetMapping({ "/tutor/pets/{id}/editar", "/vet/pets/{id}/editar", "/admin/pets/{id}/editar" })
	public String editPet(@PathVariable Long id, Model model) {
		model.addAttribute("pet", petService.findEditablePet(id));
		model.addAttribute("isAdmin", currentUserService.hasRole("ROLE_ADMIN"));
		model.addAttribute("isVet", currentUserService.hasRole("ROLE_VETERINARIO"));
		model.addAttribute("isTutor", currentUserService.hasRole("ROLE_CLIENTE"));
		return "pet-edit";
	}

	@GetMapping("/tutor/veterinarios")
	public String tutorVeterinarians(Model model) {
		addTutorData(model);
		return "tutor/veterinarios";
	}

	@GetMapping("/tutor/mensagens")
	public String tutorMessages(Model model) {
		addTutorData(model);
		return "tutor/mensagens";
	}

	@GetMapping("/tutor/consultas")
	public String tutorAppointments(@RequestParam(required = false) String status, Model model) {
		addTutorData(model);
		applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status);
		return "tutor/consultas";
	}

	@GetMapping("/tutor/recomendacoes")
	public String tutorRecommendations(Model model) {
		addTutorData(model);
		return "tutor/recomendacoes";
	}

	@GetMapping("/vet")
	public String veterinarianDashboard(Model model) {
		addVetData(model);
		return "vet/index";
	}

	@GetMapping("/vet/pets")
	public String veterinarianPets(Model model) {
		addVetData(model);
		return "vet/pets";
	}

	@GetMapping("/vet/mensagens")
	public String veterinarianMessages(Model model) {
		addVetData(model);
		return "vet/mensagens";
	}

	@GetMapping("/vet/consultas")
	public String veterinarianAppointments(@RequestParam(required = false) String status, Model model) {
		addVetData(model);
		applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status);
		return "vet/consultas";
	}

	@GetMapping("/vet/recomendacoes")
	public String veterinarianRecommendations(Model model) {
		addVetData(model);
		return "vet/recomendacoes";
	}

	@GetMapping("/admin")
	public String adminDashboard(Model model) {
		addAdminData(model);
		return "admin/index";
	}

	@GetMapping("/admin/usuarios")
	public String adminUsers(@RequestParam(required = false) String role, Model model) {
		addAdminData(model);
		if (role != null && !role.isBlank()) {
			model.addAttribute("users", userRepository.findAll().stream()
					.filter(user -> user.getRoles().stream().anyMatch(item -> item.getName().equals(role)))
					.toList());
		}
		model.addAttribute("selectedRole", role);
		return "admin/usuarios";
	}

	@GetMapping("/admin/pets")
	public String adminPets(@RequestParam(required = false, defaultValue = "todos") String active, Model model) {
		addAdminData(model);
		if ("ativos".equals(active)) {
			model.addAttribute("pets", petRepository.findAll().stream()
					.filter(pet -> Boolean.TRUE.equals(pet.getActive()))
					.toList());
		} else if ("inativos".equals(active)) {
			model.addAttribute("pets", petRepository.findAll().stream()
					.filter(pet -> !Boolean.TRUE.equals(pet.getActive()))
					.toList());
		}
		model.addAttribute("selectedActive", active);
		return "admin/pets";
	}

	@GetMapping("/admin/vinculos")
	public String adminLinks(Model model) {
		addAdminData(model);
		return "admin/vinculos";
	}

	@GetMapping("/admin/mensagens")
	public String adminMessages(Model model) {
		addAdminData(model);
		return "admin/mensagens";
	}

	@GetMapping("/admin/consultas")
	public String adminAppointments(@RequestParam(required = false) String status, Model model) {
		addAdminData(model);
		applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status);
		return "admin/consultas";
	}

	@GetMapping("/admin/recomendacoes")
	public String adminRecommendations(Model model) {
		addAdminData(model);
		return "admin/recomendacoes";
	}

	private void addTutorData(Model model) {
		String email = currentUserService.email();
		var pets = petService.listAll();
		var links = petVeterinarianRepository.findActiveLinksByTutorEmail(email);
		var messages = messageService.listMine();
		var appointments = appointmentService.listMine();
		var recommendations = recommendationService.listMine();
		model.addAttribute("pets", pets);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		model.addAttribute("linkedVeterinarians", links);
		model.addAttribute("messages", messages);
		model.addAttribute("appointments", appointments);
		model.addAttribute("recommendations", recommendations);
		model.addAttribute("petCount", pets.size());
		model.addAttribute("linkCount", links.size());
		model.addAttribute("messageCount", messages.size());
		model.addAttribute("appointmentCount", appointments.size());
		model.addAttribute("recommendationCount", recommendations.size());
		model.addAttribute("alertCount", pets.stream().mapToLong(pet -> alertRepository.countByPetId(pet.id())).sum());
	}

	private void addVetData(Model model) {
		var linkedPets = petVeterinarianRepository.findActiveLinksByVeterinarianEmail(currentUserService.email());
		var messages = messageService.listMine();
		var appointments = appointmentService.listMine();
		var recommendations = recommendationService.listMine();
		model.addAttribute("linkedPets", linkedPets);
		model.addAttribute("messages", messages);
		model.addAttribute("appointments", appointments);
		model.addAttribute("recommendations", recommendations);
		model.addAttribute("linkedPetCount", linkedPets.size());
		model.addAttribute("messageCount", messages.size());
		model.addAttribute("appointmentCount", appointments.size());
		model.addAttribute("recommendationCount", recommendations.size());
		model.addAttribute("alertCount", linkedPets.stream().mapToLong(link -> alertRepository.countByPetId(link.getPet().getId())).sum());
	}

	private void addAdminData(Model model) {
		var users = userRepository.findAll();
		var pets = petRepository.findAll();
		var links = petVeterinarianRepository.findAll();
		var messages = messageService.listAll();
		var appointments = appointmentService.listAll();
		var recommendations = recommendationService.listAll();
		model.addAttribute("users", users);
		model.addAttribute("pets", pets);
		model.addAttribute("links", links);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		model.addAttribute("messages", messages);
		model.addAttribute("appointments", appointments);
		model.addAttribute("recommendations", recommendations);
		model.addAttribute("userCount", users.size());
		model.addAttribute("petCount", pets.size());
		model.addAttribute("activePetCount", pets.stream().filter(pet -> Boolean.TRUE.equals(pet.getActive())).count());
		model.addAttribute("linkCount", links.size());
		model.addAttribute("messageCount", messages.size());
		model.addAttribute("appointmentCount", appointments.size());
		model.addAttribute("recommendationCount", recommendations.size());
	}

	@SuppressWarnings("unchecked")
	private void applyAppointmentFilter(String status, Model model) {
		if (status == null || status.isBlank()) {
			return;
		}
		var appointments = (List<br.com.fiap.petcare360_java.dto.AppointmentResponse>) model.getAttribute("appointments");
		if (appointments == null) {
			return;
		}
		model.addAttribute("appointments", appointments.stream()
				.filter(item -> item.status().name().equals(status))
				.toList());
	}

	@PostMapping("/messages/form")
	public String sendMessage(
			@RequestParam(required = false) Long petId,
			@RequestParam(required = false) Long receiverId,
			@RequestParam(required = false) String conversationKey,
			@RequestParam String subject,
			@RequestParam String message) {
		Long[] selected = selectedPair(conversationKey, petId, receiverId);
		petId = selected[0];
		receiverId = selected[1];
		messageService.send(new MessageRequest(petId, receiverId, subject, message));
		return redirectToArea("mensagens");
	}

	@PostMapping("/pets/form")
	public String createPet(
			@RequestParam String name,
			@RequestParam Integer age,
			@RequestParam BigDecimal weight,
			@RequestParam String breed,
			@RequestParam String species,
			@RequestParam String deviceId,
			@RequestParam BigDecimal temperature,
			@RequestParam Integer heartRate,
			@RequestParam Integer activityLevel,
			@RequestParam Integer battery) {
		InitialSensorDataRequest sensor = new InitialSensorDataRequest(
				OffsetDateTime.now(),
				temperature,
				heartRate,
				activityLevel,
				null,
				null,
				battery);
		petService.create(new PetRequest(name, age, weight, breed, species, deviceId, sensor));
		return "redirect:/tutor/pets";
	}

	@PostMapping("/pets/{id}/form")
	public String updatePet(
			@PathVariable Long id,
			@RequestParam String name,
			@RequestParam Integer age,
			@RequestParam BigDecimal weight,
			@RequestParam String breed,
			@RequestParam String species,
			@RequestParam String deviceId) {
		petService.updateBasic(id, name, age, weight, breed, species, deviceId);
		return redirectToArea("pets");
	}

	@PostMapping("/pets/{id}/remover")
	public String removePet(@PathVariable Long id) {
		petService.deactivate(id);
		return redirectToArea("pets");
	}

	@PostMapping("/appointments/form")
	public String requestAppointment(
			@RequestParam(required = false) Long petId,
			@RequestParam(required = false) Long veterinarianId,
			@RequestParam(required = false) String appointmentKey,
			@RequestParam String scheduledAt,
			@RequestParam String reason) {
		Long[] selected = selectedPair(appointmentKey, petId, veterinarianId);
		petId = selected[0];
		veterinarianId = selected[1];
		OffsetDateTime date = LocalDateTime.parse(scheduledAt)
				.atZone(ZoneId.systemDefault())
				.toOffsetDateTime();
		appointmentService.request(new AppointmentRequest(petId, veterinarianId, date, reason));
		return redirectToArea("consultas");
	}

	@PostMapping("/appointments/{id}/finalizar")
	public String finishAppointment(@PathVariable Long id) {
		appointmentService.finish(id);
		return redirectToArea("consultas");
	}

	@PostMapping("/appointments/{id}/remover")
	public String removeAppointment(@PathVariable Long id) {
		appointmentService.delete(id);
		return redirectToArea("consultas");
	}

	@PostMapping("/veterinarians/link/form")
	public String linkVeterinarian(@RequestParam Long petId, @RequestParam Long veterinarianId) {
		veterinarianLinkService.link(new VeterinarianLinkRequest(petId, veterinarianId));
		return redirectToArea("veterinarios");
	}

	@PostMapping("/recommendations/form")
	public String createRecommendation(
			@RequestParam Long petId,
			@RequestParam String title,
			@RequestParam String instructions,
			@RequestParam RecommendationPriorityEnum priority) {
		recommendationService.create(new RecommendationRequest(petId, title, instructions, priority));
		return redirectToArea("recomendacoes");
	}

	private String redirectToArea(String area) {
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			if ("veterinarios".equals(area)) {
				return "redirect:/admin/vinculos";
			}
			return "redirect:/admin/" + area;
		}
		if (currentUserService.hasRole("ROLE_VETERINARIO")) {
			return "redirect:/vet/" + area;
		}
		return "redirect:/tutor/" + area;
	}

	private Long[] selectedPair(String pairKey, Long firstId, Long secondId) {
		if (pairKey != null && !pairKey.isBlank()) {
			String[] ids = pairKey.split(":");
			return new Long[] { Long.valueOf(ids[0]), Long.valueOf(ids[1]) };
		}
		return new Long[] { firstId, secondId };
	}
}
