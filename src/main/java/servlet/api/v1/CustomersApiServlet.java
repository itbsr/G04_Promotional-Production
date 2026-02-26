package servlet.api.v1;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import database.DataAccessException;
import database.EntityNotFoundException;
import filter.DeviceDistinctionFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.CustomerDTO;
import model.dto.DeviceDTO;
import model.dto.NewCustomerDTO;
import model.dto.OrderDTO;
import model.service.CustomerService;
import model.service.OrderService;
import util.UuidUtils;

/**
 * 顧客管理APIサーブレット
 */
@WebServlet({ "/api/v1/customers", "/api/v1/customers/*" })
public class CustomersApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			return;
		} else if (pathInfo.matches("/" + UuidUtils.REGEX_UUID_ANY_VERSION + "/orders/?")
				|| pathInfo.matches("/\\d+/orders/?")) {
			try {
				// parse customerId from path
				final String[] pathParts = pathInfo.split("/");
				final String sCustomerId = pathParts[1];

				// 仮仕様として数値も許可。UUIDv8形式。
				// 詳細はCustomerService.registerCustomer()内のコメントを参照。
				final UUID customerId = sCustomerId.matches(UuidUtils.REGEX_UUID_ANY_VERSION)
						? UUID.fromString(sCustomerId)
						: new UUID(0x8000,
								(0x8000_0000_0000_0000L | Integer.parseInt(sCustomerId)));

				// get orders by customerId
				final OrderService orderService = new OrderService();
				final List<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, false); // TODO: 端末種別で表示名切り替え

				// response
				final Gson gson = new GsonBuilder()
						.serializeNulls() // 値がnullの場合も出力
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // 日付フォーマットをISO 8601に
						.create();
				final String responseJson = gson.toJson(orders);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json; charset=UTF-8");

				final var writer = response.getWriter();
				writer.write(responseJson);
				writer.flush();
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final String pathInfo = request.getPathInfo();

		// /api/v1/customers/checkout エンドポイント
		if (pathInfo != null && pathInfo.matches("/checkout/?")) {
			try {
				// deviceを取得
				final DeviceDTO device = (DeviceDTO) request.getSession()
						.getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);

				// 顧客を特定
				final CustomerService customerService = new CustomerService();
				final CustomerDTO customer = customerService.getAllocatedCustomerByDevice(device);

				// forceパラメータを取得
				final String forceParam = request.getParameter("force");
				final boolean force = "true".equals(forceParam);

				// 会計対象ステータスに変更
				customerService.proceedToCheckout(customer.getCustomerId(), force);

				// 成功レスポンス
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write("{\"message\":\"Customer status updated to checkout target\"}");

			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid device_id format");
			} catch (IllegalStateException e) {
				response.sendError(HttpServletResponse.SC_CONFLICT);
			} catch (EntityNotFoundException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
			}
		} else {
			// 未対応のエンドポイント
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			Gson gson = new Gson();

			try {
				// Content-Type検証
				final String contentType = request.getContentType();
				if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
					response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
							"Content-Type must be application/json");
					return;
				}

				// JSONをDTOに直接パース（bodyに対するvalidationはdtoフィールドに対して行う）
				final RequestNewCustomerDTO requestBody = gson.fromJson(request.getReader(),
						RequestNewCustomerDTO.class);
				if (requestBody == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
					return;
				}

				// バリデーション
				if (requestBody.customerCount() == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "customerCount is required");
					return;
				}
				if (requestBody.customerCount() <= 0) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"customerCount must be greater than 0");
					return;
				}

				// 顧客登録
				CustomerService customerService = new CustomerService();
				final NewCustomerDTO newCustomer = customerService.registerCustomer(requestBody.customerCount());

				if (newCustomer == null) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register customer");
					return;
				}

				// レスポンス返却
				ResponseCustomerDTO responseBody = new ResponseCustomerDTO(newCustomer.getCustomerId().toString(),
						newCustomer.getCustomerCount());
				String responseJson = gson.toJson(responseBody);
				response.setStatus(HttpServletResponse.SC_CREATED);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);
			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (Exception e) {
				e.printStackTrace(); // ログ出力
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
			}
		} else {
			// 未対応のエンドポイント
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	// リクエストボディ用DTO（必須フィールド検証のためInteger）
	private record RequestNewCustomerDTO(Integer customerCount) {
	}

	// レスポンス用DTO
	private record ResponseCustomerDTO(String customerId, int customerCount) {
	}
}
