package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import model.dto.MenuDTO;
import model.dto.OptionDTO;
import model.dto.OrderDTO;
import model.service.CustomerService;
import model.service.OrderService;
import util.UuidUtils;
import viewmodel.CheckoutViewModel;

@WebServlet("/Checkout")
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final DeviceDTO device = (DeviceDTO) request.getSession().getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);
		if (device.getDeviceTypeId() != DeviceDTO.DeviceTypes.REGISTER.getId()
				&& device.getDeviceTypeId() != DeviceDTO.DeviceTypes.TEST_PRIVILEGE.getId()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "この端末では会計処理を行えません。");
			return;
		}

		super.service(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String customerId = request.getParameter("customerId");
		if (customerId == null || customerId.trim().isEmpty()) {
			request.setAttribute("message", "顧客IDを入力してください。");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
			return;
		}
		UUID uuidCustomerId;
		if (customerId.matches("\\d+")) {
			// 仮仕様として数値も許可。UUIDv8形式。
			// 詳細はCustomerService.registerCustomer()内のコメントを参照。
			uuidCustomerId = new UUID(
					0x8000,
					(0x8000_0000_0000_0000L | Integer.parseInt(customerId)));
		} else if (customerId.matches(UuidUtils.REGEX_UUID_ANY_VERSION)) {
			uuidCustomerId = UUID.fromString(customerId);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: customerId (UUID or int32)");
			return;
		}
		try {
			final CustomerService customerService = new CustomerService();

			// 支払い完了のAPI呼び出しが来たらpaid_atを更新して終了
			String action = request.getParameter("action");
			if ("complete".equals(action)) {
				if (!customerService.isEligibleForCheckout(uuidCustomerId)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().write("このお客様IDは会計待ち状態ではありません。");
					return;
				}
				customerService.markCustomerPaid(uuidCustomerId);
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}

			// 顧客IDが有効な場合、注文履歴を取得して表示
			if (customerService.isEligibleForCheckout(uuidCustomerId)) {
				final OrderService orderService = new OrderService();

				// 顧客情報を取得
				final CustomerDTO customer = customerService.getCustomerById(uuidCustomerId);

				// 注文情報、メニュー情報、オプション情報を取得
				final Map<Integer, MenuDTO> menus = new HashMap<>();
				final Map<Integer, OptionDTO> options = new HashMap<>();
				final List<OrderDTO> orders = orderService.getOrdersByCustomerId(uuidCustomerId, menus, options, false);

				// 各注文の合計金額を計算
				final List<Integer> orderTotalPrices = new ArrayList<>();
				for (final var order : orders) {
					int totalPrice = 0;
					// menu price
					final var menu = menus.get(order.getMenuId());
					totalPrice += menu.getPrice();

					// options price
					for (final var optionId : order.getOptionIds()) {
						final var option = options.get(optionId);
						if (option != null) {
							totalPrice += option.getPrice();
						}
					}
					orderTotalPrices.add(totalPrice);
				}

				// ViewModelに詰めてJSPへ渡す
				final CheckoutViewModel viewmodel = new CheckoutViewModel(
						customer,
						orders,
						menus,
						options,
						orderTotalPrices);

				request.setAttribute("vm", viewmodel);
				request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
			} else {
				request.setAttribute("message", "このお客様IDは会計待ち状態ではありません。");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
			}

		} catch (EntityNotFoundException e) {
			// エンティティが見つからない場合（顧客、メニュー、オプション等）
			request.setAttribute("message", "指定された情報が見つかりません: " + e.getMessage());
			e.printStackTrace();
			getServletContext().log("Entity not found in CheckoutServlet", e);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
		} catch (DataAccessException e) {
			// データベースアクセスエラー
			request.setAttribute("message", "データベースエラーが発生しました。");
			e.printStackTrace();
			getServletContext().log("Database access error in CheckoutServlet", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			// UUID変換エラー
			request.setAttribute("message", "顧客IDの形式が正しくありません。");
			e.printStackTrace();
			getServletContext().log("Invalid customer ID format in CheckoutServlet", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
		} catch (Exception e) {
			// その他の予期しないエラー
			request.setAttribute("message", "予期しないエラーが発生しました。");
			e.printStackTrace();
			getServletContext().log("Unexpected error in CheckoutServlet", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.getRequestDispatcher("WEB-INF/jsp/checkout.jsp").forward(request, response);
		}
	}
}
