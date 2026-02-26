package model.dto;

/**
 * 時間帯別売上情報を保持するDTOクラス
 */
public class HourlySalesDTO {
	private int hour;
	private long sales;
	private int orders;

	public HourlySalesDTO() {
	}

	public HourlySalesDTO(int hour, long sales, int orders) {
		this.hour = hour;
		this.sales = sales;
		this.orders = orders;
	}

	// Getters and Setters
	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public long getSales() {
		return sales;
	}

	public void setSales(long sales) {
		this.sales = sales;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}
}
