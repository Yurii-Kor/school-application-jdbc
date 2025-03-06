package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.dao.exception.CourseNameDAOException;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;
import ua.foxminded.schoolapplication.util.SQLExceptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class CourseDao {
	private static final Logger logger = LoggerFactory.getLogger(CourseDao.class);

	private static final String INSERT_COURSE = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_NAME,
			DBSchemaConstants.COURSE_DESCRIPTION);

	private static final String FIND_COURSE_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", DBSchemaConstants.COURSES_TABLE, DBSchemaConstants.COURSE_ID);

	private static final String UPDATE_COURSE = String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_NAME,
			DBSchemaConstants.COURSE_DESCRIPTION,
			DBSchemaConstants.COURSE_ID);

	private static final String DELETE_COURSE = String
			.format("DELETE FROM %s WHERE %s = ?", DBSchemaConstants.COURSES_TABLE, DBSchemaConstants.COURSE_ID);

	private static final int INSERT_COURSE_NAME_POSITION = 1;
	private static final int INSERT_COURSE_DESCRIPTION_POSITION = 2;
	private static final int INSERT_RETRIEVED_ID_POSITION = 1;
	private static final int FIND_COURSE_ID_POSITION = 1;
	private static final int UPDATE_COURSE_NAME_POSITION = 1;
	private static final int UPDATE_COURSE_DESCRIPTION_POSITION = 2;
	private static final int UPDATE_COURSE_ID_POSITION = 3;
	private static final int DELETE_COURSE_ID_POSITION = 1;
	private static final int ONE_ROW_AFFECTED = 1;

	private final EntityValidator<Course> courseValidator;

	public CourseDao() {
		this.courseValidator = new EntityValidator<>();
	}

	public void addCourses(Course... courses) throws DAOException {
		courseValidator.validateEntities(courses);
		logger.debug("Add courses: {}", Arrays.toString(courses));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_COURSE,
						Statement.RETURN_GENERATED_KEYS)) {

			for (Course course : courses) {
				statement.setString(INSERT_COURSE_NAME_POSITION, course.getCourseName());
				statement.setString(INSERT_COURSE_DESCRIPTION_POSITION, course.getCourseDescription());
				statement.addBatch();
			}

			connection.setAutoCommit(false);

			try {
				statement.executeBatch();
				logger.debug("addCourses: Batch executed");
			} catch (SQLException e) {
				connection.rollback();
				connection.setAutoCommit(true);
				logger.debug("Transaction rolled back due to SQLException during 'addCourses'.");

				throw e;
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				int index = 0;
				while (generatedKeys.next() && index < courses.length) {
					Long generatedId = generatedKeys.getLong(INSERT_RETRIEVED_ID_POSITION);
					courses[index].setCourseId(generatedId);
					index++;
				}

				if (index != courses.length) {
					connection.rollback();
					connection.setAutoCommit(true);
					logger.warn("addCourses: Number of generated keys does not match number of inserted courses.");

					throw new CourseNameDAOException(
							"Number of generated keys does not match number of inserted courses.");
				}
			}

			connection.commit();
			connection.setAutoCommit(true);
			logger.info("All courses added successfully.");
		} catch (SQLException e) {
			String sqlState = SQLExceptionUtil.extractSqlState(e);

			if (DAOErrorCode.UNIQUE_VIOLATION.equals(sqlState)) {
				logger.warn("addCourses: Unique constraint violated for course_name during 'addCourses'.", e);
				throw new CourseNameDAOException("A course with the same name already exists.", e);
			}

			if (DAOErrorCode.NULL_CONSTRAINT_VIOLATION.equals(sqlState)) {
				logger.warn("addCourses: A required field is missing (NULL constraint violation).", e);
				throw new ValidationException("A required field is missing (NULL constraint violation).", e);
			}

			logger.error("Failed to add courses: {}", e.getMessage(), e);
			throw new DAOException("Failed to add courses.", e);
		}
	}

	public Course findCourseById(Long courseId) throws DAOException {
		logger.debug("Searching for course with ID: {}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_COURSE_BY_ID)) {

			statement.setLong(FIND_COURSE_ID_POSITION, courseId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					String courseName = resultSet.getString(DBSchemaConstants.COURSE_NAME);
					String courseDescription = resultSet.getString(DBSchemaConstants.COURSE_DESCRIPTION);
					Course course = new Course(courseId, courseName, courseDescription);
					logger.info("Course found: {}", course);
					return course;
				}
			}

			logger.info("Course with ID {} not found.", courseId);
			throw new ObjectNotFoundDAOException("No course found with ID: " + courseId);
		} catch (SQLException e) {
			logger.error("Error finding course by ID: {}", courseId, e);
			throw new DAOException("Failed to find course by ID", e);
		}
	}

	public void updateCourse(Course course) throws DAOException {
		courseValidator.validateEntities(course);
		logger.debug("Updating course: {}", course);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_COURSE)) {

			statement.setString(UPDATE_COURSE_NAME_POSITION, course.getCourseName());
			statement.setString(UPDATE_COURSE_DESCRIPTION_POSITION, course.getCourseDescription());
			statement.setLong(UPDATE_COURSE_ID_POSITION, course.getCourseId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("updateCourse: No course found with ID: {}", course.getCourseId());
				throw new ObjectNotFoundDAOException("No course found to update with ID: " + course.getCourseId());
			}

			logger.info("Course updated successfully: {}", course);
		} catch (SQLException e) {
			String sqlState = SQLExceptionUtil.extractSqlState(e);

			if (DAOErrorCode.UNIQUE_VIOLATION.equals(sqlState)) {
				logger.warn("updateCourse: Unique constraint violated for course_name during 'updateCourse'.", e);
				throw new CourseNameDAOException("A course with the same name already exists.", e);
			}

			if (DAOErrorCode.NULL_CONSTRAINT_VIOLATION.equals(sqlState)) {
				logger.warn("updateCourse: A required field is missing (NULL constraint violation)", e);
				throw new ValidationException("A required field is missing (NULL constraint violation)", e);
			}

			logger.error("Error updating course: {}", course, e);
			throw new DAOException("Failed to update course", e);
		}
	}

	public void deleteCourse(Long courseId) throws DAOException {
		logger.debug("Attempting to delete course with ID: {}", courseId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_COURSE)) {

			statement.setLong(DELETE_COURSE_ID_POSITION, courseId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("deleteCourse: No course found to delete with ID: {}", courseId);
				throw new ObjectNotFoundDAOException("No course found to delete with ID: " + courseId);
			}

			logger.info("Course deleted successfully with ID: {}", courseId);
		} catch (SQLException e) {
			logger.error("Error deleting course with ID: {}", courseId, e);
			throw new DAOException("Failed to delete course with ID: " + courseId, e);
		}
	}
}
