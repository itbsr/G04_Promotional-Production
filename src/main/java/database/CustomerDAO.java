package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import model.dto.CustomerDTO;
import model.dto.DeviceDTO;
import model.dto.NewCustomerDTO;
import util.UuidUtils;

/**
 * 顧客テーブルへのアクセスを管理するDAOクラス
 */
public class CustomerDAO {
	private DBManager dbManager;

	/**
	 * デフォルトコンストラクタ
	 */
	public CustomerDAO() {
		this(DBManager.getInstance());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param dbManager
	 */
	public CustomerDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	// Table name
	public static final String TABLE_NAME = "customers";

	// Column names
	public static final String COLUMN_CUSTOMER_ID = "customer_id";
	public static final String COLUMN_CUSTOMER_COUNT = "customer_count";
	public static final String COLUMN_STATUS = "customer_status_id";

	// Status values
	public static final int STATUS_REGISTERED = 1;
	public static final int STATUS_EATING = 2;
	public static final int STATUS_CHECKOUT_TARGET = 3;

	/**
	 * 新規顧客を登録する
	 * 
	 * @param newCustomer 新規顧客DTO
	 * @return 登録成功時true、失敗時false
	 */
	public boolean registerCustomer(NewCustomerDTO newCustomer) {
		final String sql = "INSERT INTO customers ("
				+ COLUMN_CUSTOMER_ID + ","
				+ COLUMN_CUSTOMER_COUNT
				+ ") VALUES (?, ?)";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setBytes(1, UuidUtils.toBytes(newCustomer.getCustomerId()));
			stmt.setInt(2, newCustomer.getCustomerCount());

			int insertedRows = stmt.executeUpdate();
			return insertedRows > 0;

		} catch (SQLException e) {
			throw new DataAccessException(
					"Failed to register new customer with ID: " + newCustomer.getCustomerId(), e);
		}
	}

	/**
	 * デバイスIDに基づいて顧客のテーブルを設定する。
	 * 
	 * @param deviceId
	 * @param customerId statusがREGISTEREDの顧客ID
	 */
	public void setTableByDeviceId(UUID deviceId, UUID customerId) {
		final String sqlSetTableId = "UPDATE " + TABLE_NAME
				+ " SET table_id = ("
				+ "   SELECT table_id"
				+ "   FROM " + DeviceDAO.TABLE_NAME
				+ "   WHERE " + DeviceDAO.COLUMN_DEVICE_TYPE_ID + " = " + DeviceDAO.DEVICE_TYPE_ID_TABLE
				+ "   AND " + DeviceDAO.COLUMN_DEVICE_ID + " = ?"
				+ " )WHERE " + COLUMN_CUSTOMER_ID + " = ?"
				+ "AND " + COLUMN_STATUS + " = " + STATUS_REGISTERED;
		final String sqlChangeStatus = "UPDATE " + TABLE_NAME
				+ " SET " + COLUMN_STATUS + " = " + STATUS_EATING
				+ " WHERE " + COLUMN_CUSTOMER_ID + " = ?"
				+ " AND " + COLUMN_STATUS + " = " + STATUS_REGISTERED;

		// Validate input
		if (deviceId == null || customerId == null) {
			throw new IllegalArgumentException("Device ID and Customer ID must not be null");
		}

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmtSetTableId = conn.prepareStatement(sqlSetTableId);
				PreparedStatement stmtChangeStatus = conn.prepareStatement(sqlChangeStatus)) {
			conn.setAutoCommit(false);
			try {
				// Set table_id based on device_id
				stmtSetTableId.setBytes(1, UuidUtils.toBytes(deviceId));
				stmtSetTableId.setBytes(2, UuidUtils.toBytes(customerId));
				int rowsUpdated = stmtSetTableId.executeUpdate();
				if (rowsUpdated != 1) {
					conn.rollback();
					throw new EntityNotFoundException("Customer or device not found", deviceId);
				}

				// Change customer status to EATING
				stmtChangeStatus.setBytes(1, UuidUtils.toBytes(customerId));
				rowsUpdated = stmtChangeStatus.executeUpdate();
				if (rowsUpdated != 1) {
					conn.rollback();
					throw new EntityNotFoundException("No customer found", customerId);
				}

				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to set table by device ID", e);
		}
	}

