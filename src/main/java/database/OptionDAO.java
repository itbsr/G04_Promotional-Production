package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.dto.NewOptionDTO;
import model.dto.OptionDTO;

/**
 * オプション情報のデータアクセスを行うDAOクラス
 */
public class OptionDAO {
	private DBManager dbmanager;

	// Column names for option links
	private static final String COLUMN_MENU_ID = "menu_id";
	private static final String COLUMN_OPTION_ID = "option_id";
	private static final String COLUMN_OPTION_PRICE = "option_price";
	private static final String COLUMN_OPTION_DELETED_AT = "option_deleted_at";
	// Column names for option names
	private static final String COLUMN_OPTION_NAME = "option_name";

	private static final String TABLE_NAME = "options";

	public OptionDAO() {
		this(DBManager.getInstance());
	}

	public OptionDAO(DBManager dbmanager) {
		this.dbmanager = dbmanager;
	}

	/**
	 * 有効なメニューに関連付けられたオプションリンクを取得するメソッド
	 * 削除済みオプションは含めない
	 * 
	 * @return オプションDTOのリスト
	 */
	public List<OptionDTO> findOptionsLinkedToActiveMenus() {
		return findOptionsLinkedToActiveMenus(false);
	}

	/**
	 * 有効なメニューに関連付けられたオプションリンクを取得するメソッド
	 *
	 * @param includeDeleted 削除されたオプションも含めるかどうか
	 * @return オプションDTOのリスト
	 */
	public List<OptionDTO> findOptionsLinkedToActiveMenus(boolean includeDeleted) {
		final List<OptionDTO> optionLinks = new ArrayList<>();
		final String sql = "SELECT "
				+ COLUMN_OPTION_ID + ","
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_NAME + ","
				+ COLUMN_OPTION_PRICE + ","
				+ COLUMN_OPTION_DELETED_AT
				+ " FROM v_options_linked_active_menus";

		try (Connection conn = DBManager.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				optionLinks.add(new OptionDTO(
						rs.getInt(COLUMN_OPTION_ID),
						rs.getInt(COLUMN_MENU_ID),
						rs.getString(COLUMN_OPTION_NAME),
						rs.getInt(COLUMN_OPTION_PRICE),
						rs.getTimestamp(COLUMN_OPTION_DELETED_AT)));
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションリンクの取得に失敗しました", e);
		}

		return optionLinks;
	}

	/**
	 * 指定されたメニューIDに関連するオプション情報を取得するメソッド
	 * 論理削除されていないオプションのみを取得する
	 * 
	 * @param menuId メニューID
	 * @return オプションDTOのリスト
	 */
	public List<OptionDTO> findByMenuId(int menuId) {
		final List<OptionDTO> options = new ArrayList<>();
		final String sql = "SELECT "
				+ COLUMN_OPTION_ID + ","
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_NAME + ","
				+ COLUMN_OPTION_PRICE + ","
				+ COLUMN_OPTION_DELETED_AT
				+ " FROM " + TABLE_NAME
				+ " WHERE " + COLUMN_MENU_ID + " = ?"
				+ " AND " + COLUMN_OPTION_DELETED_AT + " IS NULL";

		try (Connection conn = dbmanager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, menuId);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				options.add(new OptionDTO(
						rs.getInt(COLUMN_OPTION_ID),
						rs.getInt(COLUMN_MENU_ID),
						rs.getString(COLUMN_OPTION_NAME),
						rs.getInt(COLUMN_OPTION_PRICE),
						rs.getTimestamp(COLUMN_OPTION_DELETED_AT)));
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションの取得に失敗しました", e);
		}

