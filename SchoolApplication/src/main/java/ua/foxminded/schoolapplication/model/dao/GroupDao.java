package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.validation.GroupValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class GroupDao {
	private static final String TABLE = "groups";
	private static final String COLUMN_GROUP_ID = "group_id";
	private static final String COLUMN_GROUP_NAME = "group_name";

	private static final String INSERT_GROUP = String
			.format("INSERT INTO %s (%s) VALUES (?)", TABLE, COLUMN_GROUP_NAME);

	private static final String FIND_GROUP_BY_ID = String
			.format("SELECT * FROM %s WHERE %s = ?", TABLE, COLUMN_GROUP_ID);

	private static final String UPDATE_GROUP = String
			.format("UPDATE %s SET %s = ? WHERE %s = ?", TABLE, COLUMN_GROUP_NAME, COLUMN_GROUP_ID);

	private static final String DELETE_GROUP = String.format("DELETE FROM %s WHERE %s = ?", TABLE, COLUMN_GROUP_ID);

	private static final int INSERT_GROUP_NAME_POSITION = 1;
	private static final int INSERT_RETRIVED_ID_POSITION = 1;
	private static final int FIND_GROUP_BY_ID_POSITION = 1;
	private static final int UPDATE_GROUP_NAME_POSITION = 1;
	private static final int UPDATE_GROUP_ID_POSITION = 2;
	private static final int DELETE_GROUP_ID_POSITION = 1;

	private static final Group NOT_FOUND_GROUP = new Group(-1, "NOT_FOUND");
	private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";

	private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

	private final GroupValidator groupValidator;

	public GroupDao() {
		this.groupValidator = new GroupValidator();
	}

	public void addGroups(Group... groups) throws DAOException {
		validateInputGroups(groups);
		logger.debug("Add groups: {}", Arrays.toString(groups));

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(INSERT_GROUP,
						Statement.RETURN_GENERATED_KEYS)) {

			connection.setAutoCommit(false);

			try {
				for (Group group : groups) {
					statement.setString(INSERT_GROUP_NAME_POSITION, group.getGroupName());
					statement.addBatch();
				}

				int[] affectedRecords = statement.executeBatch();
				logger.debug("Batch executed, affected records: {}", affectedRecords.length);

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					int index = 0;
					while (generatedKeys.next()) {
						if (index >= groups.length) {
							throw new SQLException("Number of generated keys exceeds number of inserted groups.");
						}
						int generatedId = generatedKeys.getInt(INSERT_RETRIVED_ID_POSITION);
						groups[index].setGroupId(generatedId);
						index++;
					}
					if (index != groups.length) {
						throw new SQLException("Number of generated keys does not match number of inserted groups.");
					}
				}

				connection.commit();
				logger.info("All groups added successfully.");
			} catch (SQLException e) {
				try {
					connection.rollback();
					logger.debug("Transaction rolled back due to SQLException during 'addGroups'.");
				} catch (SQLException rollbackEx) {
					logger.error("Failed to rollback transaction during 'addGroups'.", rollbackEx);
				}

				if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
					logger.error("Unique constraint violated for group_name during 'addGroups'.", e);
					throw new DAOException("A group with the same name already exists.", e);
				}

				logger.error("Failed to add groups: {}", e.getMessage(), e);
				throw new DAOException("Failed to add groups.", e);
			}
		} catch (SQLException e) {
			logger.error("Failed to establish connection or prepare statement.", e);
			throw new DAOException("Failed to add groups due to connection issues.", e);
		}
	}

	public Group findGroupById(int groupId) throws DAOException {
		logger.debug("Searching for group with ID: {}", groupId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(FIND_GROUP_BY_ID)) {

			statement.setInt(FIND_GROUP_BY_ID_POSITION, groupId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Group group = new Group(resultSet.getInt(COLUMN_GROUP_ID), resultSet.getString(COLUMN_GROUP_NAME));
					logger.info("Group found: {}", group);
					return group;
				}
			}

			logger.info("Group with ID {} not found.", groupId);
			return new Group(NOT_FOUND_GROUP.getGroupId(), NOT_FOUND_GROUP.getGroupName());
		} catch (SQLException e) {
			logger.error("Error finding group by ID: {}", groupId, e);
			throw new DAOException("Failed to find group by ID", e);
		}
	}

	public void updateGroup(Group group) throws DAOException {
		validateInputGroups(group);
		logger.debug("Updating group: {}", group);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_GROUP)) {

			statement.setString(UPDATE_GROUP_NAME_POSITION, group.getGroupName());
			statement.setInt(UPDATE_GROUP_ID_POSITION, group.getGroupId());

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("No group found with ID: {}", group.getGroupId());
				throw new DAOException("No group found to update with ID: " + group.getGroupId());
			}

			logger.info("Group updated successfully: {}", group);
		} catch (SQLException e) {
			logger.error("Error updating group: {}", group, e);
			throw new DAOException("Failed to update group", e);
		}
	}

	public void deleteGroup(int groupId) throws DAOException {
		logger.debug("Attempting to delete group with ID: {}", groupId);

		try (Connection connection = ConnectionPool.getConnection();
				PreparedStatement statement = connection.prepareStatement(DELETE_GROUP)) {

			statement.setInt(DELETE_GROUP_ID_POSITION, groupId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("No group found to delete with ID: {}", groupId);
				throw new DAOException("No group found to delete with ID: " + groupId);
			}

			logger.info("Group deleted successfully with ID: {}", groupId);
		} catch (SQLException e) {
			logger.error("Error deleting group with ID: {}", groupId, e);
			throw new DAOException("Failed to delete group with ID: " + groupId, e);
		}
	}

	private void validateInputGroups(Group... groups) {
		if (!groupValidator.validateGroups(groups)) {
			String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
			String errorMessage = String.format("Method '%s' received invalid data", methodName);
			logger.error(errorMessage);
			throw new DAOException(errorMessage);
		}
	}
}
