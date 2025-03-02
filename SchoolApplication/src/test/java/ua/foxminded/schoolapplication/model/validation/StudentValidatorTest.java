package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.foxminded.schoolapplication.model.domain.Student;

import static org.junit.jupiter.api.Assertions.*;

class StudentValidatorTest {
	static final Long DEFAULT_ID = 1L;

	static final String VALID_FIRST_NAME = "John";
	static final String VALID_LAST_NAME = "Doe";

	static final String NULL = "null";
	static final String INVALID_NAME_EMPTY = "";
	static final String INVALID_NAME_TOO_SHORT = "J";
	static final String INVALID_NAME_WITH_DIGIT = "John1";
	static final String INVALID_NAME_WITH_SYMBOL = "Doe!";
	static final String INVALID_NAME_WITH_SPACE = "John Doe";

	static final String TEST_PATTERN = "firstName: {0}, lastName: {1} | Expected: {2}";

	StudentValidator validator;

	@BeforeEach
	void setUp() {
		validator = new StudentValidator();
	}

	@ParameterizedTest(name = TEST_PATTERN)
	@CsvSource({
			// Valid case
			VALID_FIRST_NAME + ", " + VALID_LAST_NAME + ", true",

			// Invalid cases: one or both names are null
			NULL + ", " + VALID_LAST_NAME + ", false", VALID_FIRST_NAME + ", " + NULL + ", false",

			// Invalid cases: empty strings
			INVALID_NAME_EMPTY + ", " + VALID_LAST_NAME + ", false",
			VALID_FIRST_NAME + ", " + INVALID_NAME_EMPTY + ", false",

			// Invalid cases: names that are too short
			INVALID_NAME_TOO_SHORT + ", " + VALID_LAST_NAME + ", false",
			VALID_FIRST_NAME + ", " + INVALID_NAME_TOO_SHORT + ", false",

			// Invalid cases: names with invalid characters
			INVALID_NAME_WITH_DIGIT + ", " + VALID_LAST_NAME + ", false",
			VALID_FIRST_NAME + ", " + INVALID_NAME_WITH_SYMBOL + ", false",
			INVALID_NAME_WITH_SPACE + ", " + VALID_LAST_NAME + ", false",
			VALID_FIRST_NAME + ", " + INVALID_NAME_WITH_SPACE + ", false" })

	void validateStudent_ShouldReturnExpectedResult(String firstName, String lastName, boolean expected) {
		String validatedFirstName = NULL.equals(firstName) ? null : firstName;
		String validatedLastName = NULL.equals(lastName) ? null : lastName;

		Student student = new Student(DEFAULT_ID, DEFAULT_ID, validatedFirstName, validatedLastName);

		boolean result = validator.validateStudents(student);
		assertEquals(expected, result, "Validation result mismatch for student: " + student);
	}
}