		return options;
	}

	/**
	 * 指定されたIDのオプション情報を取得するメソッド
	 * 削除済みオプションは含めない
	 * 
	 * @param optionId オプションID
	 * @return オプションDTO
	 */
	public OptionDTO findById(int optionId) {
		return findById(optionId, false);
	}

	/**
	 * 指定されたIDのオプション情報を取得するメソッド
	 * 
	 * @param optionId オプションID
	 * @param includeDeleted 論理削除されたオプションも含めるかどうか
	 * @return オプションDTO
	 */
	public OptionDTO findById(int optionId, boolean includeDeleted) {
		final String sql = "SELECT "
				+ COLUMN_OPTION_ID + ","
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_NAME + ","
				+ COLUMN_OPTION_PRICE + ","
				+ COLUMN_OPTION_DELETED_AT
				+ " FROM v_options_linked_active_menus"
				+ " WHERE " + COLUMN_OPTION_ID + " = ?"
				+ (includeDeleted ? "" : " AND " + COLUMN_OPTION_DELETED_AT + " IS NULL");

		try (Connection conn = dbmanager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, optionId);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new OptionDTO(
						rs.getInt(COLUMN_OPTION_ID),
						rs.getInt(COLUMN_MENU_ID),
						rs.getString(COLUMN_OPTION_NAME),
						rs.getInt(COLUMN_OPTION_PRICE),
						rs.getTimestamp(COLUMN_OPTION_DELETED_AT));
			} else {
				throw new EntityNotFoundException("オプションが見つかりません ", optionId);
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションの取得に失敗しました", e);
		}
	}

	/**
	 * すべてのオプション情報を取得するメソッド
	 * 削除済みオプションは含めない
	 * 
	 * @return オプションDTOのリスト
	 */
	public List<OptionDTO> getAllOptions() {
		return getAllOptions(false);
	}

	/**
	 * すべてのオプション情報を取得するメソッド
	 * 
	 * @param includeDeleted 削除されたオプションも含めるかどうか
	 * @return オプションDTOのリスト
	 */
	public List<OptionDTO> getAllOptions(boolean includeDeleted) {
		final List<OptionDTO> options = new ArrayList<>();
		final String sql = "SELECT "
				+ COLUMN_OPTION_ID + ","
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_NAME + ","
				+ COLUMN_OPTION_PRICE + ","
				+ COLUMN_OPTION_DELETED_AT
				+ " FROM " + TABLE_NAME
				+ (includeDeleted ? "" : " WHERE " + COLUMN_OPTION_DELETED_AT + " IS NULL");

		try (Connection conn = dbmanager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				options.add(new OptionDTO(
						rs.getInt(COLUMN_OPTION_ID),
						rs.getInt(COLUMN_MENU_ID),
						rs.getString(COLUMN_OPTION_NAME),
						rs.getInt(COLUMN_OPTION_PRICE),
						rs.getTimestamp(COLUMN_OPTION_DELETED_AT)));
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションの取得に失敗しました", e);
		}

		return options;
	}

	/**
	 * オプション情報を登録するメソッド
	 * 
	 * @param option オプションDTO
	 */
	public void registerOption(NewOptionDTO option) {
		final String sql = "INSERT INTO " + TABLE_NAME + " ("
				+ COLUMN_MENU_ID + ","
				+ COLUMN_OPTION_NAME + ","
				+ COLUMN_OPTION_PRICE
				+ ") VALUES (?, ?, ?)";
		try (Connection conn = dbmanager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, option.getMenuId());
			stmt.setString(2, option.getName());
			stmt.setInt(3, option.getPrice());
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new DataAccessException("オプションの登録に失敗しました", e);
		}
	}

	public void updateOption(int optionId, NewOptionDTO option) {
		final String deleteSql = "UPDATE " + TABLE_NAME
				+ " SET " + COLUMN_OPTION_DELETED_AT + " = sysdate"
				+ " WHERE " + COLUMN_OPTION_ID + " = ?"
				+ " AND " + COLUMN_OPTION_DELETED_AT + " IS NULL";

		final String editSql = "INSERT INTO " + TABLE_NAME + " ("
				+ COLUMN_MENU_ID + ", "
				+ COLUMN_OPTION_NAME + ", "
				+ COLUMN_OPTION_PRICE
				+ ") VALUES (?, ?, ?)";

		try (Connection conn = dbmanager.getConnection();
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
				PreparedStatement editStmt = conn.prepareStatement(editSql, new String[] { COLUMN_OPTION_ID });) {
			try {
				conn.setAutoCommit(false);

				// 既存オプションの論理削除
				deleteStmt.setInt(1, optionId);
				final int rowsDeleted = deleteStmt.executeUpdate();
				if (rowsDeleted == 0) {
					conn.rollback();
					throw new EntityNotFoundException("オプションが見つかりません ", optionId);
				}

				// 新オプション情報の挿入
				editStmt.setInt(1, option.getMenuId());
				editStmt.setString(2, option.getName());
				editStmt.setInt(3, option.getPrice());
				int rowsUpdated = editStmt.executeUpdate();
				if (rowsUpdated != 1) {
					conn.rollback();
					throw new DataAccessException("オプションの更新に失敗しました(更新後オプションの挿入時エラー)");
				}

				final ResultSet generatedKeys = editStmt.getGeneratedKeys();
				if (!generatedKeys.next()) {
					conn.rollback();
					throw new DataAccessException("新しいオプションIDの取得に失敗しました。(更新後オプションID取得時エラー)");
				}
				conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException ignore) {
				}
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションの更新に失敗しました", e);
		}
	}

	public int deleteOptions(List<Integer> optionIds) {
		final String deleteSql = "UPDATE " + TABLE_NAME
				+ " SET " + COLUMN_OPTION_DELETED_AT + " = sysdate"
				+ " WHERE " + COLUMN_OPTION_ID + " = ?"
				+ " AND " + COLUMN_OPTION_DELETED_AT + " IS NULL";

		try (Connection conn = dbmanager.getConnection();
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);) {
			try {
				conn.setAutoCommit(false);
				int totalRowsDeleted = 0;
				for (Integer optionId : optionIds) {
					deleteStmt.setInt(1, optionId);
					totalRowsDeleted += deleteStmt.executeUpdate();
				}
				conn.commit();
				return totalRowsDeleted;
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException ignore) {
				}
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException("オプションの削除に失敗しました", e);
		}
	}
}
