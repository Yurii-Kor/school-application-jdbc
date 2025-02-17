package ua.foxminded.schoolapplication.model.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.util.PropertiesLoader;
import ua.foxminded.schoolapplication.util.PropertiesLoadingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static final String PROPERTIES_FILE = "hikari.properties";
	private static final HikariDataSource dataSource = createDataSource();

	private ConnectionPool() {
	}

	public static HikariDataSource getDataSource() {
		return dataSource;
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static void closeSource() {
		dataSource.close();
	}

	private static HikariDataSource createDataSource() {
		try {
			logger.debug("Creating HikariDataSource.");
			HikariConfig config = createConfig();
			HikariDataSource dataSource = new HikariDataSource(config);
			logger.info("HikariDataSource successfully created.");

			return dataSource;
		} catch (Exception e) {
			logger.error("Failed to create HikariDataSource", e);
			throw new DAOException("Failed to create HikariDataSource", e);
		}
	}

	private static HikariConfig createConfig() {
		logger.debug("Initializing HikariConfig for database connection pool.");

		try {
			Properties properties = loadProperties(PROPERTIES_FILE);
			HikariConfig config = new HikariConfig(properties);
			logger.debug("HikariConfig initialized with settings: jdbcUrl={}, username={}, maxPoolSize={}, minIdle={}",
					config.getJdbcUrl(),
					config.getUsername(),
					config.getMaximumPoolSize(),
					config.getMinimumIdle());

			return config;
		} catch (Exception e) {
			logger.error("HikariConfig : Failed to initialize HikariConfig", e);
			throw new DAOException("HikariConfig : Failed to initialize HikariConfig", e);
		}
	}

	private static Properties loadProperties(String fileName) {
		logger.debug("Loading properties from file: {}", fileName);

		try {
			return PropertiesLoader.loadProperties(fileName);
		} catch (PropertiesLoadingException e) {
			logger.error("HikariConfig : Failed to load properties file: {}", fileName, e);
			throw new DAOException("HikariConfig : Failed to load properties file: " + fileName, e);
		}
	}
}
