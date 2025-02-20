package ua.foxminded.schoolapplication.model.dao.exception;

public class StudentCourseAlreadyExistsDAOException extends DAOException {
	private static final long serialVersionUID = 7L;

	public StudentCourseAlreadyExistsDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public StudentCourseAlreadyExistsDAOException(String message) {
		super(message);
	}
}
