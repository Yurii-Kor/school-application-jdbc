package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;

import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.StudentCourseAlreadyExistsDAOException;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentsCoursesDaoTest {
	static final Long DEFAULT_ID = 0L;
	static final String TEST_GROUP_NAME = "TestGroup-11";
	static final String TEST_STUDENT_FIRST_NAME = "Student";
	static final String TEST_STUDENT_LAST_NAME = "Relation";
	static final String TEST_COURSE_NAME = "TestCourse";
	static final String TEST_COURSE_DESCRIPTION = "Course description";

	GroupDao groupDao;
	StudentDao studentDao;
	CourseDao courseDao;
	StudentsCoursesDao studentsCoursesDao;

	Long testGroupId;

	Student testStudent;
	Course testCourse;

	@BeforeAll
	void initDatabase() {
		groupDao = new GroupDao();
		studentDao = new StudentDao();
		courseDao = new CourseDao();
		studentsCoursesDao = new StudentsCoursesDao();

		Group testGroup = new Group(DEFAULT_ID, TEST_GROUP_NAME);
		groupDao.addGroups(testGroup);
		testGroupId = testGroup.getGroupId();
	}

	@AfterAll
	void cleanUp() {
		groupDao.deleteGroup(testGroupId);
	}

	@BeforeEach
	void setUp() {
		testStudent = new Student(DEFAULT_ID, testGroupId, TEST_STUDENT_FIRST_NAME, TEST_STUDENT_LAST_NAME);
		studentDao.addStudents(testStudent);

		testCourse = new Course(DEFAULT_ID, TEST_COURSE_NAME, TEST_COURSE_DESCRIPTION);
		courseDao.addCourses(testCourse);
	}

	@AfterEach
	void tearDown() {
		studentDao.deleteStudent(testStudent.getStudentId());
		courseDao.deleteCourse(testCourse.getCourseId());
	}

	@Test
	void addStudentCourseShouldAddRelationAndRetrieveIds() {
		studentsCoursesDao.addStudentCourse(testStudent.getStudentId(), testCourse.getCourseId());

		List<Long> courseIds = studentsCoursesDao.findCourseIdsByStudentId(testStudent.getStudentId());
		assertTrue(courseIds.contains(testCourse.getCourseId()),
				"findCourseIdsByStudentId should return the test course id");

		List<Long> studentIds = studentsCoursesDao.findStudentIdsByCourseId(testCourse.getCourseId());
		assertTrue(studentIds.contains(testStudent.getStudentId()),
				"findStudentIdsByCourseId should return the test student id");

		studentsCoursesDao.deleteStudentCourse(testStudent.getStudentId(), testCourse.getCourseId());

		courseIds = studentsCoursesDao.findCourseIdsByStudentId(testStudent.getStudentId());
		assertFalse(courseIds.contains(testCourse.getCourseId()),
				"After deletion, findCourseIdsByStudentId should not return the test course id");
	}

	@Test
	void addStudentCourseShouldThrowExceptionWhenDuplicateRelationExists() {
		studentsCoursesDao.addStudentCourse(testStudent.getStudentId(), testCourse.getCourseId());
		assertThrows(StudentCourseAlreadyExistsDAOException.class,
				() -> studentsCoursesDao.addStudentCourse(testStudent.getStudentId(), testCourse.getCourseId()),
				"Adding a duplicate students_courses relation should throw a StudentCourseAlreadyExistsDAOException.");

		studentsCoursesDao.deleteStudentCourse(testStudent.getStudentId(), testCourse.getCourseId());
	}

	@Test
	void deleteStudentCourseShouldThrowExceptionWhenRelationDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> studentsCoursesDao.deleteStudentCourse(testStudent.getStudentId(), testCourse.getCourseId()),
				"Deleting a non-existent students_courses relation should throw an exception.");
	}
}
