package servlet;

import java.io.IOException;
import java.security.MessageDigest;

import database.EntityNotFoundException;
import filter.AdminAuthenticationFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.UserDTO;
import model.service.UserService;
import util.PasswordHashUtil;

/**
 * Servlet implementation class AdminLoginServlet
 */
@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/jsp/AdminLogin.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String username = request.getParameter("username");
		String inputPassword = request.getParameter("password");

		// バリデーション
		if (username == null || username.trim().isEmpty()) {
			request.setAttribute("errorMessage", "管理者IDを入力してください。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminLogin.jsp").forward(request, response);
			return;
		}

		if (inputPassword == null || inputPassword.trim().isEmpty()) {
			request.setAttribute("errorMessage", "パスワードを入力してください。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminLogin.jsp").forward(request, response);
			return;
		}
		// ユーザー情報の取得
		UserService usersService = new UserService();
		try {
			// ユーザが存在しない場合はEntityNotFoundExceptionがスローされる
			UserDTO userDTO = usersService.getUserByUsername(username);

			byte[] inputHash = PasswordHashUtil.hash(inputPassword, userDTO.getSalt());
			if (MessageDigest.isEqual(inputHash, userDTO.getPasswordHash())) {
				// 認証成功
				request.getSession().setAttribute(AdminAuthenticationFilter.ATTR_ADMIN_USER, userDTO);
				response.sendRedirect(request.getContextPath() + "/manage/");
			} else {
				// 認証失敗
				request.setAttribute("errorMessage", "管理者IDまたはパスワードが正しくありません。");
				request.getRequestDispatcher("/WEB-INF/jsp/AdminLogin.jsp").forward(request, response);
			}
		} catch (EntityNotFoundException e) {
			// ユーザーが存在しない場合
			request.setAttribute("errorMessage", "管理者IDまたはパスワードが正しくありません。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminLogin.jsp").forward(request, response);
			return;
		} catch (RuntimeException e) {
			// データアクセス例外などが発生した場合
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "サーバーエラーが発生しました。");
		}

	}

}
