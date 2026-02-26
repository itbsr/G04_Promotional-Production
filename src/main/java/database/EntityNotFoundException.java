package database;

/**
 * DBアクセスの際対象エンティティが見つからなかった場合にスローされる例外クラス
 */
public class EntityNotFoundException extends DataAccessException {
	private static final long serialVersionUID = 1L;

	private Object entityId;

	public EntityNotFoundException(String message) {
		super(message);
	}

	public EntityNotFoundException(String message, Object entityId) {
		super(message);
		this.entityId = entityId;
	}

	public Object getEntityId() {
		return entityId;
	}
}
