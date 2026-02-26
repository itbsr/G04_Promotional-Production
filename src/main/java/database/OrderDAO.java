package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.dto.NewOrderWithIdDTO;
import model.dto.OrderDTO;
import util.UuidUtils;

public class OrderDAO {
	private DBManager dbManager;

	public OrderDAO() {
		this(DBManager.getInstance());
	}

	public OrderDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	// Column names
	private static final String COLUMN_ORDER_ID = "order_id";
	private static final String COLUMN_MENU_ID = "menu_id";
	private static final String COLUMN_CUSTOMER_ID = "customer_id";
	private static final String COLUMN_OPTION_ID = "option_id";

	// Order Status values
	public static final int STATUS_ORDERED = 1;
	public static final int STATUS_COOKED = 2;
	public static final int STATUS_SERVED = 3;

	public boolean registerOrder(List<NewOrderWithIdDTO> order, UUID customerId) {
		final String sql = "INSERT INTO orders ("
				+ COLUMN_ORDER_ID + ","
				+ COLUMN_MENU_ID + ","
				+ " order_status_id,"
				+ COLUMN_CUSTOMER_ID
				+ ") VALUES (?, ?, ?, ?)";

		final String optionSql = "INSERT INTO order_options ("
				+ COLUMN_ORDER_ID + ","
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_ID
				+ ") VALUES (?, ?, ?)";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				PreparedStatement optionStmt = conn.prepareStatement(optionSql)) {
			conn.setAutoCommit(false);

			for (NewOrderWithIdDTO orderDTO : order) {
				stmt.setBytes(1, UuidUtils.toBytes(orderDTO.getOrderId()));
				stmt.setInt(2, orderDTO.getMenuId());
				stmt.setInt(3, STATUS_ORDERED);
				stmt.setBytes(4, UuidUtils.toBytes(customerId));
				stmt.addBatch();

				// オプションの登録
				for (Integer optionId : orderDTO.getOptionIds()) {
					optionStmt.setBytes(1, UuidUtils.toBytes(orderDTO.getOrderId()));
					optionStmt.setInt(2, orderDTO.getMenuId());
					optionStmt.setInt(3, optionId);
					optionStmt.addBatch();
				}
			}

			stmt.executeBatch();
			optionStmt.executeBatch();

			conn.commit();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 指定された顧客IDの注文履歴を注文確定時系列順で取得する
	 * 
	 * @param customerId 顧客ID
	 * @param shouldUseCustomerStatusName trueなら顧客向け表示名、falseならスタッフ向け表示名を取得
	 * @return 注文DTOのリスト
	 */
	public List<OrderDTO> getOrdersByCustomerId(UUID customerId, boolean shouldUseCustomerStatusName) {
		final String displayNameColumn = shouldUseCustomerStatusName
				? "order_status_display_name_customer"
				: "order_status_display_name_staff";

		final String sql = """
				SELECT *
				FROM (
					SELECT order_id, menu_id, order_status_id,""" + displayNameColumn
				+ """
						,   ordered_at, cooked_at, table_name, LISTAGG(option_id, ',') WITHIN GROUP(ORDER BY option_id) AS option_ids
							FROM orders
							LEFT JOIN order_options USING ( order_id, menu_id )
							JOIN order_statuses USING ( order_status_id )
							JOIN customers USING ( customer_id )
							JOIN tables USING ( table_id )
							WHERE customer_id = ?
							GROUP BY order_id, menu_id, order_status_id, """
				+ displayNameColumn + """
							, ordered_at, cooked_at, table_name
						)
						ORDER BY ordered_at ASC
						""";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setBytes(1, UuidUtils.toBytes(customerId));
			final ResultSet rs = stmt.executeQuery();

			final List<OrderDTO> orders = new ArrayList<>();
			while (rs.next()) {
				// parse option ids(comma separated string)
				final String sOptionIds = rs.getString("option_ids");
				final List<String> rgsOptionIds = sOptionIds != null ? List.of(sOptionIds.split(","))
						: new ArrayList<String>();
				final List<Integer> rgiOptionIds = new ArrayList<>();
				for (var i : rgsOptionIds)
					rgiOptionIds.add(Integer.parseInt(i));

				int statusId = rs.getInt("order_status_id");
				if (shouldUseCustomerStatusName && statusId == STATUS_COOKED)
					statusId = STATUS_ORDERED;// 顧客向け表示名では、調理済みは「調理中」とする

				orders.add(new OrderDTO(
						UuidUtils.fromBytes(rs.getBytes("order_id")),
						rs.getInt("menu_id"),
						statusId,
						rs.getString(displayNameColumn),
						rs.getString("table_name"),
						rgiOptionIds,
						rs.getTimestamp("ordered_at"),
						rs.getTimestamp("cooked_at")));
			}
			return orders;
		} catch (SQLException e) {
			throw new DataAccessException("Failed to retrieve order history for customer ID: " + customerId, e);
		}
	}

