package ua.foxminded.schoolapplication.model.dao.exception;

public class ValidationDAOException extends DAOException {
	private static final long serialVersionUID = 5L;

	public ValidationDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationDAOException(String message) {
		super(message);
	}
}
