package servlet.api.v1;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import database.DataAccessException;
import database.SalesDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.CategorySalesDTO;
import model.dto.DailySalesDTO;
import model.dto.HourlySalesDTO;
import model.dto.ProductSalesDTO;
import model.dto.SalesOrderDTO;
import model.dto.SalesSummaryDTO;

/**
 * 売上データAPIサーブレット
 */
@WebServlet("/api/v1/sales/*")
public class SalesApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final SalesDAO salesDAO = new SalesDAO();
	private final Gson gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
			.serializeNulls()
			.create();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json; charset=UTF-8");

		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			pathInfo = "/summary";
		}

		try {
			switch (pathInfo) {
				case "/summary":
					handleSummary(request, response);
					break;
				case "/daily":
					handleDailySales(request, response);
					break;
				case "/hourly":
					handleHourlySales(request, response);
					break;
				case "/monthly":
					handleMonthlySales(request, response);
					break;
				case "/products":
					handleProductSales(request, response);
					break;
				case "/categories":
					handleCategorySales(request, response);
					break;
				case "/orders":
					handleOrders(request, response);
					break;
				case "/popular":
					handlePopularProducts(request, response);
					break;
				default:
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint: " + pathInfo);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * サマリー取得 (今日 / 指定日)
	 */
	private void handleSummary(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Date date = parseDate(request.getParameter("date"), Date.valueOf(LocalDate.now()));
		Date previousDate = Date.valueOf(date.toLocalDate().minusDays(1));

		SalesSummaryDTO todaySummary = salesDAO.getDailySummary(date);
		SalesSummaryDTO previousSummary = salesDAO.getDailySummary(previousDate);

		// 前日比を計算
		todaySummary.setSalesChangePercent(
				calculateChangePercent(todaySummary.getTotalSales(), previousSummary.getTotalSales()));
		todaySummary.setOrderChangePercent(
				calculateChangePercent(todaySummary.getOrderCount(), previousSummary.getOrderCount()));
		todaySummary.setCustomerChangePercent(
				calculateChangePercent(todaySummary.getCustomerCount(), previousSummary.getCustomerCount()));
		todaySummary.setAvgChangePercent(
				calculateChangePercent(todaySummary.getAveragePerCustomer(), previousSummary.getAveragePerCustomer()));

		response.getWriter().write(gson.toJson(todaySummary));
	}

	/**
	 * 日別売上取得
	 */
	private void handleDailySales(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Date date = parseDate(request.getParameter("date"), Date.valueOf(LocalDate.now()));

		SalesSummaryDTO summary = salesDAO.getDailySummary(date);
		List<HourlySalesDTO> hourlyData = salesDAO.getHourlySales(date);

		// 前日比を計算
		Date previousDate = Date.valueOf(date.toLocalDate().minusDays(1));
		SalesSummaryDTO previousSummary = salesDAO.getDailySummary(previousDate);
		summary.setSalesChangePercent(calculateChangePercent(summary.getTotalSales(), previousSummary.getTotalSales()));

		Map<String, Object> result = new HashMap<>();
		result.put("summary", summary);
		result.put("hourlyData", hourlyData);

		response.getWriter().write(gson.toJson(result));
	}

	/**
	 * 時間帯別売上取得
	 */
	private void handleHourlySales(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Date date = parseDate(request.getParameter("date"), Date.valueOf(LocalDate.now()));

		List<HourlySalesDTO> hourlyData = salesDAO.getHourlySales(date);

		response.getWriter().write(gson.toJson(hourlyData));
	}

	/**
	 * 月別売上取得
	 */
	private void handleMonthlySales(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int year = parseInteger(request.getParameter("year"), LocalDate.now().getYear());
		int month = parseInteger(request.getParameter("month"), LocalDate.now().getMonthValue());

		LocalDate firstDay = LocalDate.of(year, month, 1);
		LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

		Date startDate = Date.valueOf(firstDay);
		Date endDate = Date.valueOf(lastDay);

		SalesSummaryDTO summary = salesDAO.getSummaryByPeriod(startDate, endDate);
		List<DailySalesDTO> dailyData = salesDAO.getDailySales(startDate, endDate);

		// 前月比を計算
		LocalDate prevFirstDay = firstDay.minusMonths(1);
		LocalDate prevLastDay = prevFirstDay.withDayOfMonth(prevFirstDay.lengthOfMonth());
		SalesSummaryDTO prevSummary = salesDAO.getSummaryByPeriod(Date.valueOf(prevFirstDay),
				Date.valueOf(prevLastDay));
		summary.setSalesChangePercent(calculateChangePercent(summary.getTotalSales(), prevSummary.getTotalSales()));

		// 最高売上日を計算
		DailySalesDTO bestDay = null;
		for (DailySalesDTO d : dailyData) {
			if (bestDay == null || d.getSales() > bestDay.getSales()) {
				bestDay = d;
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("summary", summary);
		result.put("dailyData", dailyData);
		result.put("bestDay", bestDay);

		response.getWriter().write(gson.toJson(result));
	}

	/**
	 * 商品別売上取得
	 */
	private void handleProductSales(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LocalDate today = LocalDate.now();
		Date startDate = parseDate(request.getParameter("startDate"), Date.valueOf(today.withDayOfMonth(1)));
		Date endDate = parseDate(request.getParameter("endDate"), Date.valueOf(today));

		SalesSummaryDTO summary = salesDAO.getSummaryByPeriod(startDate, endDate);
		List<ProductSalesDTO> products = salesDAO.getProductSales(startDate, endDate);

		// 集計情報
		int totalQuantity = 0;
		String topProduct = products.isEmpty() ? "-" : products.get(0).getName();
		for (ProductSalesDTO p : products) {
			totalQuantity += p.getQuantity();
		}

		Map<String, Object> productSummary = new HashMap<>();
		productSummary.put("totalSales", summary.getTotalSales());
		productSummary.put("totalQuantity", totalQuantity);
		productSummary.put("topProduct", topProduct);
		productSummary.put("productCount", products.size());

		Map<String, Object> result = new HashMap<>();
		result.put("summary", productSummary);
		result.put("products", products);

		response.getWriter().write(gson.toJson(result));
	}

	/**
	 * カテゴリ別売上取得
	 */
	private void handleCategorySales(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LocalDate today = LocalDate.now();
		Date startDate = parseDate(request.getParameter("startDate"), Date.valueOf(today.withDayOfMonth(1)));
		Date endDate = parseDate(request.getParameter("endDate"), Date.valueOf(today));

		List<CategorySalesDTO> categories = salesDAO.getCategorySales(startDate, endDate);

		long totalSales = 0;
		for (CategorySalesDTO c : categories) {
			totalSales += c.getSales();
		}

		Map<String, Object> result = new HashMap<>();
		result.put("categories", categories);
		result.put("totalSales", totalSales);

		response.getWriter().write(gson.toJson(result));
	}

	/**
	 * 注文履歴取得
	 */
	private void handleOrders(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LocalDate today = LocalDate.now();
		Date startDate = parseDate(request.getParameter("startDate"), Date.valueOf(today.minusDays(7)));
		Date endDate = parseDate(request.getParameter("endDate"), Date.valueOf(today));
		Integer status = parseIntegerOrNull(request.getParameter("status"));
		int page = parseInteger(request.getParameter("page"), 1);
		int limit = parseInteger(request.getParameter("limit"), 10);
		int offset = (page - 1) * limit;

		List<SalesOrderDTO> orders = salesDAO.getOrders(startDate, endDate, status, limit, offset);
		int totalCount = salesDAO.getOrderCount(startDate, endDate, status);
		int totalPages = (int) Math.ceil((double) totalCount / limit);

		Map<String, Object> pagination = new HashMap<>();
		pagination.put("currentPage", page);
		pagination.put("totalPages", totalPages);
		pagination.put("totalItems", totalCount);

		Map<String, Object> result = new HashMap<>();
		result.put("orders", orders);
		result.put("pagination", pagination);

		response.getWriter().write(gson.toJson(result));
	}

	/**
	 * 人気商品取得
	 */
	private void handlePopularProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Date date = parseDate(request.getParameter("date"), Date.valueOf(LocalDate.now()));
		int limit = parseInteger(request.getParameter("limit"), 5);

		List<ProductSalesDTO> products = salesDAO.getPopularProducts(date, limit);

		response.getWriter().write(gson.toJson(products));
	}

	/**
	 * 日付パラメータをパース
	 */
	private Date parseDate(String param, Date defaultValue) {
		if (param == null || param.isEmpty()) {
			return defaultValue;
		}
		try {
			return Date.valueOf(LocalDate.parse(param, DateTimeFormatter.ISO_LOCAL_DATE));
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid date format: " + param);
		}
	}

	/**
	 * 整数パラメータをパース
	 */
	private int parseInteger(String param, int defaultValue) {
		if (param == null || param.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(param);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid integer format: " + param);
		}
	}

	/**
	 * 整数パラメータをパース (null許容)
	 */
	private Integer parseIntegerOrNull(String param) {
		if (param == null || param.isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(param);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 変化率を計算
	 */
	private double calculateChangePercent(long current, long previous) {
		if (previous == 0) {
			return current > 0 ? 100.0 : 0.0;
		}
		return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
	}
}
