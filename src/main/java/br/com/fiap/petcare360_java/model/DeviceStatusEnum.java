package br.com.fiap.petcare360_java.model;

public enum DeviceStatusEnum {

	ACTIVE("Ativo"), INACTIVE("Inativo"), MAINTENANCE("Manutenção");

	private String descricao;

	DeviceStatusEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return this.descricao;
	}

}
