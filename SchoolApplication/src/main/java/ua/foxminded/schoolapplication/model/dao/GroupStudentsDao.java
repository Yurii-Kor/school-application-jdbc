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
	private static final Logger logger = LoggerFactory.getLogger(GroupStudentsDao.class);

	private static final String TABLE_GROUPS = DBSchemaConstants.GROUPS_TABLE.getValue();
	private static final String GROUPS_PRIMARY_KEY = DBSchemaConstants.GROUP_ID.getValue();
	private static final String GROUPS_SECONDARY_KEY = DBSchemaConstants.GROUP_NAME.getValue();

	private static final String TABLE_STUDENTS = DBSchemaConstants.STUDENTS_TABLE.getValue();
	private static final String STUDENTS_ID = DBSchemaConstants.STUDENT_ID.getValue();
	private static final String STUDENTS_FOREIGN_KEY = DBSchemaConstants.STUDENT_GROUP_ID.getValue();
	private static final String STUDENTS_FIRST_NAME = DBSchemaConstants.STUDENT_FIRST_NAME.getValue();
	private static final String STUDENTS_LAST_NAME = DBSchemaConstants.STUDENT_LAST_NAME.getValue();

	private static final String FIND_STUDENTS_BY_GROUP_NAME = String.format(
			"SELECT s.* FROM %s s JOIN %s g ON s.%s = g.%s WHERE g.%s = ?",

			TABLE_STUDENTS,
			TABLE_GROUPS,
			STUDENTS_FOREIGN_KEY,
			GROUPS_PRIMARY_KEY,
			GROUPS_SECONDARY_KEY);

	private static final String FIND_GROUPS_WITH_STUDENT_COUNT_LESS_OR_EQUAL = String.format(
			"SELECT g.* FROM %s g LEFT JOIN %s s ON g.%s = s.%s GROUP BY g.%s, g.%s HAVING COUNT(s.%s) <= ?",

			TABLE_GROUPS,
			TABLE_STUDENTS,
			GROUPS_PRIMARY_KEY,
			STUDENTS_FOREIGN_KEY,
			GROUPS_PRIMARY_KEY,
			GROUPS_SECONDARY_KEY,
			STUDENTS_ID);

	public List<Student> findStudentsByGroupName(String groupName) throws DAOException {
		logger.debug("Searching for students in group with name: {}", groupName);
		List<Student> students = new ArrayList<>();

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_GROUP_NAME)) {

			statement.setString(1, groupName);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int studentId = resultSet.getInt(STUDENTS_ID);
					int groupId = resultSet.getInt(STUDENTS_FOREIGN_KEY);
					String firstName = resultSet.getString(STUDENTS_FIRST_NAME);
					String lastName = resultSet.getString(STUDENTS_LAST_NAME);

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

			statement.setInt(1, maxCount);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int groupId = resultSet.getInt(GROUPS_PRIMARY_KEY);
					String groupName = resultSet.getString(GROUPS_SECONDARY_KEY);

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
