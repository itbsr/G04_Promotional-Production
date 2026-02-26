package model.service;

import java.util.List;

import database.OptionDAO;
import model.dto.NewOptionDTO;
import model.dto.OptionDTO;

/**
 * オプション情報に関する業務処理を行うServiceクラス
 * OptionDAOを使用してオプションデータを取得・提供する
 */
public class OptionService {

	private OptionDAO optionDAO;

	/**
	 * デフォルトコンストラクタ
	 */
	public OptionService() {
		this(new OptionDAO());
	}

	/**
	 * 引数ありコンストラクタ
	 *
	 * @param optionDAO オプションDAO
	 */
	public OptionService(OptionDAO optionDAO) {
		this.optionDAO = optionDAO;
	}

	/**
	 * オプションリンクのリストを取得するメソッド
	 *
	 * @return オプションリンクのリスト
	 */
	public List<OptionDTO> findOptionsLinkedToActiveMenus() {
		return optionDAO.findOptionsLinkedToActiveMenus();
	}

	/**
	 * 指定されたIDのオプション情報を取得するメソッド
	 * 
	 * @param optionId オプションID
	 * @return オプション情報DTO
	 */
	public OptionDTO findOptionById(int optionId) {
		return optionDAO.findById(optionId);
	}

	/**
	 * 指定されたメニューIDに関連するオプション情報を取得するメソッド
	 * 
	 * @param menuId メニューID
	 * @return オプション情報DTOのリスト
	 */
	public List<OptionDTO> findOptionsByMenuId(int menuId) {
		return optionDAO.findByMenuId(menuId);
	}

	/**
	 * すべてのオプション情報を取得するメソッド
	 * 
	 * @return オプション情報DTOのリスト
	 */
	public List<OptionDTO> getAllOptions() {
		return optionDAO.getAllOptions();
	}

	/**
	 * 新しいオプション情報を登録するメソッド
	 * 
	 * @param option オプション情報DTO
	 */
	public void registerOption(NewOptionDTO option) {
		optionDAO.registerOption(option);
	}

	/**
	 * 指定されたIDのオプション情報を更新するメソッド
	 * 
	 * @param option オプション情報DTO
	 */
	public void updateOption(int optionId, NewOptionDTO option) {
		optionDAO.updateOption(optionId, option);

	}

	/**
	 * 指定されたIDのオプション情報を削除するメソッド
	 * 
	 * @param optionIds オプションIDのリスト
	 * @return 削除されたオプションの数
	 */
	public int deleteOptions(List<Integer> optionIds) {
		return optionDAO.deleteOptions(optionIds);
	}
}
