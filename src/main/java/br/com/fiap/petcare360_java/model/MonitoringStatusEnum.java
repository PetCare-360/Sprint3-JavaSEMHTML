package br.com.fiap.petcare360_java.model;

public enum MonitoringStatusEnum {

	NORMAL("Normal"), WARNING("Alerta"), CRITICAL("Crítico");

	private String descricao;

	MonitoringStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return this.descricao;
	}

}
