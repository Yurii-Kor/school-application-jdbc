package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.NotFoundConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.CourseNameDAOException;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.validation.CourseValidator;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class CourseDao {
	private static final String TABLE = "courses";
	private static final String COLUMN_COURSE_ID = "course_id";
	private static final String COLUMN_COURSE_NAME = "course_name";
	private static final String COLUMN_COURSE_DESCRIPTION = "course_description";

	private static final String INSERT_COURSE = String
			.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", TABLE, COLUMN_COURSE_NAME, COLUMN_COURSE_DESCRIPTION);

	private static final String FIND_COURSE_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", TABLE, COLUMN_COURSE_ID);

	private static final String UPDATE_COURSE = String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
			TABLE,
			COLUMN_COURSE_NAME,
			COLUMN_COURSE_DESCRIPTION,
			COLUMN_COURSE_ID);

	private static final String DELETE_COURSE = String.format("DELETE FROM %s WHERE %s = ?", TABLE, COLUMN_COURSE_ID);

	private static final int INSERT_COURSE_NAME_POSITION = 1;
	private static final int INSERT_COURSE_DESCRIPTION_POSITION = 2;
	private static final int INSERT_RETRIEVED_ID_POSITION = 1;
	private static final int FIND_COURSE_ID_POSITION = 1;
	private static final int UPDATE_COURSE_NAME_POSITION = 1;
	private static final int UPDATE_COURSE_DESCRIPTION_POSITION = 2;
	private static final int UPDATE_COURSE_ID_POSITION = 3;
	private static final int DELETE_COURSE_ID_POSITION = 1;

	private static final int NOT_FOUND_ID = NotFoundConstants.NOT_FOUND.getId();
	private static final String NOT_FOUND_NAME = NotFoundConstants.NOT_FOUND.getName();

	private static final String SQL_STATE_UNIQUE_VIOLATION = DAOErrorCode.UNIQUE_VIOLATION.getCode();
	private static final String SQL_STATE_NULL_CONSTRAINT_VIOLATION = DAOErrorCode.NULL_CONSTRAINT_VIOLATION.getCode();

	private static final Logger logger = LoggerFactory.getLogger(CourseDao.class);

	private final CourseValidator courseValidator;

	public CourseDao() {
		this.courseValidator = new CourseValidator();
	}

	public void addCourses(Course... courses) throws DAOException {
		validateInputCourses(courses);
		logger.debug("Add courses: {}", Arrays.toString(courses));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_COURSE,
						Statement.RETURN_GENERATED_KEYS)) {

			connection.setAutoCommit(false);

			try {
				for (Course course : courses) {
					statement.setString(INSERT_COURSE_NAME_POSITION, course.getCourseName());
					statement.setString(INSERT_COURSE_DESCRIPTION_POSITION, course.getCourseDescription());
					statement.addBatch();
				}

				statement.executeBatch();
				logger.debug("addCourses: Batch executed");

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					int index = 0;
					while (generatedKeys.next() && index < courses.length) {
						int generatedId = generatedKeys.getInt(INSERT_RETRIEVED_ID_POSITION);
						courses[index].setCourseId(generatedId);
						index++;
					}

					if (index != courses.length) {
						logger.warn("addCourses: Number of generated keys does not match number of inserted courses.");
						throw new CourseNameDAOException(
								"Number of generated keys does not match number of inserted courses.");
					}
				}

				connection.commit();
				logger.info("All courses added successfully.");
			} catch (SQLException e) {
				try {
					connection.rollback();
					logger.debug("Transaction rolled back due to SQLException during 'addCourses'.");
				} catch (SQLException rollbackEx) {
					logger.error("Failed to rollback transaction during 'addCourses'.", rollbackEx);
				}

				if (e instanceof BatchUpdateException && e.getNextException() != null) {
					e = e.getNextException();
				}

				if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
					logger.warn("addCourses: Unique constraint violated for course_name during 'addCourses'.", e);
					throw new CourseNameDAOException("A course with the same name already exists.", e);
				}

				if (SQL_STATE_NULL_CONSTRAINT_VIOLATION.equals(e.getSQLState())) {
					logger.warn("addCourses: A required field is missing (NULL constraint violation).", e);
					throw new ValidationDAOException("A required field is missing (NULL constraint violation).", e);
				}

				logger.error("Failed to add courses: {}", e.getMessage(), e);
				throw new DAOException("Failed to add courses.", e);
			}
		} catch (SQLException e) {
			logger.error("Failed to establish connection or prepare statement.", e);
			throw new DAOException("Failed to add courses due to connection issues.", e);
		}
	}

	public Course findCourseById(int courseId) throws DAOException {
		logger.debug("Searching for course with ID: {}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_COURSE_BY_ID)) {

			statement.setInt(FIND_COURSE_ID_POSITION, courseId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					String courseName = resultSet.getString(COLUMN_COURSE_NAME);
					String courseDescription = resultSet.getString(COLUMN_COURSE_DESCRIPTION);
					Course course = new Course(courseId, courseName, courseDescription);
					logger.info("Course found: {}", course);
					return course;
				}
			}

			logger.info("Course with ID {} not found.", courseId);
			return new Course(NOT_FOUND_ID, NOT_FOUND_NAME, "");
		} catch (SQLException e) {
			logger.error("Error finding course by ID: {}", courseId, e);
			throw new DAOException("Failed to find course by ID", e);
		}
	}

	public void updateCourse(Course course) throws DAOException {
		validateInputCourses(course);
		logger.debug("Updating course: {}", course);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_COURSE)) {

			statement.setString(UPDATE_COURSE_NAME_POSITION, course.getCourseName());
			statement.setString(UPDATE_COURSE_DESCRIPTION_POSITION, course.getCourseDescription());
			statement.setInt(UPDATE_COURSE_ID_POSITION, course.getCourseId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("updateCourse: No course found with ID: {}", course.getCourseId());
				throw new ObjectNotFoundDAOException("No course found to update with ID: " + course.getCourseId());
			}

			logger.info("Course updated successfully: {}", course);
		} catch (SQLException e) {
			if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
				logger.warn("updateCourse: Unique constraint violated for course_name during 'updateCourse'.", e);
				throw new CourseNameDAOException("A course with the same name already exists.", e);
			}

			if (SQL_STATE_NULL_CONSTRAINT_VIOLATION.equals(e.getSQLState())) {
				logger.warn("updateCourse: A required field is missing (NULL constraint violation)", e);
				throw new ValidationDAOException("A required field is missing (NULL constraint violation)", e);
			}

			logger.error("Error updating course: {}", course, e);
			throw new DAOException("Failed to update course", e);
		}
	}

	public void deleteCourse(int courseId) throws DAOException {
		logger.debug("Attempting to delete course with ID: {}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_COURSE)) {

			statement.setInt(DELETE_COURSE_ID_POSITION, courseId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("deleteCourse: No course found to delete with ID: {}", courseId);
				throw new ObjectNotFoundDAOException("No course found to delete with ID: " + courseId);
			}

			logger.info("Course deleted successfully with ID: {}", courseId);
		} catch (SQLException e) {
			logger.error("Error deleting course with ID: {}", courseId, e);
			throw new DAOException("Failed to delete course with ID: " + courseId, e);
		}
	}

	private void validateInputCourses(Course... courses) {
		if (!courseValidator.validateCourses(courses)) {
			String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
			String errorMessage = String.format("Method '%s' received invalid data", methodName);
			logger.warn(errorMessage);
			throw new ValidationDAOException(errorMessage);
		}
	}
}
