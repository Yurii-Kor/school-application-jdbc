package ua.foxminded.schoolapplication.util;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;

public final class SQLExceptionUtil {
	private static final Logger logger = LoggerFactory.getLogger(SQLExceptionUtil.class);

	private SQLExceptionUtil() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	public static void handleSQLException(SQLException e, Map<String, DAOException> exceptionMap) throws DAOException {
		String sqlState = extractSqlState(e);

		if (exceptionMap.containsKey(sqlState)) {
			DAOException exception = exceptionMap.get(sqlState);
			logger.warn("SQL Exception handled with specific exception: {}", exception.getClass().getSimpleName(), e);
			throw exception;
		}

		logger.error("Unhandled SQL exception: {}", e.getMessage(), e);
		throw new DAOException("Failed to execute database operation.", e);
	}

	private static String extractSqlState(SQLException exception) {
		if (exception == null) {
			return DAOErrorCode.UNKNOWN_SQL_STATE;
		}

		String sqlState = exception.getSQLState();
		return (sqlState != null) ? sqlState : extractSqlState(exception.getNextException());
	}
}
