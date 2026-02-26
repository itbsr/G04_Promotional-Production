package util;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHashUtil {
	private static final String PASSWORD_PEPPER = "hard-coded-pepper-secret";

	public static byte[] hash(String password, byte[] salt) {
		try {
			char[] pw = (password + PASSWORD_PEPPER).toCharArray();

			PBEKeySpec spec = new PBEKeySpec(
					pw,
					salt,
					100_000, // iterations
					256);

			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

			byte[] hash = skf.generateSecret(spec).getEncoded();

			return hash;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ランダムなソルトを生成するメソッド
	 * 
	 * @return byte[16] 生成されたソルト
	 */
	public static byte[] generateSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);

		return salt;
	}
}
