package ua.foxminded.schoolapplication.util;

import java.sql.SQLException;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;

public final class SQLExceptionUtil {
	private SQLExceptionUtil() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	public static String extractSqlState(SQLException exception) {
		if (exception == null) {
			return DAOErrorCode.UNKNOWN_SQL_STATE;
		}

		String sqlState = exception.getSQLState();
		return (sqlState != null) ? sqlState : extractSqlState(exception.getNextException());
	}
}
