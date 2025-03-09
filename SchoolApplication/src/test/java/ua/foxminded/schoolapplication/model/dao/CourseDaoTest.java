package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;

import ua.foxminded.schoolapplication.model.dao.exception.CourseNameDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Course;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseDaoTest {
	static final Long DEFAULT_COURSE_ID = 0L;
	static final Long NON_EXISTENT_COURSE_ID = 999L;
	static final String DEFAULT_COURSE_NAME = "Introduction to Mathematics";
	static final String DEFAULT_COURSE_DESCRIPTION = "Basic concepts of mathematics.";
	static final String UPDATED_COURSE_NAME = "Advanced Mathematics";
	static final String UPDATED_COURSE_DESCRIPTION = "In-depth study of advanced math topics.";
	static final String NON_EXISTENT_COURSE_NAME = "NonExistentCourse";

	static final int GENERATED_COURSE = 0;

	private CourseDao courseDao;

	@BeforeEach
	void setUp() {
		courseDao = new CourseDao();
	}

	@Test
	void addCoursesShouldAddNewCourseAndFindById() {
		List<Course> generatedCourses = courseDao
				.addCourses(new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, DEFAULT_COURSE_DESCRIPTION));
		Course actualCourse = courseDao.findCourseById(generatedCourses.get(GENERATED_COURSE).getCourseId());

		assertNotNull(actualCourse, "Returned course should not be null.");
		assertEquals(generatedCourses.get(GENERATED_COURSE).getCourseName(),
				actualCourse.getCourseName(),
				"Course names should match.");
		assertEquals(generatedCourses.get(GENERATED_COURSE).getCourseDescription(),
				actualCourse.getCourseDescription(),
				"Course descriptions should match.");
		assertTrue(actualCourse.getCourseId() > DEFAULT_COURSE_ID, "Generated course ID should be greater than 0.");

		courseDao.deleteCourse(actualCourse.getCourseId());
	}

	@Test
	void addCoursesShouldThrowExceptionWhenAddingCourseWithInvalidData() {
		Course invalidCourse = new Course(DEFAULT_COURSE_ID, null, DEFAULT_COURSE_DESCRIPTION);
		assertThrows(ValidationException.class,
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

		List<Course> generatedCourses = courseDao.addCourses(firstCourse);
		assertThrows(CourseNameDAOException.class,
				() -> courseDao.addCourses(duplicateCourse),
				"Adding a course with duplicate course name should throw an exception.");

		courseDao.deleteCourse(generatedCourses.get(GENERATED_COURSE).getCourseId());
	}

	@Test
	void findCourseByIdShouldThrowExceptionWhenCourseDoesNotExist() {
		assertThrows(ObjectNotFoundDAOException.class,
				() -> courseDao.findCourseById(NON_EXISTENT_COURSE_ID),
				"Looking for a non-existent course should throw an exception.");
	}

	@Test
	void updateCourseShouldUpdateExistingCourse() {
		List<Course> generatedCourses = courseDao
				.addCourses(new Course(DEFAULT_COURSE_ID, DEFAULT_COURSE_NAME, DEFAULT_COURSE_DESCRIPTION));

		Course updatedCourse = new Course(generatedCourses.get(GENERATED_COURSE).getCourseId(), UPDATED_COURSE_NAME,
				UPDATED_COURSE_DESCRIPTION);
		courseDao.updateCourse(updatedCourse);

		Course actualCourse = courseDao.findCourseById(generatedCourses.get(GENERATED_COURSE).getCourseId());
		assertEquals(updatedCourse.getCourseName(), actualCourse.getCourseName(), "Course name should be updated.");
		assertEquals(updatedCourse.getCourseDescription(),
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
