package model.dto;

/**
 * 商品別売上情報を保持するDTOクラス
 */
public class ProductSalesDTO {
	private int menuId;
	private String name;
	private String category;
	private int price;
	private int quantity;
	private long sales;

	public ProductSalesDTO() {
	}

	public ProductSalesDTO(int menuId, String name, String category, int price, int quantity, long sales) {
		this.menuId = menuId;
		this.name = name;
		this.category = category;
		this.price = price;
		this.quantity = quantity;
		this.sales = sales;
	}

	// Getters and Setters
	public int getMenuId() {
		return menuId;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public long getSales() {
		return sales;
	}

	public void setSales(long sales) {
		this.sales = sales;
	}
}
