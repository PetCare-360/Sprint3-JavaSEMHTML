package br.com.fiap.petcare360_java.model;

public enum RecommendationPriorityEnum {
	LOW("Baixa"), NORMAL("Normal"), HIGH("Alta");

	private final String descricao;

	RecommendationPriorityEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
