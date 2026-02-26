package util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidUtils {
	public static final String REGEX_UUID_ANY_VERSION = "([0-9a-f]{8})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{12})";

	public static byte[] toBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	public static UUID fromBytes(byte[] bytes) {
		if (bytes == null || bytes.length != 16) {
			throw new IllegalArgumentException("UUID byte array must be 16 bytes");
		}
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long msb = bb.getLong();
		long lsb = bb.getLong();
		return new UUID(msb, lsb);
	}
}
