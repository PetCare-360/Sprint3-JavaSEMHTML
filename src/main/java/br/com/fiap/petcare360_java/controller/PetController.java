package br.com.fiap.petcare360_java.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import br.com.fiap.petcare360_java.dto.ActivitySummaryResponse;
import br.com.fiap.petcare360_java.dto.PetHealthStatusResponse;
import br.com.fiap.petcare360_java.dto.PetPageResponse;
import br.com.fiap.petcare360_java.dto.PetRequest;
import br.com.fiap.petcare360_java.dto.PetResponse;
import br.com.fiap.petcare360_java.dto.QuickAlertPetResponse;
import br.com.fiap.petcare360_java.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pets")
public class PetController {

	private final PetService petService;

	public PetController(PetService petService) {
		this.petService = petService;
	}

	@Operation(
			summary = "Listar pets com paginação",
			description = "Retorna os pets do usuário autenticado em uma resposta paginada simples para leitura no Swagger.")
	@GetMapping
	public PetPageResponse list(
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		return petService.list(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
	}

	@Operation(
			summary = "Listar todos os pets",
			description = "Retorna todos os pets do usuário autenticado em uma lista simples, sem paginação.")
	@GetMapping("/all")
	public List<PetResponse> listAll() {
		return petService.listAll();
	}

	@Operation(
			summary = "Listar pacientes do veterinário",
			description = "Retorna os pets vinculados ao veterinário autenticado. Administradores recebem os pets ativos do sistema.")
	@GetMapping("/patients")
	public List<PetResponse> myPatients() {
		return petService.listByVeterinarian();
	}

	@Operation(
			summary = "Cadastrar pet",
			description = "Cria o pet, a coleira inteligente e a primeira leitura de sensores em uma única requisição.")
	@PostMapping
	public ResponseEntity<PetResponse> create(@RequestBody @Valid PetRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(petService.create(request));
	}

	@Operation(
			summary = "Buscar pet por ID",
			description = "Retorna um pet específico do usuário autenticado.")
	@GetMapping("/{id}")
	public PetResponse find(@PathVariable Long id) {
		return petService.find(id);
	}

	@Operation(
			summary = "Status consolidado de saúde",
			description = "Retorna o estado atual do pet com base na última leitura de temperatura, batimentos, atividade e bateria.")
	@GetMapping("/{id}/health-status")
	public PetHealthStatusResponse healthStatus(@PathVariable Long id) {
		return petService.healthStatus(id);
	}

	@Operation(
			summary = "Alertas rápidos",
			description = "Lista apenas os pets do usuário autenticado que estão em WARNING ou CRITICAL na última leitura.")
	@GetMapping("/quick-alerts")
	public List<QuickAlertPetResponse> quickAlerts() {
		return petService.quickAlerts();
	}

	@Operation(
			summary = "Resumo de atividade das últimas 24h",
			description = "Calcula médias de temperatura, batimentos e atividade com base nas leituras das últimas 24 horas.")
	@GetMapping("/{id}/activity-summary")
	public ActivitySummaryResponse activitySummary(@PathVariable Long id) {
		return petService.activitySummary(id);
	}

	@Operation(
			summary = "Atualizar pet",
			description = "Atualiza o pet, a coleira inteligente e registra uma nova leitura histórica de sensores.")
	@PutMapping("/{id}")
	public PetResponse update(@PathVariable Long id, @RequestBody @Valid PetRequest request) {
		return petService.update(id, request);
	}

	@Operation(
			summary = "Remover pet",
			description = "Remove o pet do usuário autenticado e seus dados vinculados.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		petService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
