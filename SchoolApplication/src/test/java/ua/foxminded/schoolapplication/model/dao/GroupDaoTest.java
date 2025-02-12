package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;

import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupDaoTest {
	private static final Group TEST_GROUP = new Group(0, "TestGroup-11");
	private static final Group EMPTY_NAME_GROUP = new Group(0, null);
	private static final Group NON_EXISTENT_GROUP = new Group(999, "NonExistentGroup-22");
	private static final Group NOT_FOUND_GROUP = GroupDao.NOT_FOUND_GROUP;

	private static final String UPDATED_GROUP_NAME = "UpdatedGroup-33";

	private GroupDao groupDao;

	@BeforeAll
	void initDatabase() {
		new DaoInitializer().initializeDatabase();
	}

	@BeforeEach
	void setUp() {
		groupDao = new GroupDao();
	}

	@Test
	void addGroupsShouldAddNewGroupAndFindById() {
		Group expectedGroup = new Group(TEST_GROUP.getGroupId(), TEST_GROUP.getGroupName());
		groupDao.addGroups(expectedGroup);
		Group actualGroup = groupDao.findGroupById(expectedGroup.getGroupId());

		assertNotNull(actualGroup, "Returned group should not be null.");
		assertEquals(expectedGroup.getGroupName(), actualGroup.getGroupName(), "Group names should match.");
		assertTrue(actualGroup.getGroupId() > 0, "Generated group ID should be greater than 0.");

		groupDao.deleteGroup(actualGroup.getGroupId());
	}

	@Test
	void addGroupsShouldThrowExceptionWhenAddingGroupWithWrongGroupData() {
		assertThrows(DAOException.class,
				() -> groupDao.addGroups(EMPTY_NAME_GROUP),
				"Adding a group with an invalid group name should throw an exception.");
	}

	@Test
	void addGroupsShouldNotAddDuplicateGroupNames() {
		Group firstGroup = new Group(TEST_GROUP.getGroupId(), TEST_GROUP.getGroupName());
		Group duplicateGroup = new Group(TEST_GROUP.getGroupId(), TEST_GROUP.getGroupName());

		groupDao.addGroups(firstGroup);
		DAOException exception = assertThrows(DAOException.class,
				() -> groupDao.addGroups(duplicateGroup),
				"Adding a group with duplicate group name should throw an exception.");
		assertTrue(exception.getMessage().contains("already exists"),
				"Exception message should indicate a uniqueness violation.");

		groupDao.deleteGroup(firstGroup.getGroupId());
	}

	@Test
	void findGroupByIdShouldReturnNotFoundWhenGroupDoesNotExist() {
		Group resultGroup = groupDao.findGroupById(NON_EXISTENT_GROUP.getGroupId());
		assertEquals(NOT_FOUND_GROUP.getGroupId(),
				resultGroup.getGroupId(),
				"Non-existent group ID should match NOT_FOUND constant.");
		assertEquals(NOT_FOUND_GROUP.getGroupName(),
				resultGroup.getGroupName(),
				"Non-existent group name should match NOT_FOUND constant.");
	}

	@Test
	void updateGroupShouldUpdateExistingGroup() {
		Group group = new Group(TEST_GROUP.getGroupId(), TEST_GROUP.getGroupName());
		groupDao.addGroups(group);

		Group updatedGroup = new Group(group.getGroupId(), UPDATED_GROUP_NAME);
		groupDao.updateGroup(updatedGroup);

		Group actualGroup = groupDao.findGroupById(group.getGroupId());
		assertEquals(updatedGroup.getGroupName(),
				actualGroup.getGroupName(),
				"Group name should be updated after updateGroup call.");

		groupDao.deleteGroup(actualGroup.getGroupId());
	}

	@Test
	void updateGroupShouldThrowExceptionWhenGroupDoesNotExist() {
		DAOException exception = assertThrows(DAOException.class,
				() -> groupDao.updateGroup(NON_EXISTENT_GROUP),
				"Updating a non-existent group should throw an exception.");
		assertTrue(exception.getMessage().contains("No group found"),
				"Exception message should indicate that no group was found for update.");
	}

	@Test
	void deleteGroupShouldThrowExceptionWhenGroupDoesNotExist() {
		DAOException exception = assertThrows(DAOException.class,
				() -> groupDao.deleteGroup(NON_EXISTENT_GROUP.getGroupId()),
				"Deleting a non-existent group should throw an exception.");
		assertTrue(exception.getMessage().contains("No group found to delete"),
				"Exception message should indicate deletion failure.");
	}
}
