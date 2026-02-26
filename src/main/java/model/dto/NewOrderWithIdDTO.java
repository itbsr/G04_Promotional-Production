package model.dto;

import java.util.List;
import java.util.UUID;

/**
 * 注文情報を保持するDTOクラス。
 */
public class NewOrderWithIdDTO extends NewOrderDTO {
	protected UUID orderId;

	public NewOrderWithIdDTO() {
	}

	public NewOrderWithIdDTO(int menuId, List<Integer> optionIds, UUID orderId) {
		super(menuId, optionIds);
		this.orderId = orderId;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}
}
