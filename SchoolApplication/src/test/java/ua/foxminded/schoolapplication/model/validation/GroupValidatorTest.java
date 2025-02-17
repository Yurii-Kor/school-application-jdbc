package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

class GroupValidatorTest {
	static final String VALID_GROUP_NAME_SIMPLE = "AB-12";
	static final String VALID_GROUP_NAME_LONG = "Mathematics-101";

	static final String INVALID_GROUP_NAME_NO_HYPHEN = "AB12";
	static final String INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN = "AB-1A";
	static final String INVALID_GROUP_NAME_EMPTY = "";
	static final String INVALID_GROUP_NAME_NULL = "null";
	static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS = "AB-";
	static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS = "-12";
	static final String INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS = "AB-12-34";

	static final String TEST_PATTERN = "GroupName: {0} | Expected: {1}";

	GroupValidator validator;

	@BeforeEach
	void setUp() {
		validator = new GroupValidator();
	}

	@ParameterizedTest(name = TEST_PATTERN)
	@CsvSource({ VALID_GROUP_NAME_SIMPLE + ", true", VALID_GROUP_NAME_LONG + ", true",

			INVALID_GROUP_NAME_NO_HYPHEN + ", false", INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN + ", false",
			INVALID_GROUP_NAME_EMPTY + ", false", INVALID_GROUP_NAME_NULL + ", false",
			INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS + ", false", INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS + ", false",
			INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS + ", false" })

	void validate_GroupName_ShouldReturnExpectedResult(String groupName, boolean expected) {
		Group testedGroup;
		if (INVALID_GROUP_NAME_NULL.equals(groupName)) {
			testedGroup = new Group(1, null);
		} else {
			testedGroup = new Group(1, groupName);
		}

		boolean result = validator.validateGroups(testedGroup);
		assertEquals(expected, result, "Validation result mismatch for groupName: " + groupName);
	}
}
