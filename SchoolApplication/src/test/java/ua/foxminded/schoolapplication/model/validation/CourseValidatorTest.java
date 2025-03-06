package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Course;

import static org.junit.jupiter.api.Assertions.*;

class CourseValidatorTest {
	static final Long DEFAULT_ID = 1L;

	static final String VALID_COURSE_NAME = "Mathematics 101";
	static final String VALID_COURSE_DESCRIPTION = "An introductory course to mathematics.";

	static final String NULL = "null";
	static final String EMPTY = "";
	static final String TOO_SHORT = "A";
	static final String INVALID_CHARS = "Math@101";
	static final String TOO_LONG_NAME = "This is a very long course name that is intended to exceed the maximum allowed length of one hundred characters for courses";

	EntityValidator<Course> validator;

	@BeforeEach
	void setUp() {
		validator = new EntityValidator<>();
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

	void validateEntities_ShouldBehaveAsExpected(String courseName, String courseDescription, boolean shouldPass) {
		String validatedCourseName = NULL.equals(courseName) ? null : courseName;
		String validatedCourseDescription = NULL.equals(courseDescription) ? null : courseDescription;

		Course course = new Course(DEFAULT_ID, validatedCourseName, validatedCourseDescription);

		if (shouldPass) {
			assertDoesNotThrow(() -> validator.validateEntities(course),
					"Validation should pass for course: " + course);
		} else {
			assertThrows(ValidationException.class,
					() -> validator.validateEntities(course),
					"Validation should fail for course: " + course);
		}
	}
}
