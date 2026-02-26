package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.dto.MenuDTO;
import model.dto.NewMenuDTO;

public class MenuDAO {
	private DBManager dbManager;

	/**
	 * デフォルトコンストラクタ
	 */
	public MenuDAO() {
		this(DBManager.getInstance());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param dbManager
	 */
	public MenuDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	// Table name
	public static final String TABLE_NAME = "menus";
	public static final String VIEW_AVAILABLE_MENUS = "v_active_menus";

	// Column names
	public static final String COLUMN_MENU_ID = "menu_id";
	public static final String COLUMN_PRICE = "menu_price";
	public static final String COLUMN_MENU_NAME = "menu_name";
	public static final String COLUMN_MENU_IMAGE = "menu_image";
	public static final String COLUMN_DELETED_AT = "deleted_at";

	/**
	 * DELETEされていないメニュー一覧を取得するメソッド。
	 * オプションも削除されていないもののみ取得する。
	 *
	 * @return メニューリスト
	 */
	public List<MenuDTO> getAvailableMenu() {
		final List<MenuDTO> menuList = new ArrayList<>();
		final String sql = """
				select m.menu_id,
				       m.menu_name,
				       m.category_id,
				       m.menu_price,
				       m.menu_image,
				       m.deleted_at,
				       listagg(o.option_id,
				               ',') within group(
				        order by o.option_id) as option_ids
				  from v_active_menus m
				  left join options o
				on m.menu_id = o.menu_id
				   and o.option_deleted_at is null
				 group by m.menu_id,
				          m.menu_name,
				          m.category_id,
				          m.menu_price,
				          m.menu_image,
				          m.deleted_at,
				          m.sort_order
				 order by m.sort_order asc
				""";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				// parse option ids(comma separated string)
				final String sOptionIds = rs.getString("option_ids");
				final List<String> rgsOptionIds = sOptionIds != null ? Arrays.asList(sOptionIds.split(","))
						: new ArrayList<String>();
				final List<Integer> rgiOptionIds = new ArrayList<>();
				for (var i : rgsOptionIds)
					rgiOptionIds.add(Integer.parseInt(i));

				menuList.add(new MenuDTO(
						rs.getInt(COLUMN_MENU_ID),
						rs.getTimestamp(COLUMN_DELETED_AT),
						rs.getString(COLUMN_MENU_NAME),
						rs.getInt(CategoryDAO.COLUMN_CATEGORY_ID),
						rs.getInt(COLUMN_PRICE),
						rs.getString(COLUMN_MENU_IMAGE),
						rgiOptionIds));
			}
		} catch (SQLException e) {
			throw new DataAccessException("メニュー一覧の取得に失敗しました", e);
		}

