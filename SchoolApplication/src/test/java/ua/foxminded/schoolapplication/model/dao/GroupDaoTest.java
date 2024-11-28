package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupDaoTest {
    private static final Group TEST_GROUP = new Group(0, "Test_Group");
    private static final Group EMPTY_NAME_GROUP = new Group(0, null);
    private static final Group NON_EXISTENT_GROUP = new Group(999, "Non-Existent Group");
    private static final Group NOT_FOUND_GROUP = new Group(-1, "NOT_FOUND");

    private GroupDao groupDao;

    @BeforeEach
    void setUp() {
        new DaoInitializer().initializeDatabase();
        groupDao = new GroupDao();
    }

    @Test
    void addGroupShouldAddNewGroupAndFindById() {
        Group expectedGroup = TEST_GROUP;

        int generatedId = groupDao.addGroup(expectedGroup);
        Group actualGroup = groupDao.findGroupById(generatedId);

        assertEquals(expectedGroup.getGroupName(), actualGroup.getGroupName(), "Group names should match");
        assertTrue(actualGroup.getGroupId() > 0, "Generated group ID should be greater than 0");
    }

    @Test
    void addGroupShouldThrowExceptionWhenAddingGroupWithEmptyName() {
        DAOException exception = assertThrows(DAOException.class, () -> groupDao.addGroup(EMPTY_NAME_GROUP));

        assertTrue(exception.getMessage().contains("Failed to add group"), "Message should indicate add failure");
    }

    @Test
    void addGroupShouldThrowExceptionWhenAddingNullGroup() {
        DAOException exception = assertThrows(DAOException.class, () -> groupDao.addGroup(null));

        assertTrue(exception.getMessage().contains("null argument"), "Message should indicate null argument");
    }

    @Test
    void findGroupByIdShouldReturnNotFoundGroupWhenGroupDoesNotExist() {
        Group resultGroup = groupDao.findGroupById(NON_EXISTENT_GROUP.getGroupId());

        assertEquals(NOT_FOUND_GROUP.getGroupId(), resultGroup.getGroupId(), "ID should indicate 'NOT_FOUND'");
        assertEquals(NOT_FOUND_GROUP.getGroupName(), resultGroup.getGroupName(), "Name should indicate 'NOT_FOUND'");
    }

    @Test
    void updateGroupShouldUpdateExistingGroup() {
        int generatedId = groupDao.addGroup(TEST_GROUP);

        Group updatedGroup = new Group(generatedId, "Updated Group");
        groupDao.updateGroup(updatedGroup);

        Group actualGroup = groupDao.findGroupById(generatedId);
        assertEquals(updatedGroup.getGroupName(), actualGroup.getGroupName(), "Group name should be updated");
    }

    @Test
    void updateGroupShouldThrowExceptionWhenUpdatingNonExistentGroup() {
        Group nonExistentGroup = NON_EXISTENT_GROUP;

        DAOException exception = assertThrows(DAOException.class, () -> groupDao.updateGroup(nonExistentGroup));
        assertTrue(exception.getMessage().contains("No group found"), "Message should indicate update failure");
    }

    @Test
    void updateGroupShouldThrowExceptionWhenUpdatingNullGroup() {
        DAOException exception = assertThrows(DAOException.class, () -> groupDao.updateGroup(null));

        assertTrue(exception.getMessage().contains("null argument"), "Message should indicate null argument");
    }
    
    @Test
    void deleteGroupShouldDeleteExistingGroup() {
        int generatedId = groupDao.addGroup(TEST_GROUP);

        groupDao.deleteGroup(generatedId);

        Group resultGroup = groupDao.findGroupById(generatedId);
        assertEquals(NOT_FOUND_GROUP.getGroupName(), resultGroup.getGroupName(), "Deleted group name should indicate 'NOT_FOUND'");
    }

    @Test
    void deleteGroupShouldThrowExceptionWhenDeletingNonExistentGroup() {
        DAOException exception = assertThrows(DAOException.class, () -> groupDao.deleteGroup(NON_EXISTENT_GROUP.getGroupId()));
        assertTrue(exception.getMessage().contains("No group found to delete"), "Message should indicate delete failure");
    }
}
