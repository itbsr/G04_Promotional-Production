package model.dto;

public class UserDTO extends NewUserDTO {
	private int userId;

	public UserDTO() {
	}

	public UserDTO(int userId, String username, byte[] passwordHash, byte[] salt) {
		super(username, passwordHash, salt);
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}
