package model.dto;

import java.io.Serializable;

public class NewUserDTO implements Serializable {
	private String username;
	private byte[] passwordHash;
	private byte[] salt;

	public NewUserDTO() {
	}

	public NewUserDTO(String username, byte[] passwordHash, byte[] salt) {
		super();
		this.username = username;
		this.passwordHash = passwordHash;
		this.salt = salt;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
}
