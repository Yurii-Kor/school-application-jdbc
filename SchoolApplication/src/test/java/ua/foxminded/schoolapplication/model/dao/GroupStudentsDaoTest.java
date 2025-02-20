package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupStudentsDaoTest {
	static final int DEFAULT_ID = 0;
	static final String[] GROUP_NAMES = new String[] { "TestGroup-11", "TestGroup-22", "TestGroup-33" };
	static final String[] STUDENT_FIRST_NAMES = new String[] { "FirstNameA", "FirstNameB" };
	static final String[] STUDENT_LAST_NAMES = new String[] { "LastNameA", "LastNameB" };

	static final int ONE_STUDENT = 1;

	GroupDao groupDao;
	StudentDao studentDao;
	GroupStudentsDao groupStudentsDao;

	Group noStudentsGroup;
	Group oneStudentGroup;
	Group twoStudentsGroup;

	Student studentGroupOneStudent;
	Student firstStudentGroupTwoStudents;
	Student secondStudentGroupTwoStudents;

	@BeforeAll
	void initDatabase() {
		new DaoInitializer().initializeDatabase();
		groupDao = new GroupDao();

		noStudentsGroup = new Group(DEFAULT_ID, GROUP_NAMES[0]);
		oneStudentGroup = new Group(DEFAULT_ID, GROUP_NAMES[1]);
		twoStudentsGroup = new Group(DEFAULT_ID, GROUP_NAMES[2]);

		groupDao.addGroups(noStudentsGroup, oneStudentGroup, twoStudentsGroup);
	}

	@AfterAll
	void cleanUp() {
		groupDao.deleteGroup(noStudentsGroup.getGroupId());
		groupDao.deleteGroup(oneStudentGroup.getGroupId());
		groupDao.deleteGroup(twoStudentsGroup.getGroupId());
	}

	@BeforeEach
	void setUp() {
		studentDao = new StudentDao();
		groupStudentsDao = new GroupStudentsDao();

		studentGroupOneStudent = new Student(DEFAULT_ID, oneStudentGroup.getGroupId(), STUDENT_FIRST_NAMES[0],
				STUDENT_LAST_NAMES[0]);
		firstStudentGroupTwoStudents = new Student(DEFAULT_ID, twoStudentsGroup.getGroupId(), STUDENT_FIRST_NAMES[1],
				STUDENT_LAST_NAMES[0]);
		secondStudentGroupTwoStudents = new Student(DEFAULT_ID, twoStudentsGroup.getGroupId(), STUDENT_FIRST_NAMES[0],
				STUDENT_LAST_NAMES[1]);

		studentDao.addStudents(studentGroupOneStudent, firstStudentGroupTwoStudents, secondStudentGroupTwoStudents);

	}

	@AfterEach
	void tearDown() {
		studentDao.deleteStudent(studentGroupOneStudent.getStudentId());
		studentDao.deleteStudent(firstStudentGroupTwoStudents.getStudentId());
		studentDao.deleteStudent(secondStudentGroupTwoStudents.getStudentId());
	}

	@Test
	void findGroupsWithStudentCountLessOrEqualShouldReturnCorrectGroups() {
		List<Group> groups = groupStudentsDao.findGroupsWithStudentCountLessOrEqual(ONE_STUDENT);

		assertTrue(groups.contains(noStudentsGroup), "Empty group should be returned.");
		assertTrue(groups.contains(oneStudentGroup), "Group with 1 student should be returned.");
		assertFalse(groups.contains(twoStudentsGroup), "Group with 2 students should not be returned.");
	}

	@Test
	void findStudentsByGroupNameShouldReturnStudentsByReceivedGroupName() {
		List<Student> students = groupStudentsDao.findStudentsByGroupName(noStudentsGroup.getGroupName());

		assertTrue(students.isEmpty(), "Empty group should return empty list.");

		students = groupStudentsDao.findStudentsByGroupName(oneStudentGroup.getGroupName());

		assertEquals(ONE_STUDENT, students.size(), "Result should have exactly 1 student.");
		assertFalse(students.contains(firstStudentGroupTwoStudents),
				"Result should not contain students from other groups.");
		assertTrue(students.contains(studentGroupOneStudent), "Result should contain the right student.");
	}
}
