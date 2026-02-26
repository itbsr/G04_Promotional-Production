package model.dto;

import java.util.UUID;

public class DeviceDTO extends NewDeviceDTO {
	private String deviceTypeName;
	private String tableName;

	public DeviceDTO() {
	}

	public DeviceDTO(UUID id, String name, int deviceTypeId, String deviceTypeName, Integer tableId, String tableName) {
		super(id, name, deviceTypeId, tableId);
		this.deviceTypeName = deviceTypeName;
		this.tableName = tableName;
	}

	public String getDeviceTypeName() {
		return deviceTypeName;
	}

	public void setDeviceTypeName(String deviceTypeName) {
		this.deviceTypeName = deviceTypeName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public enum DeviceTypes {
		ADMIN(1),
		COUNTER(2),
		KITCHEN(3),
		HALL(4),
		REGISTER(5),
		TEST_PRIVILEGE(999);

		private final int id;

		DeviceTypes(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}
}
