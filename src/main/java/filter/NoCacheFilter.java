package filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NoCacheFilter extends HttpFilter {
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			// Detect page (HTML), CSS and JS requests to disable caching
			String dest = request.getHeader("Sec-Fetch-Dest");
			String mode = request.getHeader("Sec-Fetch-Mode");
			String accept = request.getHeader("Accept");
			String uri = request.getRequestURI();

			boolean isDocument = dest != null && dest.equalsIgnoreCase("document");
			boolean isNavigate = mode != null && mode.equalsIgnoreCase("navigate");
			boolean acceptsHtml = accept != null
					&& (accept.contains("text/html") || accept.contains("application/xhtml+xml"));
			boolean uriHtml = uri != null && (uri.endsWith(".html") || uri.endsWith(".xhtml"));

			boolean isStyle = dest != null && dest.equalsIgnoreCase("style");
			boolean acceptsCss = accept != null && accept.contains("text/css");
			boolean uriCss = uri != null && uri.endsWith(".css");

			boolean isScript = dest != null && dest.equalsIgnoreCase("script");
			boolean acceptsJs = accept != null
					&& (accept.contains("application/javascript") || accept.contains("text/javascript"));
			boolean uriJs = uri != null && (uri.endsWith(".js") || uri.endsWith(".mjs"));

			boolean targetHtml = isDocument || isNavigate || acceptsHtml || uriHtml;
			boolean targetCss = isStyle || acceptsCss || uriCss;
			boolean targetJs = isScript || acceptsJs || uriJs;

			if (targetHtml || targetCss || targetJs) {
				response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "0");
			}
		} finally {
			chain.doFilter(request, response);
		}
	}
}
