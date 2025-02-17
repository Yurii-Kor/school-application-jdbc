package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.NotFoundConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.model.validation.StudentValidator;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {
	private static final String TABLE = "students";
	private static final String COLUMN_STUDENT_ID = "student_id";
	private static final String COLUMN_GROUP_ID = "group_id";
	private static final String COLUMN_FIRST_NAME = "first_name";
	private static final String COLUMN_LAST_NAME = "last_name";

	private static final String INSERT_STUDENT = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
			TABLE,
			COLUMN_GROUP_ID,
			COLUMN_FIRST_NAME,
			COLUMN_LAST_NAME);

	private static final String FIND_STUDENT_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", TABLE, COLUMN_STUDENT_ID);

	private static final String FIND_STUDENTS_BY_GROUP_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", TABLE, COLUMN_GROUP_ID);

	private static final String UPDATE_STUDENT = String.format("UPDATE %s SET %s = ?, %s = ?, %s = ? WHERE %s = ?",
			TABLE,
			COLUMN_GROUP_ID,
			COLUMN_FIRST_NAME,
			COLUMN_LAST_NAME,
			COLUMN_STUDENT_ID);

	private static final String DELETE_STUDENT = String.format("DELETE FROM %s WHERE %s = ?", TABLE, COLUMN_STUDENT_ID);

	private static final int INSERT_GROUP_ID_POSITION = 1;
	private static final int INSERT_FIRST_NAME_POSITION = 2;
	private static final int INSERT_LAST_NAME_POSITION = 3;
	private static final int INSERT_RETRIVED_ID_POSITION = 1;
	private static final int FIND_STUDENT_ID_POSITION = 1;
	private static final int FIND_GROUP_ID_POSITION = 1;
	private static final int UPDATE_GROUP_ID_POSITION = 1;
	private static final int UPDATE_FIRST_NAME_POSITION = 2;
	private static final int UPDATE_LAST_NAME_POSITION = 3;
	private static final int UPDATE_STUDENT_ID_POSITION = 4;
	private static final int DELETE_STUDENT_ID_POSITION = 1;

	private static final int NOT_FOUND_ID = NotFoundConstants.NOT_FOUND.getId();
	private static final String NOT_FOUND_NAME = NotFoundConstants.NOT_FOUND.getName();
	private static final String SQL_STATE_FOREIGN_KEY_VIOLATION = DAOErrorCode.FOREIGN_KEY_VIOLATION.getCode();
	private static final String SQL_STATE_NULL_CONSTRAINT_VIOLATION = DAOErrorCode.NULL_CONSTRAINT_VIOLATION.getCode();

	private static final Logger logger = LoggerFactory.getLogger(StudentDao.class);

	private final StudentValidator studentValidator;

	public StudentDao() {
		this.studentValidator = new StudentValidator();
	}

	public void addStudents(Student... students) throws DAOException {
		validateInputStudents(students);
		logger.debug("Add students: {}", Arrays.toString(students));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_STUDENT,
						Statement.RETURN_GENERATED_KEYS)) {

			connection.setAutoCommit(false);

			try {
				for (Student student : students) {
					statement.setInt(INSERT_GROUP_ID_POSITION, student.getGroupId());
					statement.setString(INSERT_FIRST_NAME_POSITION, student.getFirstName());
					statement.setString(INSERT_LAST_NAME_POSITION, student.getLastName());
					statement.addBatch();
				}

				statement.executeBatch();
				logger.debug("addStudents: Batch executed");

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					int index = 0;
					while (generatedKeys.next() && index < students.length) {
						int generatedId = generatedKeys.getInt(INSERT_RETRIVED_ID_POSITION);
						students[index].setStudentId(generatedId);
						index++;
					}

					if (index != students.length) {
						logger.warn(
								"addStudents : Number of generated keys does not match number of inserted students.");
						throw new GroupIdDAOException(
								"Number of generated keys does not match number of inserted students.");
					}
				}

				connection.commit();
				logger.info("All students added successfully.");
			} catch (SQLException e) {
				try {
					connection.rollback();
					logger.debug("Transaction rolled back due to SQLException during 'addStudents'.");
				} catch (SQLException rollbackEx) {
					logger.error("Failed to rollback transaction during 'addStudents'.", rollbackEx);
				}

				if (e instanceof BatchUpdateException && e.getNextException() != null) {
					e = e.getNextException();
				}

				if (SQL_STATE_FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
					logger.warn("Foreign key violation: Invalid group_id specified during 'addStudents'.", e);
					throw new GroupIdDAOException("Invalid group id specified for student.", e);
				}

				if (SQL_STATE_NULL_CONSTRAINT_VIOLATION.equals(e.getSQLState())) {
					logger.warn("addStudents : A required field is missing (NULL constraint violation)", e);
					throw new ValidationDAOException("A required field is missing (NULL constraint violation)", e);
				}

				logger.error("Failed to add students: {}", e.getMessage(), e);
				throw new DAOException("Failed to add students.", e);
			}
		} catch (SQLException e) {
			logger.error("Failed to establish connection or prepare statement.", e);
			throw new DAOException("Failed to add students due to connection issues.", e);
		}
	}

	public Student findStudentById(int studentId) throws DAOException {
		logger.debug("Searching for student with ID: {}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENT_BY_ID)) {

			statement.setInt(FIND_STUDENT_ID_POSITION, studentId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					int groupId = resultSet.getInt(COLUMN_GROUP_ID);
					String firstName = resultSet.getString(COLUMN_FIRST_NAME);
					String lastName = resultSet.getString(COLUMN_LAST_NAME);

					Student student = new Student(studentId, groupId, firstName, lastName);
					logger.info("Student found: {}", student);
					return student;
				}
			}

			logger.info("Student with ID {} not found.", studentId);
			return new Student(NOT_FOUND_ID, NOT_FOUND_ID, NOT_FOUND_NAME, NOT_FOUND_NAME);
		} catch (SQLException e) {
			logger.error("Error finding student by ID: {}", studentId, e);
			throw new DAOException("Failed to find student by ID", e);
		}
	}

	public List<Student> findStudentsByGroupId(int groupId) throws DAOException {
		logger.debug("Searching for students with group_id: {}", groupId);

		List<Student> students = new ArrayList<>();

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_GROUP_ID)) {

			statement.setInt(FIND_GROUP_ID_POSITION, groupId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int studentId = resultSet.getInt(COLUMN_STUDENT_ID);
					int groupIdFromDB = resultSet.getInt(COLUMN_GROUP_ID);
					String firstName = resultSet.getString(COLUMN_FIRST_NAME);
					String lastName = resultSet.getString(COLUMN_LAST_NAME);

					Student student = new Student(studentId, groupIdFromDB, firstName, lastName);
					students.add(student);
				}
			}

			logger.info("Found {} students for group_id: {}", students.size(), groupId);
			return students;
		} catch (SQLException e) {
			logger.error("Error finding students by group_id: {}", groupId, e);
			throw new DAOException("Failed to find students by group id: " + groupId, e);
		}
	}

	public void updateStudent(Student student) throws DAOException {
		validateInputStudents(student);
		logger.debug("Updating student: {}", student);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_STUDENT)) {

			statement.setInt(UPDATE_GROUP_ID_POSITION, student.getGroupId());
			statement.setString(UPDATE_FIRST_NAME_POSITION, student.getFirstName());
			statement.setString(UPDATE_LAST_NAME_POSITION, student.getLastName());
			statement.setInt(UPDATE_STUDENT_ID_POSITION, student.getStudentId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("updateStudent : No student found with ID: {}", student.getStudentId());
				throw new ObjectNotFoundDAOException("No student found to update with ID: " + student.getStudentId());
			}

			logger.info("Student updated successfully: {}", student);

		} catch (SQLException e) {
			if (SQL_STATE_FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
				logger.warn("Foreign key violation: Invalid group_id specified during 'updateStudent'", e);
				throw new GroupIdDAOException("Invalid group id specified for student.", e);
			}

			if (SQL_STATE_NULL_CONSTRAINT_VIOLATION.equals(e.getSQLState())) {
				logger.warn("updateStudent : A required field is missing (NULL constraint violation)", e);
				throw new ValidationDAOException("A required field is missing (NULL constraint violation)", e);
			}

			logger.error("Error updating student: {}", student, e);
			throw new DAOException("Failed to update student", e);
		}
	}

	public void deleteStudent(int studentId) throws DAOException {
		logger.debug("Attempting to delete student with ID: {}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_STUDENT)) {
			statement.setInt(DELETE_STUDENT_ID_POSITION, studentId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("No student found to delete with ID: {}", studentId);
				throw new ObjectNotFoundDAOException("No group found to delete with ID: " + studentId);
			}
		} catch (SQLException e) {
			logger.error("Error deleting student with ID: {}", studentId, e);
			throw new DAOException("Failed to delete student with ID: " + studentId, e);
		}
	}

	private void validateInputStudents(Student... students) {
		if (!studentValidator.validateStudents(students)) {
			String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
			String errorMessage = String.format("Method '%s' received invalid data", methodName);
			logger.warn(errorMessage);
			throw new ValidationDAOException(errorMessage);
		}
	}
}
