package br.com.fiap.petcare360_java.model;

public enum AlertStatusEnum {
	OPEN("Aberto"), IN_PROGRESS("Em atendimento"), RESOLVED("Resolvido");

	private final String descricao;

	AlertStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
