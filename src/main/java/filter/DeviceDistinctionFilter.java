package filter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.UUID;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import database.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DeviceService;
import util.UuidUtils;

public class DeviceDistinctionFilter extends HttpFilter {
	public static final String ATTR_DEVICE_ID = "device_id";
	private static final String PARAM_DEVICE_ID = "device_id";

	DeviceService deviceService;

	public DeviceDistinctionFilter() {
		this(new DeviceService());
	}

	public DeviceDistinctionFilter(DeviceService deviceService) {
		super();
		this.deviceService = deviceService;
	}

	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");

		if (request.getRequestURI().equals("/G04PromotionalProduction/manifest.json")) {
			chain.doFilter(request, response);
			return;
		}

		if (request.getParameter(PARAM_DEVICE_ID) != null) {
			request.getSession().removeAttribute(ATTR_DEVICE_ID);
		}

		if (request.getSession().getAttribute("device_id") == null) {
			try {
				X509Certificate[] certs = (X509Certificate[]) request
						.getAttribute("jakarta.servlet.request.X509Certificate");

				// Attempt client certificate authentication first
				if (certs != null && certs.length > 0) {
					X509Certificate clientCert = certs[0];
					String dn = clientCert.getSubjectX500Principal().getName();
					LdapName ldapName = new LdapName(dn);

					String uid = null;
					for (Rdn rdn : ldapName.getRdns())
						if ("UID".equalsIgnoreCase(rdn.getType()))
							uid = rdn.getValue().toString();

					if (uid != null && uid.matches(UuidUtils.REGEX_UUID_ANY_VERSION)) {
						final var deviceId = UUID.fromString(uid);
						final var device = deviceService.getDeviceById(deviceId);

						if (device != null) {
							request.getSession().setAttribute(ATTR_DEVICE_ID, device);
							System.out.println(
									"Authorized session from \"" + request.getRemoteAddr() + "\" (" + uid
											+ ") by client certificate authentication");
							chain.doFilter(request, response);
							return;
						}
					}
				}

				// Get parameters
				var parameterMap = new HashMap<>(request.getParameterMap());
				final String[] sDeviceIdParams = parameterMap.get(PARAM_DEVICE_ID);

				// parse device id
				UUID uuidDeviceId;
				if (sDeviceIdParams == null || sDeviceIdParams[0].isEmpty()) {
					throw new InvalidParameterException();
				} else if (sDeviceIdParams[0].matches(UuidUtils.REGEX_UUID_ANY_VERSION)) {
					uuidDeviceId = UUID.fromString(sDeviceIdParams[0]);
				} else if (sDeviceIdParams[0].matches("\\d+")) {
					uuidDeviceId = new UUID(0, Integer.parseInt(sDeviceIdParams[0]));
				} else {
					throw new InvalidParameterException();
				}

				// Validate device id
				final var device = deviceService.getDeviceById(uuidDeviceId);
				if (device == null) {
					System.out.println("Unauthorized device access attempt from " + request.getRemoteAddr()
							+ " with device_id: " + sDeviceIdParams[0]);
					throw new EntityNotFoundException("Device not found: " + uuidDeviceId);
				}

				// Set DeviceId to session scope
				System.out.println(
						"Authorized session from \"" + request.getRemoteAddr() + "\" (" + device.getId()
								+ ") by \"" + PARAM_DEVICE_ID + "\" parameter");
				request.getSession().setAttribute(ATTR_DEVICE_ID, device);

				// Redirect to the same URL without device_id parameter
				// String requestURI = request.getRequestURI();
				// parameterMap.remove(PARAM_DEVICE_ID);

				// StringBuilder redirectURL = new StringBuilder(requestURI);
				// if (!parameterMap.isEmpty()) {
				// 	redirectURL.append("?");
				// 	parameterMap.forEach((key, values) -> {
				// 		for (String value : values) {
				// 			redirectURL.append(key).append("=").append(value).append("&");
				// 		}
				// 	});
				// 	// Remove trailing '&'
				// 	redirectURL.setLength(redirectURL.length() - 1);
				// }
				// response.sendRedirect(redirectURL.toString());
				// return;
			} catch (InvalidParameterException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"A client certificate containing a device ID or a " + PARAM_DEVICE_ID
								+ " parameter is required (value must be an integer or UUID format)");
				return;
			} catch (EntityNotFoundException e) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
