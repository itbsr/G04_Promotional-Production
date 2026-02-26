package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 店舗管理者用 売上管理画面を表示するサーブレット
 */
@WebServlet("/manage/sales")
public class SalesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * 売上管理画面を表示する
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 売上管理画面を表示
		request.getRequestDispatcher("/WEB-INF/html/sales.html")
				.forward(request, response);
	}
}
