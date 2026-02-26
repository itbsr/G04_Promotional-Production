package model.service;

import java.util.List;

import database.CategoryDAO;
import model.dto.CategoryDTO;

public class CategoryService {
	public List<CategoryDTO> getAllCategories() {
		CategoryDAO categoryDAO = new CategoryDAO();
		return categoryDAO.getAllCategories();
	}
}
