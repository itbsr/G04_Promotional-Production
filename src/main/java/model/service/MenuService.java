package model.service;

import java.util.List;

import database.MenuDAO;
import model.dto.MenuDTO;
import model.dto.NewMenuDTO;

/**
 * メニュー情報に関する業務処理を行うServiceクラス MenuDAOを使用してメニューデータを取得・提供する
 */
public class MenuService {

	private MenuDAO menuDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public MenuService() {
		this(new MenuDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 * 
	 * @param menuDAO     メニューDAO
	 * @param categoryDAO カテゴリーDAO
	 */
	public MenuService(MenuDAO menuDAO) {
		this.menuDAO = menuDAO;
	}

	/**
	 * メニュー一覧を取得するメソッド
	 *
	 * @return 有効なメニューのMenuDTOのリスト
	 */
	public List<MenuDTO> getAvailableMenu() {
		return menuDAO.getAvailableMenu();
	}

	/**
	 * すべてのメニュー一覧を取得するメソッド
	 * 
	 * @return MenuDTOのリスト
	 */
	public List<MenuDTO> getMenuAll() {
		return menuDAO.getMenuAll();
	}

	/**
	 * 指定されたIDのメニューを取得するメソッド
	 * 
	 * @param menuId メニューID
	 * @return MenuDTO メニューが存在しない場合はnull
	 */
	public MenuDTO getMenuById(int menuId) {
		return menuDAO.getMenuById(menuId);
	}

	/**
	 * メニュー登録を行うメソッド
	 * 
	 * @param newMenu 登録するメニュー情報
	 * @return 登録されたメニュー情報、登録失敗時はnull
	 */
	public MenuDTO registerMenu(NewMenuDTO newMenu) {
		return menuDAO.registerMenu(newMenu);
	}

	/**
	 * メニュー更新を行うメソッド
	 * 
	 * @param menuId     更新するメニューID
	 * @param updateMenu 更新するメニュー情報
	 * @return 更新成功:true, 更新失敗:false
	 */
	public int updateMenu(int menuId, NewMenuDTO updateMenu) {
		return menuDAO.updateMenu(menuId, updateMenu);
	}

	public int deleteMenus(List<Integer> menuIds) {
		return menuDAO.deleteMenus(menuIds);
	}
}
