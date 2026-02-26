package viewmodel;

import java.util.List;

import model.dto.CategoryDTO;
import model.dto.MenuDTO;

public class ManageMenuViewModel {
	private List<MenuDTO> manageMenuList;
	private List<CategoryDTO> categoryList;
	private List<String> images;

	public ManageMenuViewModel(List<MenuDTO> manageMenuList, List<CategoryDTO> categoryList,
			List<String> images) {
		this.manageMenuList = manageMenuList;
		this.categoryList = categoryList;
		this.images = images;
	}

	public void setManageMenuList(List<MenuDTO> manageMenuList) {
		this.manageMenuList = manageMenuList;
	}

	public List<MenuDTO> getManageMenuList() {
		return manageMenuList;
	}

	public void setCategoryList(List<CategoryDTO> categoryList) {
		this.categoryList = categoryList;
	}

	public List<CategoryDTO> getCategoryList() {
		return categoryList;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}
}
