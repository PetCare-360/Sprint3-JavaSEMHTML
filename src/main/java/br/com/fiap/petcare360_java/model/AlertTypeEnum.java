package br.com.fiap.petcare360_java.model;

public enum AlertTypeEnum {

	ACTIVITY("Atividade"), TEMPERATURE("Temperatura"), HEART_RATE("Batimento cardíaco"), BATTERY("Bateria");

	private String descricao;

	AlertTypeEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return this.descricao;
	}

}
