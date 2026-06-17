package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

class GroupValidatorTest {
    static final Long DEFAULT_ID = 1L;

    static final String VALID_GROUP_NAME_SIMPLE = "AB-12";
    static final String VALID_GROUP_NAME_LONG = "Mathematics-101";

    static final String INVALID_GROUP_NAME_NO_HYPHEN = "AB12";
    static final String INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN = "AB-1A";
    static final String INVALID_GROUP_NAME_EMPTY = "";
	static final String NULL = "null";
    static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS = "AB-";
    static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS = "-12";
    static final String INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS = "AB-12-34";

    static final String TEST_PATTERN = "GroupName: {0} | Expected: {1}";

    private EntityValidator<Group> validator;

    @BeforeEach
    void setUp() {
        validator = new EntityValidator<>();
    }

    @ParameterizedTest(name = TEST_PATTERN)
    @CsvSource({
            "'" + VALID_GROUP_NAME_SIMPLE + "', true",
            "'" + VALID_GROUP_NAME_LONG + "', true",

            "'" + INVALID_GROUP_NAME_NO_HYPHEN + "', false",
            "'" + INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN + "', false",
            "'" + INVALID_GROUP_NAME_EMPTY + "', false",
            "'" + NULL + "', false",
            "'" + INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS + "', false",
            "'" + INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS + "', false",
            "'" + INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS + "', false"
    })
    void validate_GroupName_ShouldBehaveAsExpected(String groupName, boolean shouldPass) {
        Group testedGroup = NULL.equals(groupName) ? new Group(DEFAULT_ID, null) : new Group(DEFAULT_ID, groupName);

        if (shouldPass) {
            assertDoesNotThrow(() -> validator.validateEntities(testedGroup),
                    "Validation should pass for groupName: " + groupName);
        } else {
            assertThrows(ValidationException.class,
                    () -> validator.validateEntities(testedGroup),
                    "Validation should fail for groupName: " + groupName);
        }
    }
}
