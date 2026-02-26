package model.service;

import database.EntityNotFoundException;
import database.UserDAO;
import model.dto.NewUserDTO;
import model.dto.UserDTO;
import util.PasswordHashUtil;

public class UserService {
	public void createAdminUser(String username, String password) {
		// 既存ユーザー確認
		try {
			if (new UserDAO().getUserByUsername(username) != null) {
				throw new IllegalArgumentException("指定されたユーザー名は既に使用されています。");
			}
		} catch (EntityNotFoundException e) {// ユーザーが存在しない場合は登録処理を続行
			byte[] salt = PasswordHashUtil.generateSalt();
			byte[] passwordHash = PasswordHashUtil.hash(password, salt);
			NewUserDTO user = new NewUserDTO(
					username,
					passwordHash,
					salt);

			UserDAO userDAO = new UserDAO();
			userDAO.registerAdminUser(user);
		}
	}

	public UserDTO getUserByUsername(String username) {
		UserDAO userDAO = new UserDAO();
		return userDAO.getUserByUsername(username);
	}
}
