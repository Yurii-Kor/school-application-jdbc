package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;

import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentDaoTest {
	static final Long DEFAULT_ID = 0L;
	static final Long NON_EXISTENT_ID = 999L;
	static final String DEFAULT_GROUP_NAME = "TestGroup-11";
	static final String DEFAULT_STUDENT_FIRST_NAME = "FirstName";
	static final String ADDITIONAL_STUDENT_FIRST_NAME = "AdditionalFirstName";
	static final String DEFAULT_STUDENT_LAST_NAME = "LastName";
	static final String ADDITIONAL_STUDENT_LAST_NAME = "AdditionalLastName";

	static final int GENERATED_STUDENT = 0;

	Long generatedGroupId = DEFAULT_ID;
	StudentDao studentDao;
	GroupDao groupDao;

	@BeforeAll
	void initDatabase() {
		Group testGroup = new Group(DEFAULT_ID, DEFAULT_GROUP_NAME);

		groupDao = new GroupDao();
		groupDao.addGroups(testGroup);

		generatedGroupId = testGroup.getGroupId();
	}

	@BeforeEach
	void setUp() {
		studentDao = new StudentDao();
	}

	@AfterAll
	void cleanUp() {
		groupDao.deleteGroup(generatedGroupId);
	}

	@Test
	void addStudentsShouldAddNewStudentAndFindById() {
		List<Student> generatedStudents = studentDao.addStudents(
				new Student(DEFAULT_ID, generatedGroupId, DEFAULT_STUDENT_FIRST_NAME, DEFAULT_STUDENT_LAST_NAME));

		Student actualStudent = studentDao.findStudentById(generatedStudents.get(GENERATED_STUDENT).getStudentId());
		assertNotNull(actualStudent, "Returned student should not be null.");
		assertEquals(generatedStudents.get(GENERATED_STUDENT).getFirstName(),
				actualStudent.getFirstName(),
				"First name should match.");
		assertEquals(generatedStudents.get(GENERATED_STUDENT).getLastName(),
				actualStudent.getLastName(),
				"Last name should match.");
		assertTrue(actualStudent.getStudentId() > DEFAULT_ID, "Generated student ID should be greater than 0.");

		studentDao.deleteStudent(actualStudent.getStudentId());
	}

	@Test
	void addStudentsShouldThrowExceptionWhenAddingStudentWithInvalidData() {
		Student invalidStudent = new Student(DEFAULT_ID, generatedGroupId, DEFAULT_STUDENT_FIRST_NAME, null);

		assertThrows(ValidationException.class,
				() -> studentDao.addStudents(invalidStudent),
				"Adding a student with invalid data should throw an exception.");
	}

	@Test
	void addStudentsShouldThrowExceptionWhenAddingStudentWithNonExistentGroupId() {
		Student invalidStudent = new Student(DEFAULT_ID, NON_EXISTENT_ID, DEFAULT_STUDENT_FIRST_NAME,
				DEFAULT_STUDENT_LAST_NAME);

		assertThrows(GroupIdDAOException.class,
				() -> studentDao.addStudents(invalidStudent),
				"Adding a student with a non-existent group ID should throw an exception.");
	}

	@Test
	void findStudentByIdShouldThrowExceptionWhenStudentDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> studentDao.findStudentById(NON_EXISTENT_ID),
				"Looking for a non-existent student should throw an exception.");
	}

	@Test
	void findStudentsByGroupIdShouldReturnListOfStudents() {
		List<Student> generatedStudents = studentDao.addStudents(
				new Student(DEFAULT_ID, generatedGroupId, DEFAULT_STUDENT_FIRST_NAME, DEFAULT_STUDENT_LAST_NAME),
				new Student(DEFAULT_ID, generatedGroupId, ADDITIONAL_STUDENT_FIRST_NAME, ADDITIONAL_STUDENT_LAST_NAME));

		List<Student> students = studentDao.findStudentsByGroupId(generatedGroupId);

		for (Student s : generatedStudents) {
			assertTrue(students.contains(s));
		}

		for (Student s : generatedStudents) {
			studentDao.deleteStudent(s.getStudentId());
		}
	}

	@Test
	void updateStudentShouldUpdateExistingStudent() {
		List<Student> generatedStudents = studentDao.addStudents(
				new Student(DEFAULT_ID, generatedGroupId, DEFAULT_STUDENT_FIRST_NAME, DEFAULT_STUDENT_LAST_NAME));

		generatedStudents.get(GENERATED_STUDENT).setFirstName(ADDITIONAL_STUDENT_FIRST_NAME);
		generatedStudents.get(GENERATED_STUDENT).setLastName(ADDITIONAL_STUDENT_LAST_NAME);
		studentDao.updateStudent(generatedStudents.get(GENERATED_STUDENT));

		Student actualStudent = studentDao.findStudentById(generatedStudents.get(GENERATED_STUDENT).getStudentId());
		assertEquals(ADDITIONAL_STUDENT_FIRST_NAME,
				actualStudent.getFirstName(),
				"Student first name should be updated.");
		assertEquals(ADDITIONAL_STUDENT_LAST_NAME, actualStudent.getLastName(), "Student last name should be updated.");

		studentDao.deleteStudent(actualStudent.getStudentId());
	}

	@Test
	void updateStudentShouldThrowExceptionWhenStudentDoesNotExist() {
		Student nonExistentStudent = new Student(NON_EXISTENT_ID, generatedGroupId, ADDITIONAL_STUDENT_FIRST_NAME,
				ADDITIONAL_STUDENT_LAST_NAME);
		assertThrows(ObjectNotFoundDAOException.class,
				() -> studentDao.updateStudent(nonExistentStudent),
				"Updating a non-existent student should throw a DAOException.");
	}

	@Test
	void updateStudentShouldThrowExceptionWhenGroupDoesNotExist() {
		List<Student> generatedStudents = studentDao.addStudents(
				new Student(DEFAULT_ID, generatedGroupId, DEFAULT_STUDENT_FIRST_NAME, DEFAULT_STUDENT_LAST_NAME));

		Student wrongDataStudent = new Student(generatedStudents.get(GENERATED_STUDENT).getStudentId(), NON_EXISTENT_ID,
				ADDITIONAL_STUDENT_FIRST_NAME, ADDITIONAL_STUDENT_LAST_NAME);
		assertThrows(GroupIdDAOException.class,
				() -> studentDao.updateStudent(wrongDataStudent),
				"Updating a non-existent student should throw a DAOException.");

		studentDao.deleteStudent(generatedStudents.get(GENERATED_STUDENT).getStudentId());
	}

	@Test
	void deleteStudentShouldThrowExceptionWhenStudentDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> studentDao.deleteStudent(NON_EXISTENT_ID),
				"Deleting a non-existent student should throw a DAOException.");
	}
}
