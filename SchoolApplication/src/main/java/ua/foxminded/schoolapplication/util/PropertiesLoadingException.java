package ua.foxminded.schoolapplication.util;

public class PropertiesLoadingException extends RuntimeException {

	public PropertiesLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertiesLoadingException(String message) {
		super(message);
	}
}
