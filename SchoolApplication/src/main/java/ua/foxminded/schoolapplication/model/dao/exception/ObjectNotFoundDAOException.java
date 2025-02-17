package ua.foxminded.schoolapplication.model.dao.exception;

public class ObjectNotFoundDAOException extends DAOException {
	private static final long serialVersionUID = 2L;

	public ObjectNotFoundDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundDAOException(String message) {
		super(message);
	}
}
