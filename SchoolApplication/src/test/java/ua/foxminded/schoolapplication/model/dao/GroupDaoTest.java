package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupDaoTest {
    private static final Group TEST_GROUP = new Group(0, "TestGroup-11");
    private static final Group EMPTY_NAME_GROUP = new Group(0, null);
    private static final Group NON_EXISTENT_GROUP = new Group(999, "NonExistentGroup-22");
    private static final Group NOT_FOUND_GROUP = new Group(-1, "NOT_FOUND");

    private GroupDao groupDao;

    @BeforeEach
    void setUp() {
        new DaoInitializer().initializeDatabase();
        groupDao = new GroupDao();
    }
    
    @Test
    void addGroupsShouldAddNewGroupAndFindById() {
        Group expectedGroup = TEST_GROUP;

        int[] generatedId = groupDao.addGroups(expectedGroup);
        Group actualGroup = groupDao.findGroupById(generatedId[0]);

        assertEquals(expectedGroup.getGroupName(), actualGroup.getGroupName(), "Group names should match");
        assertTrue(actualGroup.getGroupId() > 0, "Generated group ID should be greater than 0");
    }

    @Test
    void addGroupShouldThrowExceptionWhenAddingGroupWithWrongGroupData() {
        assertThrows(DAOException.class, () -> groupDao.addGroups(EMPTY_NAME_GROUP));
    }

    @Test
    void findGroupByIdShouldReturnNotFoundGroupWhenGroupDoesNotExist() {
        Group resultGroup = groupDao.findGroupById(NON_EXISTENT_GROUP.getGroupId());

        assertEquals(NOT_FOUND_GROUP.getGroupId(), resultGroup.getGroupId(), "ID should indicate 'NOT_FOUND'");
        assertEquals(NOT_FOUND_GROUP.getGroupName(), resultGroup.getGroupName(), "Name should indicate 'NOT_FOUND'");
    }

    @Test
    void updateGroupShouldUpdateExistingGroup() {
        int[] generatedId = groupDao.addGroups(TEST_GROUP);

        Group updatedGroup = new Group(generatedId[0], "UpdatedGroup-11");
        groupDao.updateGroup(updatedGroup);

        Group actualGroup = groupDao.findGroupById(generatedId[0]);
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
        assertThrows(DAOException.class, () -> groupDao.updateGroup(null));
    }
    
    @Test
    void deleteGroupShouldDeleteExistingGroup() {
        int[] generatedId = groupDao.addGroups(TEST_GROUP);

        groupDao.deleteGroup(generatedId[0]);

        Group resultGroup = groupDao.findGroupById(generatedId[0]);
        assertEquals(NOT_FOUND_GROUP.getGroupName(), resultGroup.getGroupName(), "Deleted group name should indicate 'NOT_FOUND'");
    }

    @Test
    void deleteGroupShouldThrowExceptionWhenDeletingNonExistentGroup() {
        DAOException exception = assertThrows(DAOException.class, () -> groupDao.deleteGroup(NON_EXISTENT_GROUP.getGroupId()));
        assertTrue(exception.getMessage().contains("No group found to delete"), "Message should indicate delete failure");
    }
}
