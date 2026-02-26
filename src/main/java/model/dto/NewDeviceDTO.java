package model.dto;

import java.io.Serializable;
import java.util.UUID;

public class NewDeviceDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID id;
	private String name;
	private int deviceTypeId;
	private Integer tableId;

	public NewDeviceDTO() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDeviceTypeId() {
		return deviceTypeId;
	}

	public void setDeviceTypeId(int deviceTypeId) {
		this.deviceTypeId = deviceTypeId;
	}

	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public NewDeviceDTO(UUID id, String name, int deviceTypeId, Integer tableId) {
		this.id = id;
		this.name = name;
		this.deviceTypeId = deviceTypeId;
		this.tableId = tableId;
	}
}
