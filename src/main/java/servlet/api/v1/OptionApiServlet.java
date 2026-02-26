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
import model.dto.NewOptionDTO;
import model.dto.OptionDTO;
import model.service.OptionService;

/**
 * オプション情報を提供するAPIサーブレット
 */
@WebServlet({ "/api/v1/options", "/api/v1/options/*" })
public class OptionApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();

		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			try {
				// 必要なインスタンスを作成
				final Gson gson = new GsonBuilder()
						.serializeNulls() // 値がnullの場合も出力
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // 日付フォーマットをISO 8601に
						.create();
				OptionService optionService = new OptionService();

				// オプションリンクJSON
				List<OptionDTO> optionLinksList = optionService.findOptionsLinkedToActiveMenus();

				final String responseJson = gson.toJson(optionLinksList);

				// レスポンス
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else if (pathInfo.matches("/\\d+/?")) {
			// Extract option ID from the path
			String[] pathParts = pathInfo.split("/");
			final int optionId = Integer.parseInt(pathParts[1]);

			try {
				final OptionService optionService = new OptionService();
				final OptionDTO option = optionService.findOptionById(optionId);

				// Convert to JSON
				final Gson gson = new GsonBuilder()
						.serializeNulls() // 値がnullの場合も出力
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // 日付フォーマットをISO 8601に
						.create();
				final String responseBody = gson.toJson(option);

				// Send response
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseBody);
			} catch (EntityNotFoundException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "オプションが見つかりません");
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			final Gson gson = new Gson();
			final NewOptionDTO newOption;
			try {
				newOption = gson.fromJson(request.getReader(), NewOptionDTO.class);

				// バリデーション
				if (newOption == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
					return;
				}
				if (newOption.getName() == null || newOption.getName().isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "name is required");
					return;
				}
				// if (newOption.getMenuId() <= 0) {
				// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "menuId must be
				// greater than 0");
				// return;
				// }
				if (newOption.getPrice() < 0) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "price must be 0 or greater");
					return;
				}
				if (newOption.getPrice() >= 1_0000_0000) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "price must be less than 100000000");
					return;
				}

				// オプション登録
				final OptionService optionService = new OptionService();
				optionService.registerOption(newOption);

				response.setStatus(HttpServletResponse.SC_CREATED);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write("{\"message\":\"Option created successfully\"}");

			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
			} catch (Exception e) {
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
			// パスからオプションIDを抽出
			final String idStr = pathInfo.substring(1).replaceAll("/", "");

			try {
				final int optionId = Integer.parseInt(idStr);

				final Gson gson = new Gson();
				final NewOptionDTO patchData = gson.fromJson(request.getReader(), NewOptionDTO.class);

				// リクエストボディのバリデーション
				if (patchData == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
					return;
				}
				if (patchData.getName() == null || patchData.getName().isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "name is required");
					return;
				}
				// if (patchData.getMenuId() <= 0) {
				// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "menuId must be
				// greater than 0");
				// return;
				// }
				if (patchData.getPrice() < 0) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "price must be 0 or greater");
					return;
				}
				if (patchData.getPrice() >= 1_0000_0000) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "price must be less than 100000000");
					return;
				}

				// オプション更新処理
				final OptionService optionService = new OptionService();
				optionService.updateOption(optionId, patchData);

				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;

			} catch (JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
				return;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid option ID");
				return;
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Option not found");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
		}
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			try {
				Gson gson = new Gson();
				DeleteOptionRequestRecord deleteRequest = gson.fromJson(request.getReader(),
						DeleteOptionRequestRecord.class);
				List<Integer> optionIds = deleteRequest.optionIds();
				if (optionIds == null || optionIds.isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "optionIds is required");
					return;
				}

				OptionService optionService = new OptionService();
				int deletedCount = optionService.deleteOptions(optionIds); // 削除されたオプションの数を取得
				int alreadyDeletedCount = optionIds.size() - deletedCount; // 既に削除されていたオプションの数を計算

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write("{"
						+ "\"requestCount\":\"" + optionIds.size() + "\","
						+ "\"deletedCount\":" + deletedCount + ","
						+ "\"alreadyDeletedCount\":" + alreadyDeletedCount
						+ "}");
			} catch (JsonSyntaxException | JsonIOException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
				return;
			} catch (DataAccessException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting options");
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private record DeleteOptionRequestRecord(List<Integer> optionIds) {
	}
}
