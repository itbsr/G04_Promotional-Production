package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.dto.CategoryDTO;

public class CategoryDAO {
	private DBManager dbManager;

	// Table name
	public static final String TABLE_NAME = "categories";

	// Column names
	public static final String COLUMN_CATEGORY_ID = "category_id";
	public static final String COLUMN_PARENT_CATEGORY_ID = "parent_category_id";
	public static final String COLUMN_CATEGORY_NAME = "category_name";
	public static final String COLUMN_CREATED_AT = "created_at";
	public static final String COLUMN_UPDATED_AT = "updated_at";

	/**
	 * デフォルトコンストラクタ
	 */
	public CategoryDAO() {
		this(DBManager.getInstance());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param dbManager
	 */
	public CategoryDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	/**
	 * すべてのカテゴリー情報を取得するメソッド
	 *
	 * @return カテゴリーリスト
	 */
	public List<CategoryDTO> getAllCategories() {
		final String sql = "SELECT level AS depth,"
				+ COLUMN_CATEGORY_ID + ","
				+ COLUMN_PARENT_CATEGORY_ID + ","
				+ COLUMN_CATEGORY_NAME + ","
				+ COLUMN_CREATED_AT + ","
				+ COLUMN_UPDATED_AT
				+ " FROM categories"
				+ " START WITH parent_category_id IS NULL"
				+ " CONNECT BY NOCYCLE PRIOR category_id = parent_category_id"
				+ " ORDER SIBLINGS BY sort_order";

		try (Connection connection = dbManager.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();) {
			Map<Integer, CategoryDTO> categoryMap = new HashMap<>();
			List<CategoryDTO> root = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("category_id");
				String name = rs.getString("category_name");
				Integer parentId = rs.getObject("parent_category_id") != null ? rs.getInt("parent_category_id") : null;
				Timestamp createdAt = rs.getTimestamp("created_at");
				Timestamp updatedAt = rs.getTimestamp("updated_at");

				// DTOを生成しルートまたは親のchildrenに追加
				final var current = new CategoryDTO(id, name, createdAt, updatedAt);
				if (parentId == null)
					root.add(current);
				else
					categoryMap.get(parentId).getChildren().add(current);

				categoryMap.put(id, current);
			}
			return root;
		} catch (SQLException e) {
			throw new DataAccessException("データベースエラーが発生しました", e);
		}
	}
}
