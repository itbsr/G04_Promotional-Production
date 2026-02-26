package servlet;

import java.io.IOException;
import java.util.UUID;

import database.DeviceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.NewDeviceDTO;
import model.service.DeviceService;
import model.service.TableService;
import util.UuidUtils;
import viewmodel.ManageDeviceViewModel;

/**
 * Servlet implementation class ManageDeviceServlet
 */
@WebServlet("/manage/devices")
public class ManageDeviceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DeviceService deviceService = new DeviceService();
		TableService tableService = new TableService();
		ManageDeviceViewModel viewModel = new ManageDeviceViewModel(
				deviceService.getAllDeviceTypes(),
				deviceService.getAllDevices(),
				tableService.getAllTables());

		request.setAttribute("vm", viewModel);
		request.getRequestDispatcher("/WEB-INF/jsp/ManageDevice.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// Get parameters
		final String sDeviceId = request.getParameter("deviceId");
		String sDeviceName = request.getParameter("deviceName");
		String sDeviceTypeId = request.getParameter("deviceTypeId");
		final String sTableId = request.getParameter("tableId");

		// Validate parameters
		if (sDeviceId == null || sDeviceId.isEmpty()
				|| !sDeviceId.matches("\\d+") && !sDeviceId.matches(UuidUtils.REGEX_UUID_ANY_VERSION)
				|| sDeviceName == null || sDeviceName.isEmpty()
				|| sDeviceTypeId == null || sDeviceTypeId.isEmpty()
				|| !sDeviceTypeId.matches("\\d+")) {
			request.setAttribute("message", "デバイス情報の未入力があります");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doGet(request, response);
			return;
		}

		if (sTableId != null && !sTableId.isEmpty()
				&& !sDeviceTypeId.equals(String.valueOf(DeviceDAO.DEVICE_TYPE_ID_TABLE))) {
			request.setAttribute("message", "テーブルIDは卓上端末の場合のみ指定可能です");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doGet(request, response);
			return;
		}

		try {
			UUID uuidDeviceId;
			if (sDeviceId.matches("\\d+"))
				uuidDeviceId = new UUID(0, Long.parseLong(sDeviceId));
			else
				uuidDeviceId = UUID.fromString(sDeviceId);

			int nDeviceTypeId = Integer.parseInt(sDeviceTypeId);
			Integer nTableId = sTableId != null && !sTableId.isEmpty() ? Integer.parseInt(sTableId) : null;

			NewDeviceDTO device = new NewDeviceDTO(uuidDeviceId, sDeviceName, nDeviceTypeId, nTableId);
			DeviceService deviceService = new DeviceService();
			deviceService.registerDevice(device);

			request.setAttribute("message", "デバイス登録が完了しました");
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message", "デバイス登録に失敗しました: " + e.getMessage());
		}

		doGet(request, response);
	}

}
