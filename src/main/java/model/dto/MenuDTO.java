package model.dto;

import java.sql.Timestamp;
import java.util.List;

/**
 * メニューDTOクラス。メニュー情報を保持する。
 */
public class MenuDTO extends NewMenuDTO {
	private int id;
	private Timestamp deletedAt;

	/**
	 * デフォルトコンストラクタ
	 */
	public MenuDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param id            メニューID
	 * @param deletedAt     削除日時
	 * @param name          メニュー名
	 * @param categoryId    カテゴリID
	 * @param price         メニューの価格
	 * @param image         メニュー画像のパス
	 * @param optionIds     オプションIDのリスト
	 */
	public MenuDTO(int id, Timestamp deletedAt, String name, int categoryId, int price, String image,
			List<Integer> optionIds) {
		super(name, categoryId, price, image, optionIds);
		this.id = id;
		this.deletedAt = deletedAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		this.deletedAt = deletedAt;
	}
}
