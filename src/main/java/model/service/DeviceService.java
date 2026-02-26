package model.service;

import java.util.List;
import java.util.UUID;

import database.DeviceDAO;
import model.dto.DeviceDTO;
import model.dto.DeviceTypeDTO;
import model.dto.NewDeviceDTO;

public class DeviceService {

	private DeviceDAO deviceDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public DeviceService() {
		this(new DeviceDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param deviceDAO オプションDAO
	 */
	public DeviceService(DeviceDAO deviceDAO) {
		this.deviceDAO = deviceDAO;
	}

	public DeviceDTO getDeviceById(UUID deviceId) {
		return deviceDAO.getDeviceById(deviceId);
	}

	public List<DeviceDTO> getAllDevices() {
		return deviceDAO.getAllDevices();
	}

	public void registerDevice(NewDeviceDTO device) {
		deviceDAO.registerDevice(device);
	}

	public List<DeviceTypeDTO> getAllDeviceTypes() {
		return deviceDAO.getAllDeviceTypes();
	}

	public void deleteDevices(List<UUID> deviceIds) {
		deviceDAO.deleteDevices(deviceIds);
	}

	public boolean isCustomerAllocated(UUID deviceId) {
		return deviceDAO.isCustomerAllocated(deviceId);
	}
}
