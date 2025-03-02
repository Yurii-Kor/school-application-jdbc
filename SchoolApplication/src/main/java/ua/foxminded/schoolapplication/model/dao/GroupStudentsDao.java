package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupStudentsDao {
	private static final String FIND_STUDENTS_BY_GROUP_NAME = String.format(
			"SELECT s.* FROM %s s JOIN %s g ON s.%s = g.%s WHERE g.%s = ?",

			DBSchemaConstants.STUDENTS_TABLE.getValue(),
			DBSchemaConstants.GROUPS_TABLE.getValue(),
			DBSchemaConstants.STUDENT_GROUP_ID.getValue(),
			DBSchemaConstants.GROUP_ID.getValue(),
			DBSchemaConstants.GROUP_NAME.getValue());

	private static final String FIND_GROUPS_WITH_STUDENT_COUNT_LESS_OR_EQUAL = String.format(
			"SELECT g.* FROM %s g LEFT JOIN %s s ON g.%s = s.%s GROUP BY g.%s, g.%s HAVING COUNT(s.%s) <= ?",

			DBSchemaConstants.GROUPS_TABLE.getValue(),
			DBSchemaConstants.STUDENTS_TABLE.getValue(),
			DBSchemaConstants.GROUP_ID.getValue(),
			DBSchemaConstants.STUDENT_GROUP_ID.getValue(),
			DBSchemaConstants.GROUP_ID.getValue(),
			DBSchemaConstants.GROUP_NAME.getValue(),
			DBSchemaConstants.STUDENT_ID.getValue());

	private static final int FIND_STUDENTS_GROUP_NAME_POSITION = 1;
	private static final int FIND_GROUPS_STUDENTS_AMOUNT_POSITION = 1;

	private static final Logger logger = LoggerFactory.getLogger(GroupStudentsDao.class);

	public List<Student> findStudentsByGroupName(String groupName) throws DAOException {
		logger.debug("Searching for students in group with name: {}", groupName);
		List<Student> students = new ArrayList<>();

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_GROUP_NAME)) {

			statement.setString(FIND_STUDENTS_GROUP_NAME_POSITION, groupName);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Long studentId = resultSet.getLong(DBSchemaConstants.STUDENT_ID.getValue());
					Long groupId = resultSet.getLong(DBSchemaConstants.STUDENT_GROUP_ID.getValue());
					String firstName = resultSet.getString(DBSchemaConstants.STUDENT_FIRST_NAME.getValue());
					String lastName = resultSet.getString(DBSchemaConstants.STUDENT_LAST_NAME.getValue());

					students.add(new Student(studentId, groupId, firstName, lastName));
				}
			}

			logger.info("Found {} students for group '{}'", students.size(), groupName);
			return students;
		} catch (SQLException e) {
			logger.error("Error finding students for group '{}'", groupName, e);
			throw new DAOException("Failed to find students by group name: " + groupName, e);
		}
	}

	public List<Group> findGroupsWithStudentCountLessOrEqual(int maxCount) throws DAOException {
		logger.debug("Finding groups with student count <= {}", maxCount);
		List<Group> groups = new ArrayList<>();

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection
						.prepareStatement(FIND_GROUPS_WITH_STUDENT_COUNT_LESS_OR_EQUAL)) {

			statement.setInt(FIND_GROUPS_STUDENTS_AMOUNT_POSITION, maxCount);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Long groupId = resultSet.getLong(DBSchemaConstants.GROUP_ID.getValue());
					String groupName = resultSet.getString(DBSchemaConstants.GROUP_NAME.getValue());

					groups.add(new Group(groupId, groupName));
				}
			}

			logger.info("Found {} groups with student count <= {}", groups.size(), maxCount);
			return groups;
		} catch (SQLException e) {
			logger.error("Error finding groups with student count <= {}", maxCount, e);
			throw new DAOException("Failed to find groups with student count <= " + maxCount, e);
		}
	}
}
