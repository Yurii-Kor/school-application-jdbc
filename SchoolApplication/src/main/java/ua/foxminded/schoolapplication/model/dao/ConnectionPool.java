package ua.foxminded.schoolapplication.model.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ua.foxminded.schoolapplication.util.PropertiesLoader;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	private static final String POOL_CONFIG = "hikari.properties";
	private static final String MIGRATION_CONFIG = "flyway.properties";

	private static final HikariDataSource dataSource;

	static {
		try {
			dataSource = createDataSource();
			initializeDatabase();
		} catch (Exception e) {
			logger.error("Failed to initialize Connection Pool", e);
			throw new RuntimeException("Failed to initialize Connection Pool", e);
		}
	}

	private ConnectionPool() {
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static void closeSource() {
		dataSource.close();
	}

	private static HikariDataSource createDataSource() {
		logger.debug("Creating HikariDataSource.");

		Properties properties = PropertiesLoader.loadProperties(POOL_CONFIG);
		HikariDataSource dataSource = new HikariDataSource(new HikariConfig(properties));
		logger.info("HikariDataSource successfully created.");

		return dataSource;
	}

	private static void initializeDatabase() {
		logger.debug("Starting database migration.");

		Properties properties = PropertiesLoader.loadProperties(MIGRATION_CONFIG);
		Flyway flyway = Flyway.configure().dataSource(dataSource).configuration(properties).load();
		logger.debug("Flyway dataSource created successfully.");

		flyway.migrate();
		logger.info("Database migration completed successfully.");
	}
}
