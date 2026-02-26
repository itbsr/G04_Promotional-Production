package model.dto;

import java.io.Serializable;
import java.util.List;

public class NewOrderDTO implements Serializable {
	protected int menuId;
	protected List<Integer> optionIds;

	/**
	 * デフォルトコンストラクタ
	 */
	public NewOrderDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param menuId    メニューID
	 * @param optionIds オプションIDのリスト
	 */
	public NewOrderDTO(int menuId, List<Integer> optionIds) {
		this.menuId = menuId;
		this.optionIds = optionIds;
	}

	public int getMenuId() {
		return menuId;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	public List<Integer> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<Integer> optionIds) {
		this.optionIds = optionIds;
	}

}
