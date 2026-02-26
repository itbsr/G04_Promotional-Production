package viewmodel;

import java.util.List;
import java.util.Map;

import model.dto.CustomerDTO;
import model.dto.MenuDTO;
import model.dto.OptionDTO;
import model.dto.OrderDTO;

public class CheckoutViewModel {
	private CustomerDTO customer;
	private List<OrderDTO> orders;
	private Map<Integer, MenuDTO> menuMap;
	private Map<Integer, OptionDTO> optionMap;
	private List<Integer> orderTotalPrices;

	public CheckoutViewModel() {
	}

	public CheckoutViewModel(CustomerDTO customer, List<OrderDTO> orders, Map<Integer, MenuDTO> menuMap,
			Map<Integer, OptionDTO> optionMap,
			List<Integer> orderTotalPrices) {
		this.customer = customer;
		this.orders = orders;
		this.menuMap = menuMap;
		this.optionMap = optionMap;
		this.orderTotalPrices = orderTotalPrices;
	}

	public CustomerDTO getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerDTO customer) {
		this.customer = customer;
	}

	public List<OrderDTO> getOrders() {
		return orders;
	}

	public void setOrders(List<OrderDTO> orders) {
		this.orders = orders;
	}

	public Map<Integer, MenuDTO> getMenuMap() {
		return menuMap;
	}

	public void setMenuMap(Map<Integer, MenuDTO> menuMap) {
		this.menuMap = menuMap;
	}

	public Map<Integer, OptionDTO> getOptionMap() {
		return optionMap;
	}

	public void setOptionMap(Map<Integer, OptionDTO> optionMap) {
		this.optionMap = optionMap;
	}

	public List<Integer> getOrderTotalPrices() {
		return orderTotalPrices;
	}

	public void setOrderTotalPrices(List<Integer> orderTotalPrices) {
		this.orderTotalPrices = orderTotalPrices;
	}
}
