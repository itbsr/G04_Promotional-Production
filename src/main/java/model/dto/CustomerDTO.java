package model.dto;

import java.sql.Timestamp;
import java.util.UUID;

public class CustomerDTO extends NewCustomerDTO {

	private Integer tableId;
	private Timestamp paidAt;
	private CustomerStatus status;

	public CustomerDTO() {
		super();
	}

	public CustomerDTO(UUID customerId, int customerCount, Integer tableId, Timestamp paidAt, int statusId,
			String statusDescription) {
		super(customerId, customerCount);
		this.tableId = tableId;
		this.paidAt = paidAt;
		this.status = new CustomerStatus(statusId, statusDescription);
	}

	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public Timestamp getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Timestamp paidAt) {
		this.paidAt = paidAt;
	}

	public CustomerStatus getStatus() {
		return status;
	}

	public void setStatus(CustomerStatus status) {
		this.status = status;
	}

	public class CustomerStatus {
		public static final int REGISTERED = 1;
		public static final int EATING = 2;
		public static final int DINING_COMPLETED = 3;

		private int id;
		private String description;

		public CustomerStatus() {

		}

		public CustomerStatus(int id, String description) {
			this.id = id;
			this.description = description;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
