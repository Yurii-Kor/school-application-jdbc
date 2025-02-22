package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.StudentCourseAlreadyExistsDAOException;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentsCoursesDao {
	private static final Logger logger = LoggerFactory.getLogger(StudentsCoursesDao.class);

	private static final String TABLE = DBSchemaConstants.STUDENTS_COURSES_TABLE.getValue();
	private static final String COLUMN_STUDENT_ID = DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID.getValue();
	private static final String COLUMN_COURSE_ID = DBSchemaConstants.STUDENTS_COURSES_COURSE_ID.getValue();

	private static final String TABLE_STUDENTS = DBSchemaConstants.STUDENTS_TABLE.getValue();
	private static final String STUDENTS_PRIMARY_KEY = DBSchemaConstants.STUDENT_ID.getValue();
	private static final String STUDENTS_GROUP_ID = DBSchemaConstants.STUDENT_GROUP_ID.getValue();
	private static final String STUDENTS_FIRST_NAME = DBSchemaConstants.STUDENT_FIRST_NAME.getValue();
	private static final String STUDENTS_LAST_NAME = DBSchemaConstants.STUDENT_LAST_NAME.getValue();

	private static final String TABLE_COURSES = DBSchemaConstants.COURSES_TABLE.getValue();
	private static final String COURSES_PRIMARY_KEY = DBSchemaConstants.COURSE_ID.getValue();
	private static final String COURSES_SECONDARY_KEY = DBSchemaConstants.COURSE_NAME.getValue();

	private static final String INSERT_STUDENTS_COURSE = String
			.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", TABLE, COLUMN_STUDENT_ID, COLUMN_COURSE_ID);
	private static final String DELETE_STUDENTS_COURSE = String
			.format("DELETE FROM %s WHERE %s = ? AND %s = ?", TABLE, COLUMN_STUDENT_ID, COLUMN_COURSE_ID);
	private static final String FIND_COURSE_IDS_BY_STUDENT = String
			.format("SELECT %s FROM %s WHERE %s = ?", COLUMN_COURSE_ID, TABLE, COLUMN_STUDENT_ID);
	private static final String FIND_STUDENT_IDS_BY_COURSE = String
			.format("SELECT %s FROM %s WHERE %s = ?", COLUMN_STUDENT_ID, TABLE, COLUMN_COURSE_ID);
	private static final String FIND_STUDENTS_BY_COURSE_NAME = String.format(
			"SELECT s.* FROM %s s JOIN %s sc ON s.%s = sc.%s JOIN %s c ON sc.%s = c.%s WHERE c.%s = ?",
			TABLE_STUDENTS,
			TABLE,
			STUDENTS_PRIMARY_KEY,
			COLUMN_STUDENT_ID,
			TABLE_COURSES,
			COLUMN_COURSE_ID,
			COURSES_PRIMARY_KEY,
			COURSES_SECONDARY_KEY);

	private static final int INSERT_STUDENT_ID_POSITION = 1;
	private static final int INSERT_COURSE_ID_POSITION = 2;
	private static final int DELETE_STUDENT_ID_POSITION = 1;
	private static final int DELETE_COURSE_ID_POSITION = 2;
	private static final int FIND_STUDENT_ID_POSITION = 1;
	private static final int FIND_COURSE_ID_POSITION = 1;
	private static final int FIND_COURSE_NAME_POSITION = 1;

	private static final String SQL_STATE_UNIQUE_VIOLATION = DAOErrorCode.UNIQUE_VIOLATION.getCode();

	public void addStudentCourse(int studentId, int courseId) throws DAOException {
		logger.debug("Adding students_course relation: studentId={}, courseId={}", studentId, courseId);
		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_STUDENTS_COURSE)) {

			statement.setInt(INSERT_STUDENT_ID_POSITION, studentId);
			statement.setInt(INSERT_COURSE_ID_POSITION, courseId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("No rows inserted for relation: studentId={}, courseId={}", studentId, courseId);
				throw new DAOException("Failed to add students_course relation.");
			}

			logger.info("students_course relation added successfully: studentId={}, courseId={}", studentId, courseId);
		} catch (SQLException e) {
			if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
				logger.warn("Duplicate relation detected for studentId={}, courseId={}", studentId, courseId, e);
				throw new StudentCourseAlreadyExistsDAOException("The student-course relation already exists.", e);
			}

			logger.error("Error adding students_course relation: studentId={}, courseId={}", studentId, courseId, e);
			throw new DAOException("Failed to add students_course relation.", e);
		}
	}

	public void deleteStudentCourse(int studentId, int courseId) throws DAOException {
		logger.debug("Deleting students_course relation: studentId={}, courseId={}", studentId, courseId);
		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_STUDENTS_COURSE)) {

			statement.setInt(DELETE_STUDENT_ID_POSITION, studentId);
			statement.setInt(DELETE_COURSE_ID_POSITION, courseId);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("No relation found to delete for studentId={}, courseId={}", studentId, courseId);
				throw new ObjectNotFoundDAOException("No students_course relation found to delete.");
			}

			logger.info("relation deleted successfully: studentId={}, courseId={}", studentId, courseId);
		} catch (SQLException e) {
			logger.error("Error deleting students_course relation: studentId={}, courseId={}", studentId, courseId, e);
			throw new DAOException("Failed to delete students_course relation.", e);
		}
	}

	public List<Integer> findCourseIdsByStudentId(int studentId) throws DAOException {
		List<Integer> courseIds = new ArrayList<>();
		logger.debug("Finding course IDs for studentId={}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_COURSE_IDS_BY_STUDENT)) {

			statement.setInt(FIND_STUDENT_ID_POSITION, studentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					courseIds.add(resultSet.getInt(COLUMN_COURSE_ID));
				}
			}

			logger.info("Found {} course IDs for studentId={}", courseIds.size(), studentId);
			return courseIds;
		} catch (SQLException e) {
			logger.error("Error finding course IDs for studentId={}", studentId, e);
			throw new DAOException("Failed to find course IDs for student.", e);
		}
	}

	public List<Integer> findStudentIdsByCourseId(int courseId) throws DAOException {
		List<Integer> studentIds = new ArrayList<>();
		logger.debug("Finding student IDs for courseId={}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENT_IDS_BY_COURSE)) {

			statement.setInt(FIND_COURSE_ID_POSITION, courseId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					studentIds.add(resultSet.getInt(COLUMN_STUDENT_ID));
				}
			}

			logger.info("Found {} student IDs for courseId={}", studentIds.size(), courseId);
			return studentIds;
		} catch (SQLException e) {
			logger.error("Error finding student IDs for courseId={}", courseId, e);
			throw new DAOException("Failed to find student IDs for course.", e);
		}
	}

	public List<Student> findStudentsByCourseName(String courseName) throws DAOException {
		List<Student> students = new ArrayList<>();
		logger.debug("Finding student IDs for course: {}", courseName);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_COURSE_NAME)) {

			statement.setString(FIND_COURSE_NAME_POSITION, courseName);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int studentId = resultSet.getInt(STUDENTS_PRIMARY_KEY);
					int groupId = resultSet.getInt(STUDENTS_GROUP_ID);
					String firstName = resultSet.getString(STUDENTS_FIRST_NAME);
					String lastName = resultSet.getString(STUDENTS_LAST_NAME);

					students.add(new Student(studentId, groupId, firstName, lastName));
				}
			}

			logger.info("Found {} students for course '{}'", students.size(), courseName);
			return students;
		} catch (SQLException e) {
			logger.error("Error finding students for course '{}'", courseName, e);
			throw new DAOException("Failed to find students by course name: " + courseName, e);
		}
	}
}
