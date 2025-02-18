package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;
import ua.foxminded.schoolapplication.model.dao.constants.NotFoundConstants;
import ua.foxminded.schoolapplication.model.dao.exception.CourseNameDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationDAOException;
import ua.foxminded.schoolapplication.model.domain.Course;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseDaoTest {
	static final int DEFAULT_COURSE_ID = 0;
	static final int NON_EXISTENT_COURSE_ID = 999;
	static final int NOT_FOUND_COURSE_ID = NotFoundConstants.NOT_FOUND.getId();
	static final String DEFAULT_COURSE_NAME = "Introduction to Mathematics";
	static final String DEFAULT_COURSE_DESCRIPTION = "Basic concepts of mathematics.";
	static final String UPDATED_COURSE_NAME = "Advanced Mathematics";
	static final String UPDATED_COURSE_DESCRIPTION = "In-depth study of advanced math topics.";
	static final String NON_EXISTENT_COURSE_NAME = "NonExistentCourse";
	static final String NOT_FOUND_COURSE_NAME = NotFoundConstants.NOT_FOUND.getName();

	private CourseDao courseDao;

	@BeforeAll
	void initDatabase() {
		new DaoInitializer().initializeDatabase();
	}

	@BeforeEach
	void setUp() {
		courseDao = new CourseDao();
	}

	@Test
	void addCoursesShouldAddNewCourseAndFindById() {
		Course expectedCourse = new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, DEFAULT_COURSE_DESCRIPTION);
		courseDao.addCourses(expectedCourse);
		Course actualCourse = courseDao.findCourseById(expectedCourse.getCourseId());

		assertNotNull(actualCourse, "Returned course should not be null.");
		assertEquals(expectedCourse.getCourseName(), actualCourse.getCourseName(), "Course names should match.");
		assertEquals(expectedCourse.getCourseDescription(),
				actualCourse.getCourseDescription(),
				"Course descriptions should match.");
		assertTrue(actualCourse.getCourseId() > DEFAULT_COURSE_ID, "Generated course ID should be greater than 0.");

		courseDao.deleteCourse(actualCourse.getCourseId());
	}

	@Test
	void addCoursesShouldThrowExceptionWhenAddingCourseWithInvalidData() {
		Course invalidCourse = new Course(DEFAULT_COURSE_ID, null, DEFAULT_COURSE_DESCRIPTION);
		assertThrows(ValidationDAOException.class,
				() -> courseDao.addCourses(invalidCourse),
				"Adding a course with an invalid course name should throw an exception.");
	}

	@Test
	void addCoursesShouldNotAddDuplicateCourseNames() {
		Course firstCourse = new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, DEFAULT_COURSE_DESCRIPTION);
		Course duplicateCourse = new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, "Different description");

		assertThrows(CourseNameDAOException.class,
				() -> courseDao.addCourses(firstCourse, duplicateCourse),
				"Adding courses with duplicate course name should throw an exception.");

		assertEquals(DEFAULT_COURSE_ID, firstCourse.getCourseId(), "Transaction should be rolled back.");
		assertEquals(DEFAULT_COURSE_ID, duplicateCourse.getCourseId(), "Transaction should be rolled back.");

		courseDao.addCourses(firstCourse);
		assertThrows(CourseNameDAOException.class,
				() -> courseDao.addCourses(duplicateCourse),
				"Adding a course with duplicate course name should throw an exception.");

		courseDao.deleteCourse(firstCourse.getCourseId());
	}

	@Test
	void findCourseByIdShouldReturnNotFoundWhenCourseDoesNotExist() {
		Course resultCourse = courseDao.findCourseById(NON_EXISTENT_COURSE_ID);
		assertEquals(NOT_FOUND_COURSE_ID,
				resultCourse.getCourseId(),
				"Non-existent course ID should match NOT_FOUND constant.");
		assertEquals(NOT_FOUND_COURSE_NAME,
				resultCourse.getCourseName(),
				"Non-existent course name should match NOT_FOUND constant.");
	}

	@Test
	void updateCourseShouldUpdateExistingCourse() {
		Course course = new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, DEFAULT_COURSE_DESCRIPTION);
		courseDao.addCourses(course);

		Course updatedCourse = new Course(course.getCourseId(), UPDATED_COURSE_NAME, UPDATED_COURSE_DESCRIPTION);
		courseDao.updateCourse(updatedCourse);

		Course actualCourse = courseDao.findCourseById(course.getCourseId());
		assertEquals(UPDATED_COURSE_NAME, actualCourse.getCourseName(), "Course name should be updated.");
		assertEquals(UPDATED_COURSE_DESCRIPTION,
				actualCourse.getCourseDescription(),
				"Course description should be updated.");

		courseDao.deleteCourse(actualCourse.getCourseId());
	}

	@Test
	void updateCourseShouldThrowExceptionWhenCourseDoesNotExist() {
		Course nonExistentCourse = new Course(NON_EXISTENT_COURSE_ID, NON_EXISTENT_COURSE_NAME,
				DEFAULT_COURSE_DESCRIPTION);
		assertThrows(ObjectNotFoundDAOException.class,
				() -> courseDao.updateCourse(nonExistentCourse),
				"Updating a non-existent course should throw an exception.");
	}

	@Test
	void deleteCourseShouldThrowExceptionWhenCourseDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> courseDao.deleteCourse(NON_EXISTENT_COURSE_ID),
				"Deleting a non-existent course should throw an exception.");
	}
}
