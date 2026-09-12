package br.com.fiap.petcare360_java.model;

public enum AppointmentStatusEnum {
	REQUESTED("Solicitada"), CONFIRMED("Confirmada"), CANCELED("Cancelada"), DONE("Concluída");

	private final String descricao;

	AppointmentStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
