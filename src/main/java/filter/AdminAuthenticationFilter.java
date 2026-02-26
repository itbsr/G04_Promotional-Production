package filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAuthenticationFilter extends HttpFilter {
	public static final String ATTR_ADMIN_USER = "adminUser";

	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request.getSession().getAttribute(ATTR_ADMIN_USER) == null) {
			System.out.println("AdminAuthenticationFilter: \"" + request.getRemoteAddr()
					+ "\"からの未認証のアクセスを検出しました。ログインページへリダイレクトします。");
			response.sendRedirect(request.getContextPath() + "/AdminLoginServlet");
			return;
		} else {
			chain.doFilter(request, response);
			System.out.println("AdminAuthenticationFilter: \"" + request.getRemoteAddr()
					+ "\"からの認証済みのアクセスを許可しました。");
			return;
		}
	}
}
