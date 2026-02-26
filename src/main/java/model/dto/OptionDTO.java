package model.dto;

import java.sql.Timestamp;

/**
 * オプションリンク情報を保持するDTOクラス。
 * メニューとオプションの関連付けおよび価格を表す。
 */
public class OptionDTO extends NewOptionDTO {
	private static final long serialVersionUID = 1L;

	private int id;
	private Timestamp optionDeletedAt;

	/**
	 * デフォルトコンストラクタ
	 */
	public OptionDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param id              オプションID
	 * @param menuId          メニューID
	 * @param name            オプション名
	 * @param price           オプション価格
	 * @param optionDeletedAt 削除日時
	 */
	public OptionDTO(int id, int menuId, String name, int price, Timestamp optionDeletedAt) {
		super(menuId, name, price);
		this.id = id;
		this.optionDeletedAt = optionDeletedAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getOptionDeletedAt() {
		return optionDeletedAt;
	}

	public void setOptionDeletedAt(Timestamp optionDeletedAt) {
		this.optionDeletedAt = optionDeletedAt;
	}
}
