package servlet.api.v1;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import database.DataAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.CategoryDTO;
import model.service.CategoryService;

@WebServlet({ "/api/v1/categories", "/api/v1/categories/*" })
public class CategoryApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final var pathInfo = request.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			try {
				final Gson gson = new GsonBuilder()
						.serializeNulls()
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
						.create();
				CategoryService categoryService = new CategoryService();

				// get category list
				List<CategoryDTO> categoryList = categoryService.getAllCategories();

				// build json response
				final String responseJson = gson.toJson(categoryList);

				// Send response
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write(responseJson);
			} catch (DataAccessException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else if (pathInfo.matches("/\\d+/?")) {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
