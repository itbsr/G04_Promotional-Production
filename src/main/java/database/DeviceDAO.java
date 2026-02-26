package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.dto.DeviceDTO;
import model.dto.DeviceTypeDTO;
import model.dto.NewDeviceDTO;
import util.UuidUtils;

public class DeviceDAO {
	// Table name
	public static final String TABLE_NAME = "devices";

	// Column names
	public static final String COLUMN_DEVICE_ID = "device_id";
	public static final String COLUMN_DEVICE_NAME = "device_name";
	public static final String COLUMN_DEVICE_TYPE_ID = "device_type_id";
	public static final String COLUMN_DEVICE_TYPE_NAME = "device_type_name";

	// Device type ID
	public static final int DEVICE_TYPE_ID_TABLE = 2; // Table type ID
	public static final int DEVICE_TYPE_ID_KITCHEN = 3; // Kitchen type ID
	public static final int DEVICE_TYPE_ID_HALL = 4; // Hall type ID

	private DBManager dbManager;

	/**
	 * デフォルトコンストラクタ
	 */
	public DeviceDAO() {
		this(DBManager.getInstance());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param dbManager
	 */
	public DeviceDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	/**
	 * 指定されたデバイスIDに対応するデバイス情報を取得する
	 *
	 * @param deviceId デバイスID
	 * @return デバイス情報。存在しない場合はnull。
	 */
	public DeviceDTO getDeviceById(UUID deviceId) {
		final String sql = "SELECT "
				+ COLUMN_DEVICE_ID + ", "
				+ COLUMN_DEVICE_NAME + ", "
				+ COLUMN_DEVICE_TYPE_ID + ", "
				+ COLUMN_DEVICE_TYPE_NAME + ", "
				+ TableDAO.COLUMN_TABLE_ID + ", "
				+ TableDAO.COLUMN_TABLE_NAME
				+ " FROM devices "
				+ "JOIN device_types USING ("
				+ COLUMN_DEVICE_TYPE_ID
				+ ") LEFT JOIN tables USING ("
				+ TableDAO.COLUMN_TABLE_ID
				+ ") WHERE " + COLUMN_DEVICE_ID + " = ?";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setBytes(1, UuidUtils.toBytes(deviceId));

			final ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int tableIdValue = rs.getInt(TableDAO.COLUMN_TABLE_ID);
				Integer tableId = rs.wasNull() ? null : tableIdValue;
				String tableName = rs.getString(TableDAO.COLUMN_TABLE_NAME);
				return new DeviceDTO(
						UuidUtils.fromBytes(rs.getBytes(COLUMN_DEVICE_ID)),
						rs.getString(COLUMN_DEVICE_NAME),
						rs.getInt(COLUMN_DEVICE_TYPE_ID),
						rs.getString(COLUMN_DEVICE_TYPE_NAME),
						tableId,
						tableName);
			} else {
				return null;
			}

		} catch (SQLException e) {
			throw new DataAccessException("デバイスの取得に失敗しました", e);
		}
	}

	public List<DeviceDTO> getAllDevices() {
		final String sql = "SELECT "
				+ COLUMN_DEVICE_ID + ", "
				+ COLUMN_DEVICE_NAME + ", "
				+ COLUMN_DEVICE_TYPE_ID + ", "
				+ COLUMN_DEVICE_TYPE_NAME + ", "
				+ TableDAO.COLUMN_TABLE_ID + ", "
				+ TableDAO.COLUMN_TABLE_NAME
				+ " FROM devices "
				+ "JOIN device_types USING ("
				+ COLUMN_DEVICE_TYPE_ID
				+ ") LEFT JOIN tables USING ("
				+ TableDAO.COLUMN_TABLE_ID
				+ ") ORDER BY " + COLUMN_DEVICE_ID;

		try (final Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<DeviceDTO> devices = new ArrayList<>();

			while (rs.next()) {
				int tableIdValue = rs.getInt(TableDAO.COLUMN_TABLE_ID);
				Integer tableId = rs.wasNull() ? null : tableIdValue;
				String tableName = rs.getString(TableDAO.COLUMN_TABLE_NAME);
				devices.add(new DeviceDTO(
						UuidUtils.fromBytes(rs.getBytes(COLUMN_DEVICE_ID)),
						rs.getString(COLUMN_DEVICE_NAME),
						rs.getInt(COLUMN_DEVICE_TYPE_ID),
						rs.getString(COLUMN_DEVICE_TYPE_NAME),
						tableId,
						tableName));
			}

			return devices;
		} catch (SQLException e) {
			throw new DataAccessException("デバイスの取得に失敗しました", e);
		}
	}

	public void registerDevice(NewDeviceDTO device) {
		final String sql = "INSERT INTO devices ("
				+ COLUMN_DEVICE_ID + ", "
				+ COLUMN_DEVICE_NAME + ", "
				+ COLUMN_DEVICE_TYPE_ID + ", "
				+ TableDAO.COLUMN_TABLE_ID
				+ ") VALUES (?, ?, ?, ?)";

		try (final Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setBytes(1, UuidUtils.toBytes(device.getId()));
			stmt.setString(2, device.getName());
			stmt.setInt(3, device.getDeviceTypeId());
			if (device.getTableId() == null) {
				stmt.setNull(4, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(4, device.getTableId());
			}

			final var result = stmt.executeUpdate();
			if (result == 0) {
				throw new DataAccessException("DBサーバーエラーによりデバイスの登録に失敗しました", null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataAccessException("DBサーバーエラー(" + e.getMessage() + ")", e);
		}
	}

	public List<DeviceTypeDTO> getAllDeviceTypes() {
		final String sql = "SELECT "
				+ COLUMN_DEVICE_TYPE_ID + ", "
				+ COLUMN_DEVICE_TYPE_NAME
				+ " FROM device_types";

		List<DeviceTypeDTO> deviceTypes = new ArrayList<>();

		try (final Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String id = rs.getString(COLUMN_DEVICE_TYPE_ID);
				String name = rs.getString(COLUMN_DEVICE_TYPE_NAME);
				DeviceTypeDTO deviceType = new DeviceTypeDTO(id, name);
				deviceTypes.add(deviceType);
			}
		} catch (SQLException e) {
			throw new DataAccessException("デバイスタイプの取得に失敗しました", e);
		}

		return deviceTypes;
	}

	public void deleteDevices(List<UUID> deviceIds) {
		final String sql = "DELETE FROM devices WHERE " + COLUMN_DEVICE_ID + " = ?";
		try (Connection conn = DBManager.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			conn.setAutoCommit(false);

			for (UUID deviceId : deviceIds) {
				stmt.setBytes(1, UuidUtils.toBytes(deviceId));
				if (stmt.executeUpdate() == 0) {
					conn.rollback();
					throw new EntityNotFoundException("指定されたデバイスが見つかりません", deviceId);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			throw new DataAccessException("DBサーバーエラー", e);
		}
	}

	public boolean isCustomerAllocated(UUID deviceId) {
		final String sql = "SELECT COUNT(*) AS cnt"
				+ " FROM " + CustomerDAO.TABLE_NAME
				+ " JOIN customer_statuses"
				+ " USING ( " + CustomerDAO.COLUMN_STATUS + " )"
				+ " WHERE table_id = ("
				+ "     SELECT table_id"
				+ "     FROM " + TABLE_NAME
				+ "     WHERE " + COLUMN_DEVICE_ID + " = ?"
				+ ") and customer_status_id = 2";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBytes(1, UuidUtils.toBytes(deviceId));
			final ResultSet rs = stmt.executeQuery();
			if (rs.next() && rs.getInt("cnt") > 0)
				return true;
			else
				return false;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to check if customer is allocated", e);
		}
	}
}
