package br.com.fiap.petcare360_java.model;

public enum MessageStatusEnum {
	SENT("Enviada"), READ("Lida"), ANSWERED("Respondida");

	private final String descricao;

	MessageStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
