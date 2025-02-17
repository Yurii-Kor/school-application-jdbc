package ua.foxminded.schoolapplication.util;

public class PropertiesLoadingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PropertiesLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertiesLoadingException(String message) {
		super(message);
	}
}
