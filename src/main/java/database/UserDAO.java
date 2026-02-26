package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.dto.NewUserDTO;
import model.dto.UserDTO;

public class UserDAO {
	private DBManager dbManager;

	/**
	 * デフォルトコンストラクタ
	 */
	public UserDAO() {
		this(DBManager.getInstance());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param dbManager
	 */
	public UserDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	/**
	 * 管理者ユーザーを登録するメソッド
	 *
	 * @param user 管理者ユーザー情報を含むUserDTOオブジェクト
	 */
	public void registerAdminUser(NewUserDTO user) {
		final String sql = "INSERT INTO users (username, password_hash, salt) values (?, ?, ?)";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setString(1, user.getUsername());
			stmt.setBytes(2, user.getPasswordHash());
			stmt.setBytes(3, user.getSalt());
			final int result = stmt.executeUpdate();
			if (result == 0) {
				throw new DataAccessException("DBサーバーエラーにより管理者ユーザーの登録に失敗しました", null);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataAccessException("DBサーバーエラー(" + e.getMessage() + ")", e);
		}
	}

	/**
	 * ユーザー名に基づいてユーザー情報を取得するメソッド
	 *
	 * @param username 取得するユーザーのユーザー名
	 * @return UserDTO ユーザー情報を含むUserDTOオブジェクト
	 */
	public UserDTO getUserByUsername(String username) {
		final String sql = "SELECT user_id, username, password_hash, salt FROM users WHERE username = ?";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);) {

			stmt.setString(1, username);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					UserDTO user = new UserDTO(
							rs.getInt("user_id"),
							rs.getString("username"),
							rs.getBytes("password_hash"),
							rs.getBytes("salt"));
					return user;
				} else {
					throw new EntityNotFoundException("ユーザーが見つかりませんでした");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataAccessException("DBサーバーエラー(" + e.getMessage() + ")", e);
		}
	}
}
