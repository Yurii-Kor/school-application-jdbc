package ua.foxminded.schoolapplication.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesLoader {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

	private PropertiesLoader() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	public static Properties loadProperties(String fileName) throws PropertiesLoadingException {
		logger.debug("Loading properties from file: {}", fileName);
		Properties properties = new Properties();

		try (InputStream inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(fileName)) {
			if (inputStream == null) {
				logger.warn("Properties file not found: {}", fileName);
				throw new PropertiesLoadingException("Properties file not found: " + fileName);
			}

			properties.load(inputStream);
			logger.info("Properties loaded successfully from file: {}", fileName);
			return properties;
		} catch (IOException e) {
			logger.error("Failed to load properties file: {}", fileName, e);
			throw new PropertiesLoadingException("Failed to load properties file: " + fileName, e);
		}
	}
}
