package servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/manage/UploadImageServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class UploadImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String UPLOAD_DIR = "menuImages";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uploadPath = getUploadPath(request);
		List<String> images = getUploadedImages(uploadPath);
		request.setAttribute("images", images);
		request.getRequestDispatcher("/WEB-INF/jsp/uploadMenuImage.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uploadPath = getUploadPath(request);
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}

		try {
			Part filePart = request.getPart("image");
			String fileName = getFileName(filePart);
			if (fileName != null && !fileName.isEmpty()) {
				filePart.write(uploadPath + File.separator + fileName);
				request.setAttribute("message", "アップロード成功: " + uploadDir.toString() + ' ' + fileName);
			} else {
				request.setAttribute("error", "ファイルが選択されていません。");
			}
		} catch (Exception e) {
			request.setAttribute("error", "アップロード失敗: " + e.getMessage());
			e.printStackTrace();
		}

		// 再度画像リストを取得して表示
		response.sendRedirect("UploadImageServlet");
	}

	String getUploadPath(HttpServletRequest request) {
		String applicationPath = request.getServletContext().getRealPath("");
		return applicationPath + File.separator + UPLOAD_DIR;
	}

	List<String> getUploadedImages(String path) {
		List<String> images = new ArrayList<>();
		File folder = new File(path);
		if (folder.exists() && folder.isDirectory()) {
			for (File file : folder.listFiles()) {
				if (file.isFile()) {
					images.add(file.getName());
				}
			}
		}
		return images;
	}

	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		for (String token : contentDisp.split(";")) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
}
