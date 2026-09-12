package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.IotDataRequest;
import br.com.fiap.petcare360_java.dto.IotDataResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.service.IotProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/iot")
public class IotController {

	private final IotProcessingService iotProcessingService;

	public IotController(IotProcessingService iotProcessingService) {
		this.iotProcessingService = iotProcessingService;
	}

	@Operation(
			summary = "Receber dados da coleira inteligente",
			description = "Valida a telemetria enviada pelo device, verifica vínculo com pet, calcula status, salva histórico e gera alertas quando necessário.")
	@PostMapping("/data")
	public ResponseEntity<IotDataResponse> receive(@RequestBody @Valid IotDataRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(iotProcessingService.process(request));
	}

	@Operation(
			summary = "Listar leituras IoT",
			description = "Lista as leituras de sensores cadastradas, da mais recente para a mais antiga.")
	@GetMapping("/data")
	public List<SensorDataResponse> list() {
		return iotProcessingService.listReadings();
	}

	@Operation(
			summary = "Buscar leitura IoT",
			description = "Busca uma leitura de sensor pelo ID.")
	@GetMapping("/data/{id}")
	public SensorDataResponse find(@PathVariable Long id) {
		return iotProcessingService.findReading(id);
	}

	@Operation(
			summary = "Atualizar leitura IoT",
			description = "Atualiza os dados de uma leitura de sensor existente.")
	@PutMapping("/data/{id}")
	public SensorDataResponse update(@PathVariable Long id, @RequestBody @Valid IotDataRequest request) {
		return iotProcessingService.updateReading(id, request);
	}

	@Operation(
			summary = "Remover leitura IoT",
			description = "Remove uma leitura de sensor pelo ID.")
	@DeleteMapping("/data/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		iotProcessingService.deleteReading(id);
		return ResponseEntity.noContent().build();
	}
}
