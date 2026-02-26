package model.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 新規メニュー登録・編集用DTOクラス。ID以外のメニュー情報を保持する。
 */
public class NewMenuDTO implements Serializable {
	private String name;
	private int categoryId;
	private int price;
	private String image;
	private List<Integer> optionIds;

	public NewMenuDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param name          メニュー名
	 * @param categoryId    カテゴリID
	 * @param price         メニューの価格
	 * @param image         メニュー画像のパス
	 * @param optionIds     オプションIDのリスト
	 */
	public NewMenuDTO(String name, int categoryId, int price, String image, List<Integer> optionIds) {
		this.name = name;
		this.categoryId = categoryId;
		this.price = price;
		this.image = image;
		this.optionIds = optionIds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<Integer> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<Integer> optionIds) {
		this.optionIds = optionIds;
	}
}
