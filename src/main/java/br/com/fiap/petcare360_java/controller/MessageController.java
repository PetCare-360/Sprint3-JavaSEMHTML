package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.MessageResponse;
import br.com.fiap.petcare360_java.service.MessageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/messages")
public class MessageController {

	private final MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping
	public List<MessageResponse> listMine() {
		return messageService.listMine();
	}

	@PostMapping
	public ResponseEntity<MessageResponse> send(@RequestBody @Valid MessageRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(messageService.send(request));
	}
}
