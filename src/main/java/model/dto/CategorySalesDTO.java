package model.dto;

/**
 * カテゴリ別売上情報を保持するDTOクラス
 */
public class CategorySalesDTO {
	private int categoryId;
	private String name;
	private long sales;
	private int quantity;
	private int productCount;

	public CategorySalesDTO() {
	}

	public CategorySalesDTO(int categoryId, String name, long sales, int quantity, int productCount) {
		this.categoryId = categoryId;
		this.name = name;
		this.sales = sales;
		this.quantity = quantity;
		this.productCount = productCount;
	}

	// Getters and Setters
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSales() {
		return sales;
	}

	public void setSales(long sales) {
		this.sales = sales;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}
}
