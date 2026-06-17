package ua.foxminded.schoolapplication.model.dao.exception;

public class ValidationException extends DAOException {

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}
}
