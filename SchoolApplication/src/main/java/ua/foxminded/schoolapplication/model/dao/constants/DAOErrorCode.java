package ua.foxminded.schoolapplication.model.dao.constants;

public enum DAOErrorCode {
	UNIQUE_VIOLATION("23505"),
	FOREIGN_KEY_VIOLATION("23503"),
	NULL_CONSTRAINT_VIOLATION("23502");

	private final String exceptionCode;

	DAOErrorCode(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	public String getCode() {

		return exceptionCode;
	}
}
