package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.dto.CategorySalesDTO;
import model.dto.DailySalesDTO;
import model.dto.HourlySalesDTO;
import model.dto.ProductSalesDTO;
import model.dto.SalesOrderDTO;
import model.dto.SalesOrderDTO.SalesOrderItemDTO;
import model.dto.SalesSummaryDTO;
import util.UuidUtils;

/**
 * 売上データにアクセスするためのDAOクラス
 */
public class SalesDAO {
	private DBManager dbManager;

	public SalesDAO() {
		this(DBManager.getInstance());
	}

	public SalesDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	/**
	 * 指定した日付の売上サマリーを取得する。
	 * 指定した日付に来店した顧客が対象。退店するまでに日付をまたいだ場合、日付けが変わった後の注文も含まれる。
	 * 
	 * @param date 対象とする日付
	 * @return 売上サマリー
	 */
	public SalesSummaryDTO getDailySummary(Date date) {
		final String sql = """
				SELECT
					COALESCE(SUM(total_sales), 0) AS total_sales,
					COALESCE(SUM(order_count), 0) AS order_count,
					COALESCE(SUM(customer_count), 0) AS customer_count
				FROM (
					SELECT
						COALESCE(SUM(m.menu_price + NVL(opt_total, 0)), 0) AS total_sales,
						COUNT(DISTINCT o.order_id) AS order_count,
						c.customer_count AS customer_count
					FROM orders o
					JOIN menus m ON o.menu_id = m.menu_id
					JOIN customers c ON o.customer_id = c.customer_id
					LEFT JOIN (
						SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
						FROM order_options oo
						JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
						GROUP BY oo.order_id, oo.menu_id
					) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
					WHERE TRUNC(c.visited_at) = ?
					GROUP BY c.customer_id, c.customer_count
				)
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, date);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				long totalSales = rs.getLong("total_sales");
				int orderCount = rs.getInt("order_count");
				int customerCount = rs.getInt("customer_count");
				return new SalesSummaryDTO(totalSales, orderCount, customerCount);
			}

			return new SalesSummaryDTO(0, 0, 0);

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get daily summary for date: " + date, e);
		}
	}

	/**
	 * 指定した期間の売上サマリーを取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @return 売上サマリー
	 */
	public SalesSummaryDTO getSummaryByPeriod(Date startDate, Date endDate) {
		final String sql = """
				SELECT
					COALESCE(SUM(total_sales), 0) AS total_sales,
					COALESCE(SUM(order_count), 0) AS order_count,
					COALESCE(SUM(customer_count), 0) AS customer_count
				FROM (
					SELECT
						COALESCE(SUM(m.menu_price + NVL(opt_total, 0)), 0) AS total_sales,
						COUNT(DISTINCT o.order_id) AS order_count,
						c.customer_count AS customer_count
					FROM orders o
					JOIN menus m ON o.menu_id = m.menu_id
					JOIN customers c ON o.customer_id = c.customer_id
					LEFT JOIN (
						SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
						FROM order_options oo
						JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
						GROUP BY oo.order_id, oo.menu_id
					) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
					WHERE TRUNC(c.visited_at) BETWEEN ? AND ?
					GROUP BY c.customer_id, c.customer_count
				)
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, startDate);
			stmt.setDate(2, endDate);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				long totalSales = rs.getLong("total_sales");
				int orderCount = rs.getInt("order_count");
				int customerCount = rs.getInt("customer_count");
				return new SalesSummaryDTO(totalSales, orderCount, customerCount);
			}

			return new SalesSummaryDTO(0, 0, 0);

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get summary for period: " + startDate + " to " + endDate, e);
		}
	}

	/**
	 * 指定された日付の時間帯別売上を取得します。
	 * 顧客が来店した時刻ごとに集計されます。
	 * 
	 * @param date 対象の日付
	 * @return 時間帯別売上リスト
	 */
	public List<HourlySalesDTO> getHourlySales(Date date) {
		final String sql = """
				SELECT
					TO_NUMBER(TO_CHAR(o.ordered_at, 'HH24')) AS hour,
					COALESCE(SUM(m.menu_price + NVL(opt_total, 0)), 0) AS sales,
					COUNT(DISTINCT o.order_id) AS orders
				FROM orders o
				JOIN menus m ON o.menu_id = m.menu_id
				LEFT JOIN (
					SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
					FROM order_options oo
					JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
					GROUP BY oo.order_id, oo.menu_id
				) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
				WHERE TRUNC(o.ordered_at) = ?
				GROUP BY TO_NUMBER(TO_CHAR(o.ordered_at, 'HH24'))
					""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, date);
			ResultSet rs = stmt.executeQuery();

			// 0-23時まで初期化
			Map<Integer, HourlySalesDTO> hourlyMap = new HashMap<>();
			for (int h = 0; h < 24; h++) {
				hourlyMap.put(h, new HourlySalesDTO(h, 0, 0));
			}

			while (rs.next()) {
				int hour = rs.getInt("hour");
				long sales = rs.getLong("sales");
				int orders = rs.getInt("orders");
				hourlyMap.put(hour, new HourlySalesDTO(hour, sales, orders));
			}

			// 全24時間分（0-23時）を返す
			List<HourlySalesDTO> result = new ArrayList<>();
			for (int h = 0; h < 24; h++) {
				result.add(hourlyMap.get(h));
			}
			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get hourly sales for date: " + date, e);
		}
	}

	/**
	 * 指定した期間の日別売上を取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @return 日別売上リスト
	 */
	public List<DailySalesDTO> getDailySales(Date startDate, Date endDate) {
		final String sql = """
				select
					sale_date,
					sum(sales) as sales,
					sum(orders) as orders,
					sum(customers) as customers
				from
					(
						SELECT
							TRUNC(c.visited_at) AS sale_date,
							COALESCE(SUM(m.menu_price + NVL(opt_total, 0)), 0) AS sales,
							COUNT( o.order_id) AS orders,
							c.customer_count as customers
						FROM orders o
						JOIN menus m ON o.menu_id = m.menu_id
						JOIN customers c ON o.customer_id = c.customer_id
						LEFT JOIN (
							SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
							FROM order_options oo
							JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
							GROUP BY oo.order_id, oo.menu_id
						) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
						WHERE TRUNC(c.visited_at) BETWEEN ? AND ?
						GROUP BY c.customer_id, c.customer_count, TRUNC(c.visited_at)
					)
					group by sale_date
					order BY sale_date
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, startDate);
			stmt.setDate(2, endDate);
			ResultSet rs = stmt.executeQuery();

			List<DailySalesDTO> result = new ArrayList<>();
			while (rs.next()) {
				Date saleDate = rs.getDate("sale_date");
				long sales = rs.getLong("sales");
				int orders = rs.getInt("orders");
				int customers = rs.getInt("customers");
				result.add(new DailySalesDTO(saleDate, sales, orders, customers));
			}

			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get daily sales for period: " + startDate + " to " + endDate, e);
		}
	}

	/**
	 * 指定した期間の商品別売上を取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @return 商品別売上リスト（売上金額降順）
	 */
	public List<ProductSalesDTO> getProductSales(Date startDate, Date endDate) {
		final String sql = """
				SELECT
					m.menu_id,
					m.menu_name,
					c.category_name,
					m.menu_price,
					COUNT(*) AS quantity,
					SUM(m.menu_price + NVL(opt_total, 0)) AS sales
				FROM orders o
				JOIN menus m ON o.menu_id = m.menu_id
				JOIN categories c ON m.category_id = c.category_id
				LEFT JOIN (
					SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
					FROM order_options oo
					JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
					GROUP BY oo.order_id, oo.menu_id
				) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
				WHERE TRUNC(o.ordered_at) BETWEEN ? AND ?
				GROUP BY m.menu_id, m.menu_name, c.category_name, m.menu_price
				ORDER BY sales DESC
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, startDate);
			stmt.setDate(2, endDate);
			ResultSet rs = stmt.executeQuery();

			List<ProductSalesDTO> result = new ArrayList<>();
			while (rs.next()) {
				int menuId = rs.getInt("menu_id");
				String name = rs.getString("menu_name");
				String category = rs.getString("category_name");
				int price = rs.getInt("menu_price");
				int quantity = rs.getInt("quantity");
				long sales = rs.getLong("sales");
				result.add(new ProductSalesDTO(menuId, name, category, price, quantity, sales));
			}

			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get product sales for period: " + startDate + " to " + endDate, e);
		}
	}

	/**
	 * 指定した期間のカテゴリ別売上を取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @return カテゴリ別売上リスト（売上金額降順）
	 */
	public List<CategorySalesDTO> getCategorySales(Date startDate, Date endDate) {
		final String sql = """
				SELECT
					COALESCE(parent.category_id, c.category_id) AS category_id,
					COALESCE(parent.category_name, c.category_name) AS category_name,
					SUM(m.menu_price + NVL(opt_total, 0)) AS sales,
					COUNT(*) AS quantity,
					COUNT(DISTINCT m.menu_id) AS product_count
				FROM orders o
				JOIN menus m ON o.menu_id = m.menu_id
				JOIN categories c ON m.category_id = c.category_id
				LEFT JOIN categories parent ON c.parent_category_id = parent.category_id
				LEFT JOIN (
					SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
					FROM order_options oo
					JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
					GROUP BY oo.order_id, oo.menu_id
				) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
				WHERE TRUNC(o.ordered_at) BETWEEN ? AND ?
				GROUP BY COALESCE(parent.category_id, c.category_id), COALESCE(parent.category_name, c.category_name)
				ORDER BY sales DESC
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, startDate);
			stmt.setDate(2, endDate);
			ResultSet rs = stmt.executeQuery();

			List<CategorySalesDTO> result = new ArrayList<>();
			while (rs.next()) {
				int categoryId = rs.getInt("category_id");
				String name = rs.getString("category_name");
				long sales = rs.getLong("sales");
				int quantity = rs.getInt("quantity");
				int productCount = rs.getInt("product_count");
				result.add(new CategorySalesDTO(categoryId, name, sales, quantity, productCount));
			}

			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get category sales for period: " + startDate + " to " + endDate,
					e);
		}
	}

	/**
	 * 指定した期間の注文一覧を取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @param status    ステータス（nullの場合は全て）
	 * @param limit     取得件数
	 * @param offset    オフセット
	 * @return 注文リスト
	 */
	public List<SalesOrderDTO> getOrders(Date startDate, Date endDate, Integer status, int limit, int offset) {
		final String sql = """
				SELECT * FROM (
					SELECT
						o.order_id,
						o.ordered_at,
						t.table_id,
						t.table_name,
						m.menu_name,
						m.menu_price,
						o.order_status_id,
						os.order_status_display_name_staff,
						NVL(opt_total, 0) AS opt_total,
						ROW_NUMBER() OVER (ORDER BY o.ordered_at DESC) AS rn
					FROM orders o
					JOIN menus m ON o.menu_id = m.menu_id
					JOIN customers c ON o.customer_id = c.customer_id
					JOIN tables t ON c.table_id = t.table_id
					JOIN order_statuses os ON o.order_status_id = os.order_status_id
					LEFT JOIN (
						SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
						FROM order_options oo
						JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
						GROUP BY oo.order_id, oo.menu_id
					) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
					WHERE TRUNC(o.ordered_at) BETWEEN ? AND ?
				"""
				+ (status != null ? " AND o.order_status_id = ?" : "")
				+ """
						) WHERE rn > ? AND rn <= ?
						ORDER BY ordered_at DESC
						""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			int paramIndex = 1;
			stmt.setDate(paramIndex++, startDate);
			stmt.setDate(paramIndex++, endDate);
			if (status != null) {
				stmt.setInt(paramIndex++, status);
			}
			stmt.setInt(paramIndex++, offset);
			stmt.setInt(paramIndex++, offset + limit);

			ResultSet rs = stmt.executeQuery();

			// order_idでグループ化
			Map<String, SalesOrderDTO> orderMap = new HashMap<>();
			List<String> orderIds = new ArrayList<>();

			while (rs.next()) {
				byte[] orderIdBytes = rs.getBytes("order_id");
				String orderId = UuidUtils.fromBytes(orderIdBytes).toString();

				SalesOrderDTO order = orderMap.get(orderId);
				if (order == null) {
					order = new SalesOrderDTO();
					order.setOrderId(orderId);
					order.setCreatedAt(rs.getTimestamp("ordered_at"));
					order.setTableNumber(rs.getInt("table_id"));
					order.setTableName(rs.getString("table_name"));
					order.setStatus(rs.getString("order_status_display_name_staff"));
					order.setItems(new ArrayList<>());
					order.setTotal(0);
					orderMap.put(orderId, order);
					orderIds.add(orderId);
				}

				// 商品情報を追加
				String menuName = rs.getString("menu_name");
				int menuPrice = rs.getInt("menu_price");
				int optTotal = rs.getInt("opt_total");
				order.getItems().add(new SalesOrderItemDTO(menuName, menuPrice + optTotal, 1));
				order.setTotal(order.getTotal() + menuPrice + optTotal);
			}

			// 順序を保持してリスト化
			List<SalesOrderDTO> result = new ArrayList<>();
			for (String orderId : orderIds) {
				result.add(orderMap.get(orderId));
			}

			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get orders for period: " + startDate + " to " + endDate, e);
		}
	}

	/**
	 * 指定した期間の注文件数を取得する
	 * 
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @param status    ステータス（nullの場合は全て）
	 * @return 注文件数
	 */
	public int getOrderCount(Date startDate, Date endDate, Integer status) {
		final String sql = """
				SELECT COUNT(DISTINCT o.order_id) AS cnt
				FROM orders o
				WHERE TRUNC(o.ordered_at) BETWEEN ? AND ?
				"""
				+ (status != null ? " AND o.order_status_id = ?" : "");

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			int paramIndex = 1;
			stmt.setDate(paramIndex++, startDate);
			stmt.setDate(paramIndex++, endDate);
			if (status != null) {
				stmt.setInt(paramIndex++, status);
			}

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("cnt");
			}
			return 0;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get order count for period: " + startDate + " to " + endDate, e);
		}
	}

	/**
	 * 指定した日付の人気商品TOP Nを取得する
	 * 
	 * @param date  日付
	 * @param limit 取得件数
	 * @return 商品別売上リスト
	 */
	public List<ProductSalesDTO> getPopularProducts(Date date, int limit) {
		final String sql = """
				SELECT * FROM (
					SELECT
						m.menu_id,
						m.menu_name,
						c.category_name,
						m.menu_price,
						COUNT(*) AS quantity,
						SUM(m.menu_price + NVL(opt_total, 0)) AS sales
					FROM orders o
					JOIN menus m ON o.menu_id = m.menu_id
					JOIN categories c ON m.category_id = c.category_id
					LEFT JOIN (
						SELECT oo.order_id, oo.menu_id, SUM(op.option_price) AS opt_total
						FROM order_options oo
						JOIN options op ON oo.menu_id = op.menu_id AND oo.option_id = op.option_id
						GROUP BY oo.order_id, oo.menu_id
					) opt ON o.order_id = opt.order_id AND o.menu_id = opt.menu_id
					WHERE TRUNC(o.ordered_at) = ?
					GROUP BY m.menu_id, m.menu_name, c.category_name, m.menu_price
					ORDER BY quantity DESC, sales DESC
				) WHERE ROWNUM <= ?
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, date);
			stmt.setInt(2, limit);
			ResultSet rs = stmt.executeQuery();

			List<ProductSalesDTO> result = new ArrayList<>();
			while (rs.next()) {
				int menuId = rs.getInt("menu_id");
				String name = rs.getString("menu_name");
				String category = rs.getString("category_name");
				int price = rs.getInt("menu_price");
				int quantity = rs.getInt("quantity");
				long sales = rs.getLong("sales");
				result.add(new ProductSalesDTO(menuId, name, category, price, quantity, sales));
			}

			return result;

		} catch (SQLException e) {
			throw new DataAccessException("Failed to get popular products for date: " + date, e);
		}
	}

}