		return menuList;
	}

	/**
	 * 削除済みを含むすべてのメニュー一覧を取得するメソッド
	 *
	 * @return メニューリスト
	 */
	public List<MenuDTO> getMenuAll() {
		final List<MenuDTO> manageMenuList = new ArrayList<>();
		final String sql = "SELECT "
				+ COLUMN_MENU_ID + ","
				+ COLUMN_MENU_NAME + ","
				+ CategoryDAO.COLUMN_CATEGORY_ID + ","
				+ COLUMN_PRICE + ","
				+ COLUMN_MENU_IMAGE + ","
				+ COLUMN_DELETED_AT + ","
				+ " LISTAGG(option_id, ',') WITHIN GROUP(ORDER BY option_id) as option_ids"
				+ " FROM " + TABLE_NAME
				+ " LEFT JOIN options USING (" + COLUMN_MENU_ID + ")"
				+ " GROUP BY "
				+ COLUMN_MENU_ID + ","
				+ COLUMN_MENU_NAME + ","
				+ CategoryDAO.COLUMN_CATEGORY_ID + ","
				+ COLUMN_PRICE + ","
				+ COLUMN_MENU_IMAGE + ","
				+ COLUMN_DELETED_AT;

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				// parse option ids(comma separated string)
				final String sOptionIds = rs.getString("option_ids");
				final List<String> rgsOptionIds = sOptionIds != null ? Arrays.asList(sOptionIds.split(","))
						: new ArrayList<String>();
				final List<Integer> rgiOptionIds = new ArrayList<>();
				for (var i : rgsOptionIds)
					rgiOptionIds.add(Integer.parseInt(i));

				manageMenuList.add(new MenuDTO(
						rs.getInt(COLUMN_MENU_ID),
						rs.getTimestamp(COLUMN_DELETED_AT),
						rs.getString(COLUMN_MENU_NAME),
						rs.getInt(CategoryDAO.COLUMN_CATEGORY_ID),
						rs.getInt(COLUMN_PRICE),
						rs.getString(COLUMN_MENU_IMAGE),
						rgiOptionIds));

			}
		} catch (SQLException | NumberFormatException e) {
			throw new DataAccessException("メニュー一覧の取得に失敗しました", e);
		}

		return manageMenuList;
	}

	public MenuDTO registerMenu(NewMenuDTO newMenu) {
		final String sql = "INSERT INTO " + TABLE_NAME + " ("
				+ COLUMN_MENU_NAME + ","
				+ CategoryDAO.COLUMN_CATEGORY_ID + ","
				+ COLUMN_PRICE + ","
				+ COLUMN_MENU_IMAGE
				+ ") VALUES (?, ?, ?, ?)";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, new String[] { COLUMN_MENU_ID })) {

			stmt.setString(1, newMenu.getName());
			stmt.setInt(2, newMenu.getCategoryId());
			stmt.setInt(3, newMenu.getPrice());
			stmt.setString(4, newMenu.getImage());
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted == 0) {
				return null;
			}

			// 挿入されたメニューIDを取得
			final ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (!generatedKeys.next()) {
				throw new DataAccessException("新しいメニューIDの取得に失敗しました。");
			}
			final int newMenuId = generatedKeys.getInt(1);

			// 登録したメニューを取得して返却
			return getMenuById(newMenuId);

		} catch (SQLException e) {
			throw new DataAccessException("メニューの登録に失敗しました", e);
		}
	}

	public int updateMenu(int menuId, NewMenuDTO patchData) {
		final String deleteSql = "UPDATE " + TABLE_NAME
				+ " SET " + COLUMN_DELETED_AT + " = sysdate"
				+ " WHERE " + COLUMN_MENU_ID + " = ?"
				+ " AND " + COLUMN_DELETED_AT + " IS NULL";
		final String editSql = "INSERT INTO " + TABLE_NAME + " ("
				+ COLUMN_MENU_NAME + ", "
				+ CategoryDAO.COLUMN_CATEGORY_ID + ", "
				+ COLUMN_PRICE + ", "
				+ COLUMN_MENU_IMAGE
				+ ") VALUES (?, ?, ?, ?)";
		// 元のメニューに紐づくオプションを取得するSQL
		final String selectOptionsSql = "SELECT option_name, option_price FROM options WHERE menu_id = ? AND option_deleted_at IS NULL";
		// 新しいメニューにオプションをコピーするSQL
		final String copyOptionSql = "INSERT INTO options (menu_id, option_name, option_price) VALUES (?, ?, ?)";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
				PreparedStatement editStmt = conn.prepareStatement(editSql, new String[] { COLUMN_MENU_ID });
				PreparedStatement selectOptionsStmt = conn.prepareStatement(selectOptionsSql);
				PreparedStatement copyOptionStmt = conn.prepareStatement(copyOptionSql);) {
			try {

				conn.setAutoCommit(false);

				// 元のメニューに紐づくオプション情報を先に取得（論理削除前にコピー用として保存）
				selectOptionsStmt.setInt(1, menuId);
				ResultSet optionsRs = selectOptionsStmt.executeQuery();
				List<String[]> optionsList = new ArrayList<>();
				while (optionsRs.next()) {
					optionsList.add(new String[] {
							optionsRs.getString("option_name"),
							String.valueOf(optionsRs.getInt("option_price"))
					});
				}

				// 既存のメニューを論理削除
				deleteStmt.setInt(1, menuId);
				final int rowsDeleted = deleteStmt.executeUpdate();
				if (rowsDeleted == 0) {
					conn.rollback();
					throw new EntityNotFoundException("指定されたメニューが見つかりません。", menuId);
				}

				// 新しいメニュー情報を挿入
				editStmt.setString(1, patchData.getName());
				editStmt.setInt(2, patchData.getCategoryId());
				editStmt.setInt(3, patchData.getPrice());
				editStmt.setString(4, patchData.getImage());
				int rowsUpdated = editStmt.executeUpdate();
				if (rowsUpdated != 1) {
					conn.rollback();
					throw new DataAccessException("メニューの更新に失敗しました(更新後メニューの挿入時エラー)");
				}

				// 新しいメニューIDを取得
				final ResultSet generatedKeys = editStmt.getGeneratedKeys();
				if (!generatedKeys.next()) {
					conn.rollback();
					throw new DataAccessException("新しいメニューIDの取得に失敗しました。(更新後メニューのID取得エラー)");
				}

				final int newMenuId = generatedKeys.getInt(1);

				// 元のオプション情報を新しいメニューにコピー（元のオプションは論理削除せずそのまま残す）
				for (String[] option : optionsList) {
					copyOptionStmt.setInt(1, newMenuId);
					copyOptionStmt.setString(2, option[0]); // option_name
					copyOptionStmt.setInt(3, Integer.parseInt(option[1])); // option_price
					copyOptionStmt.executeUpdate();
				}

				conn.commit();
				return newMenuId;
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException ignore) {
				}
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException("メニューの更新に失敗しました", e);
		}
	}

	public int deleteMenus(List<Integer> menuIds) {
		if (menuIds == null || menuIds.isEmpty()) {
			return 0;
		}
		final String sql = "UPDATE " + TABLE_NAME
				+ " SET " + COLUMN_DELETED_AT + " = sysdate"
				+ " WHERE " + COLUMN_MENU_ID + " = ? "
				+ " AND " + COLUMN_DELETED_AT + " IS NULL";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			try {
				conn.setAutoCommit(false);
				int totalUpdated = 0;
				for (Integer menuId : menuIds) {
					stmt.setInt(1, menuId);
					stmt.addBatch();
				}
				int[] rowsUpdated = stmt.executeBatch();
				for (int count : rowsUpdated) {
					totalUpdated += count;
				}

				conn.commit();
				return totalUpdated;
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException ignore) {
				}
				throw e;
			}

		} catch (SQLException e) {
			throw new DataAccessException("メニューの削除に失敗しました", e);
		}

	}

	public MenuDTO getMenuById(int menuId) {
		final String sql = "SELECT "
				+ COLUMN_MENU_ID + ","
				+ COLUMN_MENU_NAME + ","
				+ CategoryDAO.COLUMN_CATEGORY_ID + ","
				+ COLUMN_PRICE + ","
				+ COLUMN_MENU_IMAGE + ","
				+ COLUMN_DELETED_AT
				+ " FROM " + TABLE_NAME
				+ " WHERE " + COLUMN_MENU_ID + " = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, menuId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new MenuDTO(
							rs.getInt(COLUMN_MENU_ID),
							rs.getTimestamp(COLUMN_DELETED_AT),
							rs.getString(COLUMN_MENU_NAME),
							rs.getInt(CategoryDAO.COLUMN_CATEGORY_ID),
							rs.getInt(COLUMN_PRICE),
							rs.getString(COLUMN_MENU_IMAGE),
							new ArrayList<Integer>());
				} else {
					throw new EntityNotFoundException("指定されたメニューが見つかりません。", menuId);
				}
			}

		} catch (SQLException e) {
			throw new DataAccessException("メニューの取得に失敗しました", e);
		}
	}
}
