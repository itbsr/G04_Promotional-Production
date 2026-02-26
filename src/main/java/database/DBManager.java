package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	private static final String CN_STRING = buildConnectionString();
	private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "g2404";
	private static final String PASS = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "pass";
	private static final String DRIVER = "oracle.jdbc.OracleDriver";

	private static String buildConnectionString() {
		String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
		String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "1521";
		String dbSid = System.getenv("DB_SID") != null ? System.getenv("DB_SID") : "dbsys";
		return "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbSid;
	}

	private DBManager() {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			System.err.println("JDBCドライバのロードに失敗しました");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * インスタンスを取得するメソッド
	 *
	 * @return インスタンス
	 */
	public static DBManager getInstance() {
		return DBManagerHolder.INSTANCE;
	}

	/**
	 * インスタンスを保持する内部クラス。スレッドセーフなシングルトン実装
	 */
	private static class DBManagerHolder {
		private static final DBManager INSTANCE = new DBManager();
	}

	/**
	 * コネクションを取得
	 *
	 * @return 生成されたコネクション
	 * @throws SQLException
	 */
	Connection getConnection() throws SQLException {
		// TODO: 将来的にはコネクションプール導入を検討
		return DriverManager.getConnection(CN_STRING, USER, PASS);
	}
}
