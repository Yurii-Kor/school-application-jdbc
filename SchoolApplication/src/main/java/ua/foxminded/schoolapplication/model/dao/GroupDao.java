package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.schoolapplication.model.domain.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GroupDao {
    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    private static final String INSERT_GROUP = "INSERT INTO groups (group_name) VALUES (?)";
    private static final String FIND_GROUP_BY_ID = "SELECT group_id, group_name FROM groups WHERE group_id = ?";
    private static final String UPDATE_GROUP = "UPDATE groups SET group_name = ? WHERE group_id = ?";
    private static final String DELETE_GROUP = "DELETE FROM groups WHERE group_id = ?";

    private static final int INSERT_GROUP_NAME_POSITION = 1;
    private static final int INSERT_RETRIVED_ID_POSITION = 1;
    private static final int FIND_GROUP_BY_ID_POSITION = 1;
    private static final int UPDATE_GROUP_NAME_POSITION = 1;
    private static final int UPDATE_GROUP_ID_POSITION = 2;
    private static final int DELETE_GROUP_ID_POSITION = 1;

    private static final String COLUMN_GROUP_ID = "group_id";
    private static final String COLUMN_GROUP_NAME = "group_name";

    private static final Group NOT_FOUND_GROUP = new Group(-1, "NOT_FOUND");

    private final ConnectionPool connectionPool;

    public GroupDao() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    public int addGroup(Group group) {
        validateNotNull(group);
        logger.debug("Adding group: {}", group);

        try (Connection connection = connectionPool.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_GROUP,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(INSERT_GROUP_NAME_POSITION, group.getGroupName());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(INSERT_RETRIVED_ID_POSITION);
                    group.setGroupId(generatedId);
                    logger.debug("Generated group ID: {}", generatedId);
                    logger.info("Group added successfully: {}", group);
                    return generatedId;
                } else {
                    logger.error("Failed to retrieve generated group ID.");
                    throw new DAOException("Failed to retrieve generated group ID.");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to add group: {}", group, e);
            throw new DAOException("Failed to add group: " + group, e);
        }
    }

    public Group findGroupById(int groupId) {
        logger.debug("Searching for group with ID: {}", groupId);

        try (Connection connection = connectionPool.getConnection();
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
            return NOT_FOUND_GROUP;
        } catch (SQLException e) {
            logger.error("Error finding group by ID: {}", groupId, e);
            throw new DAOException("Failed to find group by ID", e);
        }
    }

    public void updateGroup(Group group) {
        validateNotNull(group);
        logger.debug("Updating group: {}", group);

        try (Connection connection = connectionPool.getConnection();
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

    public void deleteGroup(int groupId) {
        logger.debug("Attempting to delete group with ID: {}", groupId);

        try (Connection connection = connectionPool.getConnection();
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
    
    private void validateNotNull(Group group) {
        if (group == null) {
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            String errorMessage = String.format("Method '%s' received a null argument", methodName);
            logger.error(errorMessage);
            throw new DAOException(errorMessage);
        }
    }
}
