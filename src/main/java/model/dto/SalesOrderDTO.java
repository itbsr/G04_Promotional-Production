package model.dto;

import java.sql.Timestamp;
import java.util.List;

/**
 * 売上画面用の注文詳細情報を保持するDTOクラス
 */
public class SalesOrderDTO {
	private String orderId;
	private Timestamp createdAt;
	private int tableNumber;
	private String tableName;
	private List<SalesOrderItemDTO> items;
	private long total;
	private String status;

	public SalesOrderDTO() {
	}

	// Getters and Setters
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<SalesOrderItemDTO> getItems() {
		return items;
	}

	public void setItems(List<SalesOrderItemDTO> items) {
		this.items = items;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 注文内の商品情報
	 */
	public static class SalesOrderItemDTO {
		private String name;
		private int price;
		private int quantity;

		public SalesOrderItemDTO() {
		}

		public SalesOrderItemDTO(String name, int price, int quantity) {
			this.name = name;
			this.price = price;
			this.quantity = quantity;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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
	}
}
