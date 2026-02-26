package model.dto;

import java.sql.Date;

/**
 * 日別売上情報を保持するDTOクラス
 */
public class DailySalesDTO {
	private Date date;
	private long sales;
	private int orders;
	private int customers;
	private long avgPerCustomer;

	public DailySalesDTO() {
	}

	public DailySalesDTO(Date date, long sales, int orders, int customers) {
		this.date = date;
		this.sales = sales;
		this.orders = orders;
		this.customers = customers;
		this.avgPerCustomer = customers > 0 ? sales / customers : 0;
	}

	// Getters and Setters
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public int getCustomers() {
		return customers;
	}

	public void setCustomers(int customers) {
		this.customers = customers;
	}

	public long getAvgPerCustomer() {
		return avgPerCustomer;
	}

	public void setAvgPerCustomer(long avgPerCustomer) {
		this.avgPerCustomer = avgPerCustomer;
	}
}
