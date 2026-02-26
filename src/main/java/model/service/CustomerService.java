package model.service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import database.CustomerDAO;
import database.OrderDAO;
import model.dto.CustomerDTO;
import model.dto.DeviceDTO;
import model.dto.NewCustomerDTO;
import model.dto.OrderDTO;

/**
 * 顧客に関するビジネスロジックを管理するサービスクラス
 */
public class CustomerService {
	private CustomerDAO customerDAO;
	private OrderDAO orderDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public CustomerService() {
		this(new CustomerDAO(), new OrderDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 * 
	 * @param customerDAO 顧客DAO
	 * @param orderDAO 注文DAO
	 */
	public CustomerService(CustomerDAO customerDAO, OrderDAO orderDAO) {
		this.customerDAO = customerDAO;
		this.orderDAO = orderDAO;
	}

	/**
	 * 新規顧客を登録する
	 * 
	 * @param customerCount 顧客数
	 * @return 登録成功時に登録内容のDTO、失敗時null
	 */
	public NewCustomerDTO registerCustomer(int customerCount) {
		// UUIDを生成
		// final UUID newCustomerId = Generators.timeBasedEpochGenerator().generate();

		// 一時的な簡易実装としてカスタムUUIDを使用
		// UUIDv8として以下の仕様で生成しています
		// 1. バージョンフィールド : 0x8
		// 2. var : UUIDv8仕様に準拠し0b10
		// 3. rand_b : 0~999999をとる乱数
		final UUID newCustomerId = new UUID(
				// ver=0x8
				0x8000,
				// var=0b10, rand_b=rand(0<=n<=999999)
				(0x8000_0000_0000_0000L | (new Random().nextInt(1_000_000) & 0x3FFF_FFFF_FFFF_FFFFL)));

		// DTOを生成
		final NewCustomerDTO newCustomer = new NewCustomerDTO(newCustomerId, customerCount);

		// DAOを呼び出して登録
		boolean success = customerDAO.registerCustomer(newCustomer);
		return success ? newCustomer : null;
	}

	public void setTableByDeviceId(UUID deviceId, UUID customerId) {
		customerDAO.setTableByDeviceId(deviceId, customerId);
	}

	/**
	 * 会計待ち状態かどうかを判定する
	 * @param customerId 顧客ID
	 * @return 会計待ち状態の場合true、そうでない場合false
	 */
	public boolean isEligibleForCheckout(UUID customerId) {
		final CustomerDTO customer = customerDAO.getCustomerById(customerId);
		final int statusId = customer.getStatus().getId();
		return statusId == CustomerDTO.CustomerStatus.DINING_COMPLETED && customer.getPaidAt() == null;
	}

	/**
	 * 顧客IDから顧客情報を取得する
	 * 
	 * @param customerId 顧客ID
	 * @return 顧客情報のDTO
	 */
	public CustomerDTO getCustomerById(UUID customerId) {
		return customerDAO.getCustomerById(customerId);
	}

	/**
	 * デバイスに割り当てられた(飲食中の)顧客を取得する。
	 * 
	 * @param device デバイス情報
	 * @return 顧客情報のDTO
	 */
	public CustomerDTO getAllocatedCustomerByDevice(DeviceDTO device) {
		return customerDAO.getAllocatedCustomerByDevice(device);
	}

	/**
	 * 顧客を会計対象ステータスに変更する
	 *
	 * @param customerId 対象の顧客ID
	 * @param force trueの場合、未配膳商品があっても強制的に会計対象に変更する
	 */
	public void proceedToCheckout(UUID customerId, boolean force) {
		final CustomerDTO customer = customerDAO.getCustomerById(customerId);

		// 食事中ステータス（EATING）かつ未払いでない場合はエラー
		if (customer.getStatus().getId() != CustomerDAO.STATUS_EATING) {
			throw new IllegalStateException("Customer is not in EATING status");
		}

		if (customer.getPaidAt() != null) {
			throw new IllegalStateException("Customer has already been paid");
		}

		// 未配膳商品をチェック
		if (!force) {
			final List<OrderDTO> orders = orderDAO.getOrdersByCustomerId(customerId, false);
			for (final OrderDTO order : orders) {
				if (order.getStatusId() != OrderDAO.STATUS_SERVED)
					throw new IllegalStateException("Customer has unserved orders");
			}
		}

		// ステータスを会計対象に変更
		customerDAO.updateStatusToCheckoutTarget(customerId);
	}

	/**
	 * 顧客の支払い時刻を設定するラッパー
	 *
	 * @param customerId 対象の顧客ID
	 */
	public void markCustomerPaid(UUID customerId) {
		customerDAO.markPaid(customerId);
	}
}
