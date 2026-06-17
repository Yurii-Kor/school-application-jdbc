package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;
import ua.foxminded.schoolapplication.util.SQLExceptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentDao {
	private static final Logger logger = LoggerFactory.getLogger(StudentDao.class);

	private static final String INSERT_STUDENT = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.STUDENT_FIRST_NAME,
			DBSchemaConstants.STUDENT_LAST_NAME);

	private static final String FIND_STUDENT_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", DBSchemaConstants.STUDENTS_TABLE, DBSchemaConstants.STUDENT_ID);

	private static final String FIND_STUDENTS_BY_GROUP_ID = String.format("SELECT * FROM %s WHERE %s = ?",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID);

	private static final String UPDATE_STUDENT = String.format("UPDATE %s SET %s = ?, %s = ?, %s = ? WHERE %s = ?",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.STUDENT_FIRST_NAME,
			DBSchemaConstants.STUDENT_LAST_NAME,
			DBSchemaConstants.STUDENT_ID);

	private static final String DELETE_STUDENT = String
			.format("DELETE FROM %s WHERE %s = ?", DBSchemaConstants.STUDENTS_TABLE, DBSchemaConstants.STUDENT_ID);

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
	private static final int ONE_ROW_AFFECTED = 1;

	private final EntityValidator<Student> studentValidator;

	public StudentDao() {
		this.studentValidator = new EntityValidator<>();
	}

	public List<Student> addStudents(Student... students) throws DAOException {
		studentValidator.validateEntities(students);
		logger.debug("Add students: {}", Arrays.toString(students));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_STUDENT,
						Statement.RETURN_GENERATED_KEYS)) {

			for (Student student : students) {
				statement.setLong(INSERT_GROUP_ID_POSITION, student.getGroupId());
				statement.setString(INSERT_FIRST_NAME_POSITION, student.getFirstName());
				statement.setString(INSERT_LAST_NAME_POSITION, student.getLastName());
				statement.addBatch();
			}

			connection.setAutoCommit(false);

			try {
				statement.executeBatch();
				logger.debug("addStudents: Batch executed");
			} catch (SQLException e) {
				connection.rollback();
				connection.setAutoCommit(true);
				logger.debug("Transaction rolled back due to SQLException during 'addStudents'.");
				throw e;
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				int index = 0;
				while (generatedKeys.next() && index < students.length) {
					Long generatedId = generatedKeys.getLong(INSERT_RETRIVED_ID_POSITION);
					students[index].setStudentId(generatedId);
					index++;
				}

				if (index != students.length) {
					connection.rollback();
					connection.setAutoCommit(true);
					logger.warn("addStudents : Number of generated keys does not match number of inserted students.");
					throw new GroupIdDAOException(
							"Number of generated keys does not match number of inserted students.");
				}
			}

			connection.commit();
			connection.setAutoCommit(true);
			logger.info("All students added successfully.");
			return Arrays.asList(students);
		} catch (SQLException e) {
			SQLExceptionUtil.handleSQLException(e, getDAOExceptionMap(e));
			throw new RuntimeException("SQLException wasn't caught by Handler.", e);
		}
	}

	public Student findStudentById(Long studentId) throws DAOException {
		logger.debug("Searching for student with ID: {}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENT_BY_ID)) {

			statement.setLong(FIND_STUDENT_ID_POSITION, studentId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Long groupId = resultSet.getLong(DBSchemaConstants.STUDENT_GROUP_ID);
					String firstName = resultSet.getString(DBSchemaConstants.STUDENT_FIRST_NAME);
					String lastName = resultSet.getString(DBSchemaConstants.STUDENT_LAST_NAME);

					Student student = new Student(studentId, groupId, firstName, lastName);
					logger.info("Student found: {}", student);
					return student;
				}
			}

			logger.info("Student with ID {} not found.", studentId);
			throw new ObjectNotFoundDAOException("No student found with ID: " + studentId);
		} catch (SQLException e) {
			logger.error("Error finding student by ID: {}", studentId, e);
			throw new DAOException("Failed to find student by ID", e);
		}
	}

	public List<Student> findStudentsByGroupId(Long groupId) throws DAOException {
		logger.debug("Searching for students with group_id: {}", groupId);

		List<Student> students = new ArrayList<>();

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_GROUP_ID)) {

			statement.setLong(FIND_GROUP_ID_POSITION, groupId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Long studentId = resultSet.getLong(DBSchemaConstants.STUDENT_ID);
					Long groupIdFromDB = resultSet.getLong(DBSchemaConstants.STUDENT_GROUP_ID);
					String firstName = resultSet.getString(DBSchemaConstants.STUDENT_FIRST_NAME);
					String lastName = resultSet.getString(DBSchemaConstants.STUDENT_LAST_NAME);

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
		studentValidator.validateEntities(student);
		logger.debug("Updating student: {}", student);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_STUDENT)) {

			statement.setLong(UPDATE_GROUP_ID_POSITION, student.getGroupId());
			statement.setString(UPDATE_FIRST_NAME_POSITION, student.getFirstName());
			statement.setString(UPDATE_LAST_NAME_POSITION, student.getLastName());
			statement.setLong(UPDATE_STUDENT_ID_POSITION, student.getStudentId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("updateStudent : No student found with ID: {}", student.getStudentId());
				throw new ObjectNotFoundDAOException("No student found to update with ID: " + student.getStudentId());
			}

			logger.info("Student updated successfully: {}", student);

		} catch (SQLException e) {
			SQLExceptionUtil.handleSQLException(e, getDAOExceptionMap(e));
		}
	}

	public void deleteStudent(Long studentId) throws DAOException {
		logger.debug("Attempting to delete student with ID: {}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_STUDENT)) {
			statement.setLong(DELETE_STUDENT_ID_POSITION, studentId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("No student found to delete with ID: {}", studentId);
				throw new ObjectNotFoundDAOException("No group found to delete with ID: " + studentId);
			}
		} catch (SQLException e) {
			logger.error("Error deleting student with ID: {}", studentId, e);
			throw new DAOException("Failed to delete student with ID: " + studentId, e);
		}
	}

	private Map<String, DAOException> getDAOExceptionMap(SQLException e) {
		Map<String, DAOException> exceptionMap = new HashMap<>();

		exceptionMap.put(DAOErrorCode.FOREIGN_KEY_VIOLATION,
				new GroupIdDAOException("Invalid group id specified for student.", e));
		exceptionMap.put(DAOErrorCode.NULL_CONSTRAINT_VIOLATION,
				new ValidationException("A required field is missing (NULL constraint violation)", e));

		return exceptionMap;
	}
}
