package model.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import database.CustomerDAO;
import database.MenuDAO;
import database.OptionDAO;
import database.OrderDAO;
import model.dto.CustomerDTO;
import model.dto.DeviceDTO;
import model.dto.MenuDTO;
import model.dto.NewOrderDTO;
import model.dto.NewOrderWithIdDTO;
import model.dto.OptionDTO;
import model.dto.OrderDTO;

public class OrderService {
	private OrderDAO orderDAO;
	private CustomerDAO customerDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public OrderService() {
		this(new OrderDAO(), new CustomerDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 * 
	 * @param orderDAO 注文DAO
	 * @param customerDAO 顧客DAO
	 */
	public OrderService(OrderDAO orderDAO, CustomerDAO customerDAO) {
		this.orderDAO = orderDAO;
		this.customerDAO = customerDAO;
	}

	/**
	 * 注文を登録するメソッド
	 * 
	 * @param newOrders  新規注文リスト
	 * @param device デバイス情報
	 * @return 登録された注文リスト
	 */
	public List<OrderDTO> registerOrder(List<NewOrderDTO> newOrders, DeviceDTO device) {
		final List<NewOrderWithIdDTO> orders = new ArrayList<>();
		for (NewOrderDTO newOrder : newOrders) {
			final UUID uuid = Generators.timeBasedEpochGenerator().generate();
			final NewOrderWithIdDTO order = new NewOrderWithIdDTO(
					newOrder.getMenuId(),
					newOrder.getOptionIds(),
					uuid);
			orders.add(order);
		}

		final CustomerDTO customer = customerDAO.getAllocatedCustomerByDevice(device);
		final boolean result = orderDAO.registerOrder(orders, customer.getCustomerId());
		if (result) {
			final List<OrderDTO> registeredOrders = new ArrayList<>();
			for (NewOrderWithIdDTO order : orders) {
				final OrderDTO registeredOrder = orderDAO.getOrderById(order.getOrderId());
				registeredOrders.add(registeredOrder);
			}

			return registeredOrders;
		} else {
			return null;
		}
	}

	/**
	 * 指定された顧客IDの注文履歴を取得するメソッド
	 * 
	 * @param customerId 顧客ID
	 * @param shouldUseCustomerStatusName trueなら顧客向け表示名、falseならスタッフ向け表示名を取得
	 * @return 注文履歴のリスト
	 */
	public List<OrderDTO> getOrdersByCustomerId(UUID customerId, boolean shouldUseCustomerStatusName) {
		return orderDAO.getOrdersByCustomerId(customerId, shouldUseCustomerStatusName);
	}

	/**
	 * 指定されたステータスIDの注文一覧を取得するメソッド
	 * 
	 * @param statusId 注文ステータスID
	 * @return 注文リスト
	 */
	public List<OrderDTO> getOrdersByStatusId(int statusId) {
		return orderDAO.getOrdersByStatusId(statusId);
	}

	/**
	 * 注文ステータスを更新するメソッド
	 * 
	 * @param orderId 注文ID
	 * @param statusId 新しいステータスID
	 * @return 更新後の注文DTO、失敗した場合はnull
	 */
	public OrderDTO updateOrderStatus(UUID orderId, int statusId) {
		// ステータスIDの妥当性チェック
		if (statusId != OrderDAO.STATUS_ORDERED
				&& statusId != OrderDAO.STATUS_COOKED
				&& statusId != OrderDAO.STATUS_SERVED) {
			return null;
		}

		// 注文が存在するかチェック
		orderDAO.getOrderById(orderId);

		// ステータスを更新
		orderDAO.updateOrderStatus(orderId, statusId);

		// 更新後の注文を取得して返却
		return orderDAO.getOrderById(orderId);
	}

	/**
	 * 顧客IDに紐づく注文を取得し、メニュー／オプションを引数のMapに詰めて返す。
	 *
	 * @param customerId 顧客ID
	 * @param menus 渡されたMapに対し、注文に含まれるメニューを追加格納して返す
	 * @param options 渡されたMapに対し、注文に含まれるオプションを追加格納して返す
	 * @param shouldUseCustomerStatusName trueなら顧客向け表示名、falseならスタッフ向け表示名を取得
	 * @return 注文リスト
	 */
	public List<OrderDTO> getOrdersByCustomerId(UUID customerId, Map<Integer, MenuDTO> menus,
			Map<Integer, OptionDTO> options, boolean shouldUseCustomerStatusName) {
		final List<OrderDTO> orders = orderDAO.getOrdersByCustomerId(customerId, shouldUseCustomerStatusName);

		if (menus != null) {
			final Set<Integer> menuIds = new HashSet<>();
			for (OrderDTO order : orders)
				menuIds.add(order.getMenuId());

			final MenuDAO menuDAO = new MenuDAO();
			for (Integer menuId : menuIds)
				menus.put(menuId, menuDAO.getMenuById(menuId));
		}

		if (options != null) {
			final Set<Integer> optionIds = new HashSet<>();
			for (OrderDTO order : orders)
				optionIds.addAll(order.getOptionIds());

			final OptionDAO optionDAO = new OptionDAO();
			for (Integer optionId : optionIds)
				options.put(optionId, optionDAO.findById(optionId));
		}

		return orders;
	}
}
