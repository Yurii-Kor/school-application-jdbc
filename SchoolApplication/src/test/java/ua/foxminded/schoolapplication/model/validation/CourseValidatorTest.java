package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.foxminded.schoolapplication.model.domain.Course;

import static org.junit.jupiter.api.Assertions.*;

class CourseValidatorTest {
	private static final int DEFAULT_ID = 1;
	private static final String VALID_COURSE_NAME = "Mathematics 101";
	private static final String VALID_COURSE_DESCRIPTION = "An introductory course to mathematics.";

	private static final String NULL = "null";
	private static final String EMPTY = "";
	private static final String TOO_SHORT = "A";
	private static final String INVALID_CHARS = "Math@101";
	private static final String TOO_LONG_NAME = "This is a very long course name that is intended to exceed the maximum allowed length of one hundred characters for courses";

	private CourseValidator validator;

	@BeforeEach
	void setUp() {
		validator = new CourseValidator();
	}

	@ParameterizedTest(name = "courseName: \"{0}\", courseDescription: \"{1}\" | Expected: {2}")
	@CsvSource({
			// Valid cases
			"'" + VALID_COURSE_NAME + "', '" + VALID_COURSE_DESCRIPTION + "', true",
			"'" + VALID_COURSE_NAME + "', '', true",

			// Invalid course name: null
			"null, '" + VALID_COURSE_DESCRIPTION + "', false",

			// Invalid course name: empty string
			"'" + EMPTY + "', '" + VALID_COURSE_DESCRIPTION + "', false",

			// Invalid course name: too short
			"'" + TOO_SHORT + "', '" + VALID_COURSE_DESCRIPTION + "', false",

			// Invalid course name: contains invalid characters
			"'" + INVALID_CHARS + "', '" + VALID_COURSE_DESCRIPTION + "', false",

			// Invalid course name: too long
			"'" + TOO_LONG_NAME + "', '" + VALID_COURSE_DESCRIPTION + "', false" })

	void validateCourseName_ShouldReturnExpectedResult(String courseName, String courseDescription, boolean expected) {
		String validatedCourseName = NULL.equals(courseName) ? null : courseName;
		String validatedCourseDescription = NULL.equals(courseDescription) ? null : courseDescription;
		
		Course course = new Course(DEFAULT_ID, validatedCourseName, validatedCourseDescription);
		
		boolean result = validator.validateCourses(course);
		assertEquals(expected, result, "Validation result mismatch for course: " + course);
	}

	@Test
	void validateCourseDescription_ShouldFailForTooLongDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1001; i++) {
			sb.append("A");
		}

		String tooLongDescription = sb.toString();
		Course course = new Course(DEFAULT_ID, VALID_COURSE_NAME, tooLongDescription);
		assertFalse(validator.validateCourses(course),
				"Validation should fail for a course with too long description.");
	}
}
