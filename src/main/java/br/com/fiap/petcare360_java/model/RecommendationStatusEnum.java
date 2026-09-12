package br.com.fiap.petcare360_java.model;

public enum RecommendationStatusEnum {
	ACTIVE("Ativa"), COMPLETED("Concluída"), CANCELED("Cancelada");

	private final String descricao;

	RecommendationStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
