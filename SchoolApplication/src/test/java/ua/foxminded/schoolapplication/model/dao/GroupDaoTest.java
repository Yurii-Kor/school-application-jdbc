package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;

import ua.foxminded.schoolapplication.model.dao.exception.GroupNameDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationDAOException;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupDaoTest {
	static final Long DEFAULT_GROUP_ID = 0L;
	static final Long NON_EXISTENT_GROUP_ID = 999L;
	static final String DEFAULT_GROUP_NAME = "TestGroup-11";
	static final String UPDATED_GROUP_NAME = "NonExistentGroup-22";
	static final String NON_EXISTENT_GROUP_NAME = "UpdatedGroup-33";
	GroupDao groupDao;

	@BeforeEach
	void setUp() {
		groupDao = new GroupDao();
	}

	@Test
	void addGroupsShouldAddNewGroupAndFindById() {
		Group expectedGroup = new Group(DEFAULT_GROUP_ID, DEFAULT_GROUP_NAME);
		groupDao.addGroups(expectedGroup);
		Group actualGroup = groupDao.findGroupById(expectedGroup.getGroupId());

		assertNotNull(actualGroup, "Returned group should not be null.");
		assertEquals(expectedGroup.getGroupName(), actualGroup.getGroupName(), "Group names should match.");
		assertTrue(actualGroup.getGroupId() > DEFAULT_GROUP_ID, "Generated group ID should be greater than 0.");

		groupDao.deleteGroup(actualGroup.getGroupId());
	}

	@Test
	void addGroupsShouldThrowExceptionWhenAddingGroupWithWrongGroupData() {
		Group emptyNameGroup = new Group(DEFAULT_GROUP_ID, null);
		assertThrows(ValidationDAOException.class,
				() -> groupDao.addGroups(emptyNameGroup),
				"Adding a group with an invalid group name should throw an exception.");
	}

	@Test
	void addGroupsShouldNotAddDuplicateGroupNames() {
		Group firstGroup = new Group(DEFAULT_GROUP_ID, DEFAULT_GROUP_NAME);
		Group duplicateGroup = new Group(DEFAULT_GROUP_ID, DEFAULT_GROUP_NAME);

		assertThrows(GroupNameDAOException.class,
				() -> groupDao.addGroups(firstGroup, duplicateGroup),
				"Adding groups with duplicate group name should rollback transaction before throwing an exception");

		assertEquals(DEFAULT_GROUP_ID, firstGroup.getGroupId(), "Transaction shold be rolled back.");
		assertEquals(DEFAULT_GROUP_ID, duplicateGroup.getGroupId(), "Transaction shold be rolled back.");

		groupDao.addGroups(firstGroup);
		assertThrows(GroupNameDAOException.class,
				() -> groupDao.addGroups(duplicateGroup),
				"Adding a group with duplicate group name should throw an exception.");

		groupDao.deleteGroup(firstGroup.getGroupId());
	}

	@Test
	void findGroupByIdShouldThrowExceptionWhenGroupDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> groupDao.findGroupById(NON_EXISTENT_GROUP_ID),
				"Looking for a non-existent group should throw an exception.");
	}

	@Test
	void updateGroupShouldUpdateExistingGroup() {
		Group group = new Group(DEFAULT_GROUP_ID, DEFAULT_GROUP_NAME);
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
		Group nonExistentGroup = new Group(NON_EXISTENT_GROUP_ID, NON_EXISTENT_GROUP_NAME);
		assertThrows(ObjectNotFoundDAOException.class,
				() -> groupDao.updateGroup(nonExistentGroup),
				"Updating a non-existent group should throw an exception.");
	}

	@Test
	void updateGroupShouldThrowExceptionWhenUpdatingExistentGroupName() {
		Group group = new Group(DEFAULT_GROUP_ID, DEFAULT_GROUP_NAME);
		Group groupWithExistentGroupName = new Group(DEFAULT_GROUP_ID, UPDATED_GROUP_NAME);
		groupDao.addGroups(group, groupWithExistentGroupName);

		Group updatedGroup = new Group(group.getGroupId(), UPDATED_GROUP_NAME);
		assertThrows(GroupNameDAOException.class,
				() -> groupDao.updateGroup(updatedGroup),
				"Updating a group with an existing name should throw an exception.");

		groupDao.deleteGroup(group.getGroupId());
		groupDao.deleteGroup(groupWithExistentGroupName.getGroupId());
	}

	@Test
	void deleteGroupShouldThrowExceptionWhenGroupDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> groupDao.deleteGroup(NON_EXISTENT_GROUP_ID),
				"Deleting a non-existent group should throw an exception.");
	}
}
