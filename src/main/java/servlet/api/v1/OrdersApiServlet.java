package servlet.api.v1;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import database.DataAccessException;
import database.DeviceDAO;
import database.EntityNotFoundException;
import filter.DeviceDistinctionFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.CustomerDTO;
import model.dto.DeviceDTO;
import model.dto.NewOrderDTO;
import model.dto.OrderDTO;
import model.service.CustomerService;
import model.service.OrderService;
import util.UuidUtils;

/**
 * Servlet implementation class RegisterOrdersApiServlet
 */
@WebServlet({ "/api/v1/orders", "/api/v1/orders/*" })
public class OrdersApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public OrdersApiServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		final var session = request.getSession();
		final var device = (DeviceDTO) session.getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);

		if (pathInfo == null || pathInfo.equals("/")) {
			// statusパラメータを取得
			final String statusParam = request.getParameter("status");

			if (device != null
					&& device.getDeviceTypeId() == DeviceDAO.DEVICE_TYPE_ID_KITCHEN
					|| device.getDeviceTypeId() == DeviceDAO.DEVICE_TYPE_ID_HALL) {
				// キッチンデバイスからのアクセス
				if (statusParam != null && !statusParam.isEmpty()) {
					// statusパラメータが指定されている場合の処理
					try {
						final int statusId = Integer.parseInt(statusParam);

						// 注文一覧を取得
						final OrderService orderService = new OrderService();
						final List<OrderDTO> orders = orderService.getOrdersByStatusId(statusId);

						// レスポンスを作成
						final Gson gson = new GsonBuilder()
								.serializeNulls()
								.create();
						final String responseJson = gson.toJson(orders);

						response.setContentType("application/json; charset=UTF-8");
						response.getWriter().write(responseJson);
						return;
					} catch (NumberFormatException e) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "status must be a valid integer");
						return;
					} catch (DataAccessException e) {
						e.printStackTrace();
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						return;
					}
				} else {
					// キッチンデバイス以外はstatusパラメータでの絞り込みは許可しない
					response.sendError(HttpServletResponse.SC_FORBIDDEN,
							"Access denied: kitchen device required for status filtering");
					return;
				}
			} else {
				// キッチンデバイス以外からのアクセス（卓上端末など）
				try {
					// デバイスが卓上端末かどうかをチェック
					if (device == null || device.getDeviceTypeId() != DeviceDAO.DEVICE_TYPE_ID_TABLE) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN,
								"Access denied: table device required");
						return;
					}

					// 卓上端末から顧客IDを取得
					CustomerService customerService = new CustomerService();
					final CustomerDTO customer = customerService.getAllocatedCustomerByDevice(device);

					// 顧客の注文履歴を取得（顧客向け表示名を使用）
					OrderService orderService = new OrderService();
					final List<OrderDTO> orders = orderService.getOrdersByCustomerId(customer.getCustomerId(), true);

					// レスポンスを返却
					Gson gson = new Gson();
					String json = gson.toJson(orders);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("application/json; charset=UTF-8");
					response.getWriter().write(json);
					return;

				} catch (EntityNotFoundException e) {
					e.printStackTrace(); // ログ出力
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not allocated");
				} catch (Exception e) {
					e.printStackTrace(); // ログ出力
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			Gson gson = new Gson();

			try {
				final RequestNewOrderDTO requestDTO = gson.fromJson(request.getReader(), RequestNewOrderDTO.class);
				final List<NewOrderDTO> newOrder = requestDTO.orders();

				// バリデーション
				if (newOrder == null || newOrder.isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "orders is required");
					return;
				}

				// デバイスID取得
				final var session = request.getSession();
				final var device = (DeviceDTO) session.getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);

				// 注文登録
				OrderService orderService = new OrderService();
				final List<OrderDTO> InsertedOrders = orderService.registerOrder(newOrder, device);

				// 結果確認
				if (InsertedOrders == null) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}

				// レスポンス返却
				InsertedOrdersDTO responseDTO = new InsertedOrdersDTO(InsertedOrders);
				String json = gson.toJson(responseDTO);
				response.setStatus(HttpServletResponse.SC_CREATED);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(json);
			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (Exception e) {
				e.printStackTrace(); // ログ出力
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
			}
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();

		// /api/v1/orders/{orderId}/status パターンをチェック
		if (pathInfo != null && pathInfo.matches("/" + UuidUtils.REGEX_UUID_ANY_VERSION + "/status/?$")) {
			try {
				// orderIdを抽出
				final String orderIdStr = pathInfo.replaceAll("/(" + UuidUtils.REGEX_UUID_ANY_VERSION + ")/status/?$",
						"$1");
				final UUID orderId = UUID.fromString(orderIdStr);

				// リクエストボディから新しいステータスIDを取得
				final Gson gson = new Gson();
				final UpdateStatusRequestDTO requestDTO = gson.fromJson(request.getReader(),
						UpdateStatusRequestDTO.class);

				if (requestDTO == null || requestDTO.statusId() == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "statusId is required");
					return;
				}

				// ステータスを更新
				final OrderService orderService = new OrderService();
				final OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, requestDTO.statusId());

				if (updatedOrder != null) {
					// 成功時のレスポンス
					final String responseJson = new GsonBuilder()
							.serializeNulls()
							.create()
							.toJson(updatedOrder);
					response.setContentType("application/json; charset=UTF-8");
					response.getWriter().write(responseJson);
				} else {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found or invalid status");
				}
			} catch (IllegalArgumentException e) {
				// UUID形式が不正
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	// リクエストボディ用DTO
	private record RequestNewOrderDTO(List<NewOrderDTO> orders) {
	}

	private record InsertedOrdersDTO(List<OrderDTO> orders) {
	}

	private record UpdateStatusRequestDTO(Integer statusId) {
	}

}
