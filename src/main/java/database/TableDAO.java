package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.dto.TableDTO;

public class TableDAO {
	private DBManager dbManager;

	public TableDAO() {
		this(DBManager.getInstance());
	}

	public TableDAO(DBManager dbManager) {
		this.dbManager = dbManager;
	}

	public static final String COLUMN_TABLE_ID = "table_id";
	public static final String COLUMN_TABLE_NAME = "table_name";
	public static final String COLUMN_TABLE_CAPACITY = "table_capacity";
	public static final String TABLE_NAME = "tables";

	public List<TableDTO> getAllTables() {
		final String sql = "SELECT "
				+ COLUMN_TABLE_ID + ", "
				+ COLUMN_TABLE_NAME + ", "
				+ COLUMN_TABLE_CAPACITY + " "
				+ "FROM " + TABLE_NAME;

		try (
				final var conn = dbManager.getConnection();
				final var stmt = conn.prepareStatement(sql);
				final var rs = stmt.executeQuery();) {
			final List<TableDTO> tables = new ArrayList<>();
			while (rs.next()) {
				final var nTableId = rs.getInt(COLUMN_TABLE_ID);
				final var sTableName = rs.getString(COLUMN_TABLE_NAME);
				final var nTableCapacity = rs.getInt(COLUMN_TABLE_CAPACITY);

				final var table = new TableDTO(nTableId, sTableName, nTableCapacity);
				tables.add(table);
			}
			return tables;
		} catch (SQLException e) {
			throw new DataAccessException("卓(テーブル)情報の取得に失敗しました。", e);
		}
	}
}
