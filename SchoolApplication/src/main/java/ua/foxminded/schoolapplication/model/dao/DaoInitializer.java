package ua.foxminded.schoolapplication.model.dao;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.util.PropertiesLoadingException;
import ua.foxminded.schoolapplication.util.PropertiesLoader;

import java.util.Properties;

public class DaoInitializer {
	private static final Logger logger = LoggerFactory.getLogger(DaoInitializer.class);
	private static final String CONFIG_FILE = "flyway.properties";

	public void initializeDatabase() throws DAOException {
		logger.debug("Starting database migration.");

		try {
			Flyway flyway = configureFlyway();

			flyway.migrate();
			logger.info("Database migration completed successfully.");
		} catch (Exception e) {
			logger.error("Failed to initialize database with Flyway", e);
			throw new DAOException("Failed to initialize database", e);
		}
	}

	private Flyway configureFlyway() {
		logger.debug("Configuring Flyway using configuration file: {}", CONFIG_FILE);

		try {
			Properties properties = loadProperties(CONFIG_FILE);
			return Flyway.configure().dataSource(ConnectionPool.getDataSource()).configuration(properties).load();
		} catch (Exception e) {
			logger.error("Flyway : Failed to configure Flyway", e);
			throw new DAOException("Flyway : Failed to configure Flyway", e);
		}
	}

	private Properties loadProperties(String fileName) {
		logger.debug("Loading properties from file: {}", fileName);

		try {
			return PropertiesLoader.loadProperties(fileName);
		} catch (PropertiesLoadingException e) {
			logger.error("Flyway : Failed to load properties file: {}", fileName, e);
			throw new DAOException("Flyway : Failed to load properties file: " + fileName, e);
		}
	}
}
