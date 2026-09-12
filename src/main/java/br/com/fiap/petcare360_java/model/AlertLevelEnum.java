package br.com.fiap.petcare360_java.model;

public enum AlertLevelEnum {

	INFO("Informativo"), WARNING("Alerta"), CRITICAL("Crítico");

	private String descricao;

	AlertLevelEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return this.descricao;
	}

}
