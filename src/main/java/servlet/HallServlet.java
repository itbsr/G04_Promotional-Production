package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ホール担当者用画面を表示するサーブレット
 */
@WebServlet("/hall")
public class HallServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * ホール管理画面を表示する
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// ホール管理画面を表示
		request.getRequestDispatcher("WEB-INF/html/hall.html")
				.forward(request, response);
	}
}
