package servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.CategoryDTO;
import model.dto.MenuDTO;
import model.service.CategoryService;
import model.service.MenuService;
import viewmodel.ManageMenuViewModel;

/**
 * Servlet implementation class ManageMenuServlet
 */
@WebServlet("/manage/ManageMenuServlet")
public class ManageMenuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MenuService menuService = new MenuService();
		CategoryService categoryService = new CategoryService();

		List<MenuDTO> manageMenuList = menuService.getMenuAll();
		List<CategoryDTO> categoryList = categoryService.getAllCategories();

		final var uploadImgServlet = new UploadImageServlet();
		String uploadPath = new UploadImageServlet().getUploadPath(request);
		List<String> images = uploadImgServlet.getUploadedImages(uploadPath);

		ManageMenuViewModel viewModel = new ManageMenuViewModel(manageMenuList, categoryList, images);

		request.setAttribute("viewModel", viewModel);
		request.getRequestDispatcher("/WEB-INF/jsp/ManageMenu.jsp").forward(request, response);
	}
}
