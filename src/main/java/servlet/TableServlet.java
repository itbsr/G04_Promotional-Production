package servlet;

import java.io.IOException;
import java.util.UUID;

import database.DataAccessException;
import database.EntityNotFoundException;
import filter.DeviceDistinctionFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.DeviceDTO;
import model.service.CustomerService;
import model.service.DeviceService;
import util.UuidUtils;

/**
 * Servlet implementation class TableServlet
 */
@WebServlet("/Table")
public class TableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final DeviceDTO device = (DeviceDTO) request.getSession().getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);
		final boolean isCustomerAllocated = new DeviceService().isCustomerAllocated(device.getId());

		if (isCustomerAllocated)
			request.getRequestDispatcher("WEB-INF/html/table.html").forward(request, response);
		else
			request.getRequestDispatcher("/WEB-INF/html/enterCustomerId.html").forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			final DeviceDTO device = (DeviceDTO) request.getSession()
					.getAttribute(DeviceDistinctionFilter.ATTR_DEVICE_ID);
			final boolean isCustomerAllocated = new DeviceService().isCustomerAllocated(device.getId());
			if (isCustomerAllocated) {
				response.sendError(HttpServletResponse.SC_CONFLICT);
				return;
			}

			request.setCharacterEncoding("UTF-8");

			// Validate Content-Type
			final String contentType = request.getContentType();
			if (contentType == null || !contentType.contains("application/x-www-form-urlencoded")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Content-Type must be application/x-www-form-urlencoded");
				return;
			}

			// Validate and parse customerId
			final String sCustomerId = request.getParameter("customerId");
			if (sCustomerId == null || sCustomerId.isEmpty()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"parameter customerId is required (UUID or int32)");
				return;
			}

			// Validate and Parse customerId
			UUID uuidCustomerId;
			if (sCustomerId.matches("\\d+")) {
				// 仮仕様として数値も許可。UUIDv8形式。
				// 詳細はCustomerService.registerCustomer()内のコメントを参照。
				uuidCustomerId = new UUID(
						0x8000,
						(0x8000_0000_0000_0000L | Integer.parseInt(sCustomerId)));
			} else if (sCustomerId.matches(UuidUtils.REGEX_UUID_ANY_VERSION)) {
				uuidCustomerId = UUID.fromString(sCustomerId);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: customerId (UUID or int32)");
				return;
			}

			final UUID uuidDeviceId = device.getId();
			new CustomerService().setTableByDeviceId(uuidDeviceId, uuidCustomerId);

			response.sendRedirect("Table");
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (DataAccessException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}
}
