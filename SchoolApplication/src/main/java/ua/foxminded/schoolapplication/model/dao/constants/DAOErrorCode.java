package ua.foxminded.schoolapplication.model.dao.constants;

public class DAOErrorCode {
    public static final String UNIQUE_VIOLATION = "23505";
    public static final String FOREIGN_KEY_VIOLATION = "23503";
    public static final String NULL_CONSTRAINT_VIOLATION = "23502";
    public static final String UNKNOWN_SQL_STATE = "UNKNOWN_SQL_STATE";

    private DAOErrorCode() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