	/**
	 * 指定されたステータスIDの注文一覧を注文確定時系列順で取得する
	 * 
	 * @param statusId 注文ステータスID
	 * @return 注文DTOのリスト
	 */
	public List<OrderDTO> getOrdersByStatusId(int statusId) {
		final String sql = """
				SELECT *
				FROM (
					SELECT order_id, menu_id, order_status_id, order_status_display_name_staff,
						ordered_at, cooked_at, table_name, LISTAGG(option_id, ',') WITHIN GROUP(ORDER BY option_id) AS option_ids
					FROM orders
					LEFT JOIN order_options USING ( order_id, menu_id )
					JOIN order_statuses USING ( order_status_id )
					JOIN customers USING ( customer_id )
					JOIN tables USING ( table_id )
					WHERE order_status_id = ?
					GROUP BY order_id, menu_id, order_status_id, order_status_display_name_staff, ordered_at, cooked_at, table_name
				)
				ORDER BY ordered_at ASC
				""";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setInt(1, statusId);

			try (final ResultSet rs = stmt.executeQuery()) {
				final List<OrderDTO> orders = new ArrayList<>();

				while (rs.next()) {
					// parse option ids(comma separated string)
					final String sOptionIds = rs.getString("option_ids");
					final List<String> rgsOptionIds = sOptionIds != null ? List.of(sOptionIds.split(","))
							: new ArrayList<String>();
					final List<Integer> rgiOptionIds = new ArrayList<>();
					for (var i : rgsOptionIds)
						rgiOptionIds.add(Integer.parseInt(i));

					orders.add(new OrderDTO(UuidUtils.fromBytes(rs.getBytes("order_id")),
							rs.getInt("menu_id"),
							rs.getInt("order_status_id"),
							rs.getString("order_status_display_name_staff"),
							rs.getString("table_name"),
							rgiOptionIds,
							rs.getTimestamp("ordered_at"),
							rs.getTimestamp("cooked_at")));
				}
				return orders;
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to retrieve orders for status ID: " + statusId, e);
		}
	}

	/**
	 * 指定された注文IDで注文を取得する
	 * 
	 * @param orderId 注文ID
	 * @return 注文DTO、見つからない場合はnull
	 */
	public OrderDTO getOrderById(UUID orderId) {
		final String sql = """
				SELECT order_id, menu_id, order_status_id, order_status_display_name_staff,
					ordered_at, cooked_at, table_name, LISTAGG(option_id, ',') WITHIN GROUP(ORDER BY option_id) AS option_ids
				FROM orders
				LEFT JOIN order_options USING ( order_id, menu_id )
				JOIN order_statuses USING ( order_status_id )
				JOIN customers USING ( customer_id )
				JOIN tables USING ( table_id )
				WHERE order_id = ?
				GROUP BY order_id, menu_id, order_status_id, order_status_display_name_staff, ordered_at, cooked_at, table_name
				""";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setBytes(1, UuidUtils.toBytes(orderId));
			final ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// parse option ids(comma separated string)
				final String sOptionIds = rs.getString("option_ids");
				final List<String> rgsOptionIds = sOptionIds != null ? List.of(sOptionIds.split(","))
						: new ArrayList<String>();
				final List<Integer> rgiOptionIds = new ArrayList<>();
				for (var i : rgsOptionIds)
					rgiOptionIds.add(Integer.parseInt(i));

				return new OrderDTO(
						UuidUtils.fromBytes(rs.getBytes("order_id")),
						rs.getInt("menu_id"),
						rs.getInt("order_status_id"),
						rs.getString("order_status_display_name_staff"),
						rs.getString("table_name"),
						rgiOptionIds,
						rs.getTimestamp("ordered_at"),
						rs.getTimestamp("cooked_at"));
			}
			throw new EntityNotFoundException("Order not found for order", orderId);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to retrieve order for order ID: " + orderId, e);
		}
	}

	/**
	 * 指定された注文IDの注文ステータスを更新する
	 * 
	 * @param orderId 注文ID
	 * @param statusId 新しいステータスID
	 * @return 更新に成功した場合true、失敗した場合false
	 */
	public void updateOrderStatus(UUID orderId, int statusId) {
		final String sql = "UPDATE orders SET order_status_id = ? WHERE order_id = ?";

		try (final Connection conn = dbManager.getConnection();
				final PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setInt(1, statusId);
			stmt.setBytes(2, UuidUtils.toBytes(orderId));
			final int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated == 0) {
				throw new EntityNotFoundException("Order not found", orderId);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to update order status for order ID: " + orderId, e);
		}
	}
}
