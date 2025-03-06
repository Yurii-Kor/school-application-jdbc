package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.StudentCourseAlreadyExistsDAOException;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.util.SQLExceptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentsCoursesDao {
	private static final Logger logger = LoggerFactory.getLogger(StudentsCoursesDao.class);

	private static final String INSERT_STUDENTS_COURSE = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID);

	private static final String DELETE_STUDENTS_COURSE = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID);

	private static final String FIND_COURSE_IDS_BY_STUDENT = String.format("SELECT %s FROM %s WHERE %s = ?",
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID,
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID);

	private static final String FIND_STUDENT_IDS_BY_COURSE = String.format("SELECT %s FROM %s WHERE %s = ?",
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID);

	private static final String FIND_STUDENTS_BY_COURSE_NAME = String.format(
			"SELECT s.* FROM %s s JOIN %s sc ON s.%s = sc.%s JOIN %s c ON sc.%s = c.%s WHERE c.%s = ?",

			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID,
			DBSchemaConstants.COURSE_ID,
			DBSchemaConstants.COURSE_NAME);

	private static final int INSERT_STUDENT_ID_POSITION = 1;
	private static final int INSERT_COURSE_ID_POSITION = 2;
	private static final int DELETE_STUDENT_ID_POSITION = 1;
	private static final int DELETE_COURSE_ID_POSITION = 2;
	private static final int FIND_STUDENT_ID_POSITION = 1;
	private static final int FIND_COURSE_ID_POSITION = 1;
	private static final int FIND_COURSE_NAME_POSITION = 1;
	private static final int ONE_ROW_AFFECTED = 1;

	public void addStudentCourse(Long studentId, Long courseId) throws DAOException {
		logger.debug("Adding students_course relation: studentId={}, courseId={}", studentId, courseId);
		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_STUDENTS_COURSE)) {

			statement.setLong(INSERT_STUDENT_ID_POSITION, studentId);
			statement.setLong(INSERT_COURSE_ID_POSITION, courseId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("No rows inserted for relation: studentId={}, courseId={}", studentId, courseId);
				throw new DAOException("Failed to add students_course relation.");
			}

			logger.info("students_course relation added successfully: studentId={}, courseId={}", studentId, courseId);
		} catch (SQLException e) {
			if (DAOErrorCode.UNIQUE_VIOLATION.equals(SQLExceptionUtil.extractSqlState(e))) {
				logger.warn("Duplicate relation detected for studentId={}, courseId={}", studentId, courseId, e);
				throw new StudentCourseAlreadyExistsDAOException("The student-course relation already exists.", e);
			}

			logger.error("Error adding students_course relation: studentId={}, courseId={}", studentId, courseId, e);
			throw new DAOException("Failed to add students_course relation.", e);
		}
	}

	public void deleteStudentCourse(Long studentId, Long courseId) throws DAOException {
		logger.debug("Deleting students_course relation: studentId={}, courseId={}", studentId, courseId);
		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_STUDENTS_COURSE)) {

			statement.setLong(DELETE_STUDENT_ID_POSITION, studentId);
			statement.setLong(DELETE_COURSE_ID_POSITION, courseId);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("No relation found to delete for studentId={}, courseId={}", studentId, courseId);
				throw new ObjectNotFoundDAOException("No students_course relation found to delete.");
			}

			logger.info("relation deleted successfully: studentId={}, courseId={}", studentId, courseId);
		} catch (SQLException e) {
			logger.error("Error deleting students_course relation: studentId={}, courseId={}", studentId, courseId, e);
			throw new DAOException("Failed to delete students_course relation.", e);
		}
	}

	public List<Long> findCourseIdsByStudentId(Long studentId) throws DAOException {
		List<Long> courseIds = new ArrayList<>();
		logger.debug("Finding course IDs for studentId={}", studentId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_COURSE_IDS_BY_STUDENT)) {

			statement.setLong(FIND_STUDENT_ID_POSITION, studentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					courseIds.add(resultSet.getLong(DBSchemaConstants.STUDENTS_COURSES_COURSE_ID));
				}
			}

			logger.info("Found {} course IDs for studentId={}", courseIds.size(), studentId);
			return courseIds;
		} catch (SQLException e) {
			logger.error("Error finding course IDs for studentId={}", studentId, e);
			throw new DAOException("Failed to find course IDs for student.", e);
		}
	}

	public List<Long> findStudentIdsByCourseId(Long courseId) throws DAOException {
		List<Long> studentIds = new ArrayList<>();
		logger.debug("Finding student IDs for courseId={}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENT_IDS_BY_COURSE)) {

			statement.setLong(FIND_COURSE_ID_POSITION, courseId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					studentIds.add(resultSet.getLong(DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID));
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
					Long studentId = resultSet.getLong(DBSchemaConstants.STUDENT_ID);
					Long groupId = resultSet.getLong(DBSchemaConstants.STUDENT_GROUP_ID);
					String firstName = resultSet.getString(DBSchemaConstants.STUDENT_FIRST_NAME);
					String lastName = resultSet.getString(DBSchemaConstants.STUDENT_LAST_NAME);

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
