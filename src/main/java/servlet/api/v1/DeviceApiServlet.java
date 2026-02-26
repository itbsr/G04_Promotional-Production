package servlet.api.v1;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import database.DataAccessException;
import database.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DeviceService;

/**
 * Servlet implementation class DeviceApiServlet
 */
@WebServlet({ "/api/v1/devices", "/api/v1/devices/*" })
public class DeviceApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			try {
				final String contentType = request.getContentType();
				if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
					response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
							"Content-Type must be application/json");
					return;
				}

				final Gson gson = new Gson();
				final DeleteDevicesRequestRecord requestRecord = gson.fromJson(request.getReader(),
						DeleteDevicesRequestRecord.class);
				if (requestRecord == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "request body is null");
					return;
				}
				final List<UUID> deviceIds = requestRecord.deviceIds();
				// バリデーション
				if (deviceIds == null || deviceIds.isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The \"deviceIds\" field is required");
					return;
				}
				// デバイス削除
				DeviceService deviceService = new DeviceService();
				deviceService.deleteDevices(deviceIds);

				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;
			} catch (JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (NumberFormatException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid device ID");
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"device not found: " + e.getEntityId().toString());
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	private record DeleteDevicesRequestRecord(List<UUID> deviceIds) {
	}
}
