package model.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * 新規顧客登録用のDTO
 */
public class NewCustomerDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID customerId;
	private int customerCount;

	/**
	 * デフォルトコンストラクタ
	 */
	public NewCustomerDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param customerId    顧客ID
	 * @param customerCount 顧客数
	 */
	public NewCustomerDTO(UUID customerId, int customerCount) {
		this.customerId = customerId;
		this.customerCount = customerCount;
	}

	public UUID getCustomerId() {
		return customerId;
	}

	public void setCustomerId(UUID customerId) {
		this.customerId = customerId;
	}

	public int getCustomerCount() {
		return customerCount;
	}

	public void setCustomerCount(int customerCount) {
		this.customerCount = customerCount;
	}
}
