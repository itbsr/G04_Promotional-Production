package model.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * 注文情報を保持するDTOクラス。
 */
public class OrderDTO extends NewOrderWithIdDTO {
	private int statusId;
	private String statusName;
	private String tableName;
	private Timestamp orderedAt;
	private Timestamp cookedAt;

	/**
	 * デフォルトコンストラクタ
	 */
	public OrderDTO() {
	}

	/**
	 * 全フィールドを初期化するコンストラクタ
	 *
	 * @param menuId    メニューID
	 * @param optionIds オプションIDのリスト
	 * @param orderId   注文ID
	 * @param statusId    注文ステータスID
	 * @param statusName  注文ステータス名
	 * @param tableName   テーブル名
	 */
	public OrderDTO(UUID orderId, int menuId, int statusId, String statusName, String tableName,
			List<Integer> optionIds,
			Timestamp orderedAt, Timestamp cookedAt) {
		super(menuId, optionIds, orderId);
		this.orderId = orderId;
		this.menuId = menuId;
		this.statusId = statusId;
		this.statusName = statusName;
		this.tableName = tableName;
		this.optionIds = optionIds;
		this.orderedAt = orderedAt;
		this.cookedAt = cookedAt;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Timestamp getOrderedAt() {
		return orderedAt;
	}

	public void setOrderedAt(Timestamp orderedAt) {
		this.orderedAt = orderedAt;
	}

	public Timestamp getCookedAt() {
		return cookedAt;
	}

	public void setCookedAt(Timestamp cookedAt) {
		this.cookedAt = cookedAt;
	}
}