	public CustomerDTO getAllocatedCustomerByDevice(DeviceDTO device) {
		final String sql = "SELECT "
				+ COLUMN_CUSTOMER_ID + ","
				+ COLUMN_CUSTOMER_COUNT + ","
				+ "table_id,"
				+ "paid_at,"
				+ COLUMN_STATUS + ","
				+ " customer_status_display_name"
				+ " FROM " + TABLE_NAME
				+ " JOIN customer_statuses"
				+ " USING ( " + COLUMN_STATUS + " )"
				+ " WHERE table_id = ("
				+ "     SELECT table_id"
				+ "     FROM " + DeviceDAO.TABLE_NAME
				+ "     WHERE " + DeviceDAO.COLUMN_DEVICE_ID + " = ?"
				+ ") AND " + COLUMN_STATUS + " = " + STATUS_EATING;

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBytes(1, UuidUtils.toBytes(device.getId()));
			final ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CustomerDTO(
						UuidUtils.fromBytes(rs.getBytes(COLUMN_CUSTOMER_ID)),
						rs.getInt(COLUMN_CUSTOMER_COUNT),
						rs.getInt("table_id"),
						rs.getTimestamp("paid_at"),
						rs.getInt(COLUMN_STATUS),
						rs.getString("customer_status_display_name"));
			else
				throw new EntityNotFoundException("No allocated customer found for device", device.getId());
		} catch (SQLException e) {
			throw new DataAccessException("Failed to get allocated customer by device", e);
		}
	}

	public CustomerDTO getCustomerById(UUID customerId) {
		final String sql = """
				select customer_id,
				       customer_count,
				       table_id,
					   paid_at,
				       customer_status_id,
				       customer_status_display_name
				  from customers
				  join customer_statuses
				  using (customer_status_id)
				 where customer_id = ?
				""";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBytes(1, UuidUtils.toBytes(customerId));
			final ResultSet rs = stmt.executeQuery();

			if (rs.next())
				return new CustomerDTO(
						UuidUtils.fromBytes(rs.getBytes("customer_id")),
						rs.getInt("customer_count"),
						rs.getInt("table_id"),
						rs.getTimestamp("paid_at"),
						rs.getInt("customer_status_id"),
						rs.getString("customer_status_display_name"));
			else
				throw new EntityNotFoundException("No customer found", customerId);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to get customer by ID", e);
		}
	}

	/**
	 * 顧客のステータスを会計対象（CHECKOUT_TARGET）に変更する
	 *
	 * @param customerId 対象の顧客ID
	 */
	public void updateStatusToCheckoutTarget(UUID customerId) {
		final String sql = "UPDATE customers"
				+ "   SET customer_status_id = " + CustomerDTO.CustomerStatus.DINING_COMPLETED
				+ " WHERE customer_id = ?"
				+ "   AND customer_status_id = " + CustomerDTO.CustomerStatus.EATING
				+ "   AND paid_at IS NULL";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBytes(1, UuidUtils.toBytes(customerId));

			int updated = stmt.executeUpdate();
			if (updated != 1)
				throw new EntityNotFoundException(
						"No customer updated (customer not found, already checked out, or not eligible for checkout)",
						customerId);

		} catch (SQLException e) {
			throw new DataAccessException("Failed to update customer status to checkout target", e);
		}
	}

	/**
	 * 顧客の支払い時刻を現在時刻（SYSDATE）に設定する
	 *
	 * @param customerId 対象の顧客ID
	 */
	public void markPaid(UUID customerId) {
		final String sql = """
				update customers
				   set
				   paid_at = sysdate
				 where customer_id = ?
				   and paid_at is null
				""";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBytes(1, UuidUtils.toBytes(customerId));
			int updated = stmt.executeUpdate();
			if (updated != 1) {
				throw new EntityNotFoundException("No customer updated (maybe already paid or not found)", customerId);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to mark customer as paid", e);
		}
	}
}
