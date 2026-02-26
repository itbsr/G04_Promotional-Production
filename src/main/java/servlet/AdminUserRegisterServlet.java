package servlet;

import java.io.IOException;

import database.DataAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.UserService;

/**
 * Servlet implementation class AdminUserRegisterServlet
 */
@WebServlet("/manage/AdminUserRegisterServlet")
public class AdminUserRegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/jsp/AdminUserRegister.jsp").forward(request, response);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			request.setCharacterEncoding("UTF-8");

			String username = request.getParameter("username");
			String password = request.getParameter("password");

			// バリデーション
			if (username == null || username.trim().isEmpty()) {
				request.setAttribute("errorMessage", "管理者IDを入力してください。");
				doGet(request, response);
				return;
			}
			if (password == null || password.trim().isEmpty()) {
				request.setAttribute("errorMessage", "パスワードを入力してください。");
				doGet(request, response);
				return;
			}
			// パスワード長の最小値チェック（例：8文字以上）
			if (password.length() < 5) {
				request.setAttribute("errorMessage", "パスワードは5文字以上である必要があります。");
				doGet(request, response);
				return;
			}
			// ユーザー名のフォーマットチェック（例：英数字のみ）
			if (!username.matches("^[a-zA-Z0-9]+$")) {
				request.setAttribute("errorMessage", "管理者IDは英数字のみで構成されている必要があります。");
				doGet(request, response);
				return;
			}

			// 管理者ユーザー登録
			final UserService usersService = new UserService();
			usersService.createAdminUser(username, password);

			// 登録成功
			request.setAttribute("successMessage", "管理者ユーザー登録が完了しました。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminUserRegister.jsp").forward(request, response);

		} catch (IllegalArgumentException e) {
			// 既存ユーザーが存在する場合
			request.setAttribute("errorMessage", "指定された管理者IDは既に存在します。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminUserRegister.jsp").forward(request, response);
		} catch (DataAccessException e) {
			// データアクセス例外が発生した場合
			e.printStackTrace();
			request.setAttribute("errorMessage", "登録処理中にエラーが発生しました。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminUserRegister.jsp").forward(request, response);
		} catch (Exception e) {
			// 予期しない例外が発生した場合
			e.printStackTrace();
			request.setAttribute("errorMessage", "予期しないエラーが発生しました。");
			request.getRequestDispatcher("/WEB-INF/jsp/AdminUserRegister.jsp").forward(request, response);
			return;
		}
	}
}
