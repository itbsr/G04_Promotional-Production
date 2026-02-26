package model.service;

import java.util.List;

import database.TableDAO;
import model.dto.TableDTO;

public class TableService {
	private TableDAO tableDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public TableService() {
		this(new TableDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param tableDAO オプションDAO
	 */
	public TableService(TableDAO tableDAO) {
		this.tableDAO = tableDAO;
	}

	/**
	 * すべてのテーブル情報を取得する
	 *
	 * @return テーブル情報DTOのリスト
	 */
	public List<TableDTO> getAllTables() {
		return tableDAO.getAllTables();
	}
}
