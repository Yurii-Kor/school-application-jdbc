package ua.foxminded.schoolapplication.model.dao.constants;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public enum DAOErrorCode {
	UNIQUE_VIOLATION("SQL_STATE_UNIQUE_VIOLATION"), FOREIGN_KEY_VIOLATION("SQL_STATE_FOREIGN_KEY_VIOLATION"),
	NULL_CONSTRAINT_VIOLATION("SQL_STATE_NULL_CONSTRAINT_VIOLATION");

	private static final String ERROR_CODES_FILE = "error_codes.json";
	private static final Map<String, String> errorCodes = new HashMap<>();

	static {
		loadErrorCodes();
	}

	private final String jsonKey;

	DAOErrorCode(String jsonKey) {
		this.jsonKey = jsonKey;
	}

	public String getCode() {
		String code = errorCodes.get(jsonKey);
		if (code == null) {
			throw new RuntimeException("No error code found for key: " + jsonKey);
		}
		return code;
	}

	private static void loadErrorCodes() {
		try (InputStream inputStream = DAOErrorCode.class.getClassLoader().getResourceAsStream(ERROR_CODES_FILE)) {
			if (inputStream == null) {
				throw new RuntimeException("Error codes file not found: " + ERROR_CODES_FILE);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			errorCodes.putAll(objectMapper.readValue(inputStream, Map.class));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load error codes from JSON", e);
		}
	}
}
