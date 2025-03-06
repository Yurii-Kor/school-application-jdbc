package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.constants.DAOErrorCode;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupNameDAOException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;
import ua.foxminded.schoolapplication.util.SQLExceptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class GroupDao {
	private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

	private static final String INSERT_GROUP = String
			.format("INSERT INTO %s (%s) VALUES (?)", DBSchemaConstants.GROUPS_TABLE, DBSchemaConstants.GROUP_NAME);

	private static final String FIND_GROUP_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", DBSchemaConstants.GROUPS_TABLE, DBSchemaConstants.GROUP_ID);

	private static final String UPDATE_GROUP = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.GROUP_NAME,
			DBSchemaConstants.GROUP_ID);

	private static final String DELETE_GROUP = String
			.format("DELETE FROM %s WHERE %s = ?", DBSchemaConstants.GROUPS_TABLE, DBSchemaConstants.GROUP_ID);

	private static final int INSERT_GROUP_NAME_POSITION = 1;
	private static final int INSERT_RETRIVED_ID_POSITION = 1;
	private static final int FIND_GROUP_BY_ID_POSITION = 1;
	private static final int UPDATE_GROUP_NAME_POSITION = 1;
	private static final int UPDATE_GROUP_ID_POSITION = 2;
	private static final int DELETE_GROUP_ID_POSITION = 1;
	private static final int ONE_ROW_AFFECTED = 1;

	private final EntityValidator<Group> groupValidator;

	public GroupDao() {
		this.groupValidator = new EntityValidator<>();
	}

	public void addGroups(Group... groups) throws DAOException {
		groupValidator.validateEntities(groups);
		logger.debug("Add groups: {}", Arrays.toString(groups));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_GROUP,
						Statement.RETURN_GENERATED_KEYS)) {

			for (Group group : groups) {
				statement.setString(INSERT_GROUP_NAME_POSITION, group.getGroupName());
				statement.addBatch();
			}

			connection.setAutoCommit(false);

			try {
				statement.executeBatch();
				logger.debug("addGroups : Batch executed");
			} catch (SQLException e) {
				connection.rollback();
				connection.setAutoCommit(true);
				logger.debug("Transaction rolled back due to SQLException during 'addGroups'.");

				throw e;
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				int index = 0;
				while (generatedKeys.next() && index < groups.length) {
					Long generatedId = generatedKeys.getLong(INSERT_RETRIVED_ID_POSITION);
					groups[index].setGroupId(generatedId);
					index++;
				}

				if (index != groups.length) {
					connection.rollback();
					connection.setAutoCommit(true);
					logger.warn("addGroups : Number of generated keys exceeds number of inserted groups.");
					throw new GroupNameDAOException("Number of generated keys exceeds number of inserted groups.");
				}
			}

			connection.commit();
			connection.setAutoCommit(true);
			logger.info("All groups added successfully.");
		} catch (SQLException e) {
			String sqlState = SQLExceptionUtil.extractSqlState(e);

			if (DAOErrorCode.UNIQUE_VIOLATION.equals(sqlState)) {
				logger.warn("addGroups : Unique constraint violated for group_name during 'addGroups'.", e);
				throw new GroupNameDAOException("A group with the same name already exists.", e);
			}

			if (DAOErrorCode.NULL_CONSTRAINT_VIOLATION.equals(sqlState)) {
				logger.warn("addGroups : A required field is missing (NULL constraint violation)", e);
				throw new ValidationException("A required field is missing (NULL constraint violation)", e);
			}

			logger.error("Failed to add groups: {}", e.getMessage(), e);
			throw new DAOException("Failed to add groups.", e);
		}
	}

	public Group findGroupById(Long groupId) throws DAOException {
		logger.debug("Searching for group with ID: {}", groupId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_GROUP_BY_ID)) {

			statement.setLong(FIND_GROUP_BY_ID_POSITION, groupId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Group group = new Group(resultSet.getLong(DBSchemaConstants.GROUP_ID),
							resultSet.getString(DBSchemaConstants.GROUP_NAME));
					logger.info("Group found: {}", group);
					return group;
				}
			}

			logger.info("Group with ID {} not found.", groupId);
			throw new ObjectNotFoundDAOException("No group found with ID: " + groupId);
		} catch (SQLException e) {
			logger.error("Error finding group by ID: {}", groupId, e);
			throw new DAOException("Failed to find group by ID", e);
		}
	}

	public void updateGroup(Group group) throws DAOException {
		groupValidator.validateEntities(group);
		logger.debug("Updating group: {}", group);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_GROUP)) {

			statement.setString(UPDATE_GROUP_NAME_POSITION, group.getGroupName());
			statement.setLong(UPDATE_GROUP_ID_POSITION, group.getGroupId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("No group found with ID: {}", group.getGroupId());
				throw new ObjectNotFoundDAOException("No group found to update with ID: " + group.getGroupId());
			}

			logger.info("Group updated successfully: {}", group);
		} catch (SQLException e) {
			String sqlState = SQLExceptionUtil.extractSqlState(e);

			if (DAOErrorCode.UNIQUE_VIOLATION.equals(sqlState)) {
				logger.warn("updateGroup : Unique constraint violated for group_name during 'addGroups'.", e);
				throw new GroupNameDAOException("A group with the same name already exists.", e);
			}

			if (DAOErrorCode.NULL_CONSTRAINT_VIOLATION.equals(sqlState)) {
				logger.warn("updateGroup : A required field is missing (NULL constraint violation)", e);
				throw new ValidationException("A required field is missing (NULL constraint violation)", e);
			}

			logger.error("Error updating group: {}", group, e);
			throw new DAOException("Failed to update group", e);
		}
	}

	public void deleteGroup(Long groupId) throws DAOException {
		logger.debug("Attempting to delete group with ID: {}", groupId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_GROUP)) {

			statement.setLong(DELETE_GROUP_ID_POSITION, groupId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != ONE_ROW_AFFECTED) {
				logger.warn("No group found to delete with ID: {}", groupId);
				throw new ObjectNotFoundDAOException("No group found to delete with ID: " + groupId);
			}

			logger.info("Group deleted successfully with ID: {}", groupId);
		} catch (SQLException e) {
			if (DAOErrorCode.UNIQUE_VIOLATION.equals(SQLExceptionUtil.extractSqlState(e))) {
				logger.warn("Cannot delete group with ID {} because it has dependent students", groupId, e);
				throw new GroupIdDAOException("Group cannot be deleted because it has associated students", e);
			}

			logger.error("Error deleting group with ID: {}", groupId, e);
			throw new DAOException("Failed to delete group with ID: " + groupId, e);
		}
	}
}
