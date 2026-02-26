package servlet.api.v1;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import database.DataAccessException;
import database.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.MenuDTO;
import model.dto.NewMenuDTO;
import model.dto.OptionDTO;
import model.service.MenuService;
import model.service.OptionService;

/**
 * Servlet implementation class TableServlet
 */
@WebServlet({ "/api/v1/menus", "/api/v1/menus/*" })
public class MenuApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();

		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			try {
				final Gson gson = new GsonBuilder()
						.serializeNulls()
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
						.create();
				MenuService menuService = new MenuService();

				// get available menu list
				List<MenuDTO> menuList = menuService.getAvailableMenu();

				// build json response
				final String responseJson = gson.toJson(menuList);

				// Send response
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else if (pathInfo.matches("/-?\\d+/options/?")) {
			// /api/v1/menus/{id}/options - 特定メニューのオプション一覧取得
			final String idStr = pathInfo.replaceAll("^/(-?\\d+)/options/?$", "$1");

			try {
				final int menuId = Integer.parseInt(idStr);

				final OptionService optionService = new OptionService();
				final List<OptionDTO> options = optionService.findOptionsByMenuId(menuId);

				final Gson gson = new GsonBuilder()
						.serializeNulls()
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
						.create();

				final String responseJson = gson.toJson(options);

				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid menu ID format");
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else if (pathInfo.matches("/-?\\d+/?")) {
			// パスからメニューIDを抽出
			final String idStr = pathInfo.substring(1).replaceAll("/", "");

			try {
				final int menuId = Integer.parseInt(idStr);

				final MenuService menuService = new MenuService();
				final MenuDTO menu = menuService.getMenuById(menuId);

				final Gson gson = new GsonBuilder()
						.serializeNulls()
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
						.create();

				final String responseJson = gson.toJson(menu);

				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);

			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid menu ID format");
			} catch (EntityNotFoundException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Menu not found");
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		final var pathInfo = request.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			Gson gson = new Gson();
			NewMenuDTO newMenu;
			try {
				newMenu = gson.fromJson(request.getReader(), NewMenuDTO.class);

				// Validate request body
				if ((newMenu.getName() == null || newMenu.getName().isEmpty()
						|| newMenu.getCategoryId() == 0
						|| newMenu.getImage() == null || newMenu.getImage().isEmpty()
						|| newMenu.getPrice() == 0
						|| newMenu.getPrice() >= 1_0000_0000)) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}

				// Register new menu
				final MenuService menuService = new MenuService();
				final MenuDTO insertedMenu = menuService.registerMenu(newMenu);

				// 結果確認
				if (insertedMenu == null) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				// レスポンス返却
				String json = gson.toJson(insertedMenu);
				response.setStatus(HttpServletResponse.SC_CREATED);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(json);
			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}

		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if ("PATCH".equalsIgnoreCase(request.getMethod())) {
			doPatch(request, response);
		} else {
			super.service(request, response);
		}
	}

	private void doPatch(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();

		if (pathInfo != null && pathInfo.matches("/-?\\d+/?")) {

			// パスからメニューIDを抽出
			final String idStr = pathInfo.substring(1).replaceAll("/", "");

			try {
				final int menuId = Integer.parseInt(idStr);

				final Gson gson = new Gson();
				final NewMenuDTO patchData;
				patchData = gson.fromJson(request.getReader(), NewMenuDTO.class);

				// リクエストボディのバリデーション
				if (patchData == null || patchData.getName() == null || patchData.getName().isEmpty()
						|| patchData.getCategoryId() == 0
						|| patchData.getImage() == null || patchData.getImage().isEmpty()
						|| patchData.getPrice() == 0 || patchData.getPrice() >= 1_0000_0000) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}

				// メニュー更新処理
				final MenuService menuService = new MenuService();
				final int newId = menuService.updateMenu(menuId, patchData);

				// 更新後のメニュー情報を取得してレスポンスとして返す
				final MenuDTO updatedMenu = menuService.getMenuById(newId);
				final String responseJson = gson.toJson(updatedMenu);

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);
				return;
			} catch (JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
				return;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid menu ID");
				return;
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Menu not found");
				return;
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			try {
				// Parse JSON request body to extract menu IDs
				Gson gson = new Gson();
				DeleteMenuRequestRecord requestBody = gson.fromJson(request.getReader(), DeleteMenuRequestRecord.class);
				List<Integer> menuIds = requestBody.ids();
				if (menuIds == null || menuIds.isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required field: \"MenuIds\"");
					return;
				}

				MenuService menuService = new MenuService();
				int deletedCount = menuService.deleteMenus(menuIds); // 削除されたメニューの数を取得
				int alreadyDeletedCount = menuIds.size() - deletedCount; // 既に削除されていたメニューの数を計算

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write("{"
						+ "\"requestCount\":\"" + menuIds.size() + "\","
						+ "\"deletedCount\":" + deletedCount + ","
						+ "\"alreadyDeletedCount\":" + alreadyDeletedCount
						+ "}");

			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (DataAccessException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting menus");
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private record DeleteMenuRequestRecord(List<Integer> ids) {
	}

}
