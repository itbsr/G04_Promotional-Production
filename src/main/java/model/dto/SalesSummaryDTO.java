package model.dto;

/**
 * 売上サマリー情報を保持するDTOクラス
 */
public class SalesSummaryDTO {
	private long totalSales;
	private int orderCount;
	private int customerCount;
	private long averagePerCustomer;
	private double salesChangePercent;
	private double orderChangePercent;
	private double customerChangePercent;
	private double avgChangePercent;

	public SalesSummaryDTO() {
	}

	public SalesSummaryDTO(long totalSales, int orderCount, int customerCount) {
		this.totalSales = totalSales;
		this.orderCount = orderCount;
		this.customerCount = customerCount;
		this.averagePerCustomer = customerCount > 0 ? totalSales / customerCount : 0;
	}

	// Getters and Setters
	public long getTotalSales() {
		return totalSales;
	}

	public void setTotalSales(long totalSales) {
		this.totalSales = totalSales;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}

	public int getCustomerCount() {
		return customerCount;
	}

	public void setCustomerCount(int customerCount) {
		this.customerCount = customerCount;
	}

	public long getAveragePerCustomer() {
		return averagePerCustomer;
	}

	public void setAveragePerCustomer(long averagePerCustomer) {
		this.averagePerCustomer = averagePerCustomer;
	}

	public double getSalesChangePercent() {
		return salesChangePercent;
	}

	public void setSalesChangePercent(double salesChangePercent) {
		this.salesChangePercent = salesChangePercent;
	}

	public double getOrderChangePercent() {
		return orderChangePercent;
	}

	public void setOrderChangePercent(double orderChangePercent) {
		this.orderChangePercent = orderChangePercent;
	}

	public double getCustomerChangePercent() {
		return customerChangePercent;
	}

	public void setCustomerChangePercent(double customerChangePercent) {
		this.customerChangePercent = customerChangePercent;
	}

	public double getAvgChangePercent() {
		return avgChangePercent;
	}

	public void setAvgChangePercent(double avgChangePercent) {
		this.avgChangePercent = avgChangePercent;
	}
}
