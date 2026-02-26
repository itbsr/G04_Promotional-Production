package viewmodel;

import java.io.Serializable;
import java.util.List;

import model.dto.DeviceDTO;
import model.dto.DeviceTypeDTO;
import model.dto.TableDTO;

public class ManageDeviceViewModel implements Serializable {
	static final long serialVersionUID = 1L;

	private List<DeviceTypeDTO> deviceTypes;
	private List<DeviceDTO> devices;
	private List<TableDTO> tables;

	public ManageDeviceViewModel() {
	}

	public ManageDeviceViewModel(List<DeviceTypeDTO> deviceTypes, List<DeviceDTO> devices, List<TableDTO> tables) {
		this.deviceTypes = deviceTypes;
		this.devices = devices;
		this.tables = tables;
	}

	public List<DeviceTypeDTO> getDeviceTypes() {
		return deviceTypes;
	}

	public void setDeviceTypes(List<DeviceTypeDTO> deviceTypes) {
		this.deviceTypes = deviceTypes;
	}

	public List<DeviceDTO> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceDTO> devices) {
		this.devices = devices;
	}

	public List<TableDTO> getTables() {
		return tables;
	}

	public void setTables(List<TableDTO> tables) {
		this.tables = tables;
	}
}
