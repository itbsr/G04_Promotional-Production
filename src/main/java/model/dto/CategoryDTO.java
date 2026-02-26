package model.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * カテゴリー情報を保持するDTOクラス。
 */
public class CategoryDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	List<CategoryDTO> children = new ArrayList<>();

	public CategoryDTO(int id, String name, Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int categoryId) {
		this.id = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String categoryName) {
		this.name = categoryName;
	}

	public List<CategoryDTO> getChildren() {
		return children;
	}

	public void setChildren(List<CategoryDTO> children) {
		this.children = children;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
}
