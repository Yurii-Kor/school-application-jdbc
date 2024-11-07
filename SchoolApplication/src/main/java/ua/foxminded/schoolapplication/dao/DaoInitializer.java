package ua.foxminded.schoolapplication.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DaoInitializer {
	private static final Logger logger = LoggerFactory.getLogger(DaoInitializer.class);
	private static final String SQL_FILE_PATH = "initialize_database.sql";

	public void initializeDatabase() throws DAOException {
		logger.debug("Starting database initialization.");

		String sqlCommands = loadSqlFile(SQL_FILE_PATH);
		executeSqlCommands(sqlCommands);

		logger.info("Database initialization completed successfully.");
	}

	private String loadSqlFile(String filePath) {
		logger.debug("Loading SQL file: {}", filePath);

		try (InputStream inputStream = DaoInitializer.class.getClassLoader().getResourceAsStream(filePath);
				InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {

			if (inputStream == null) {
				logger.warn("SQL file not found: {}", filePath);
				throw new DAOException("SQL file not found: " + filePath);
			}

			String sql = reader.lines().collect(Collectors.joining(System.lineSeparator()));
			logger.info("SQL file loaded successfully: {}", filePath);

			return sql;
		} catch (Exception e) {
			logger.error("Failed to load SQL file: {}", filePath, e);
			throw new DAOException("Failed to load SQL file: " + filePath, e);
		}
	}

	private void executeSqlCommands(String sqlCommands) {
		logger.debug("Executing SQL commands for database initialization.");

		try (Connection conn = ConnectionPool.getInstance().getConnection(); Statement stmt = conn.createStatement()) {

			stmt.execute(sqlCommands);
			logger.info("SQL commands executed successfully for database initialization.");

		} catch (Exception e) {
			logger.error("Failed to execute SQL commands for database initialization.", e);
			throw new DAOException("Failed to execute SQL commands", e);
		}
	}
}
