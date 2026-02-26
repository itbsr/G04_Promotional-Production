package model.dto;

import java.io.Serializable;

public class NewOptionDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int menuId;
	private String name;
	private int price;

	public NewOptionDTO() {
	}

	public NewOptionDTO(int menuId, String name, int price) {
		this.menuId = menuId;
		this.name = name;
		this.price = price;
	}

	public int getMenuId() {
		return menuId;
	}

	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}
