package ua.foxminded.schoolapplication.model.dao.exception;

public class StudentCourseAlreadyExistsDAOException extends DAOException {

	public StudentCourseAlreadyExistsDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public StudentCourseAlreadyExistsDAOException(String message) {
		super(message);
	}
}
