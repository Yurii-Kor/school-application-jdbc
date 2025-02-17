package ua.foxminded.schoolapplication.model.validation;

import ua.foxminded.schoolapplication.model.domain.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class StudentValidator {
    private static final Logger logger = LoggerFactory.getLogger(StudentValidator.class);

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private static final String NAME_REGEX = "^[A-Za-z]+$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    public void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }

        String trimmedName = name.trim();
        int length = trimmedName.length();
        if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("%s must be between %d and %d characters, but was: %d",
                        fieldName, MIN_NAME_LENGTH, MAX_NAME_LENGTH, length)
            );
        }

        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new IllegalArgumentException(
                String.format("%s does not match the required format: %s", fieldName, NAME_REGEX)
            );
        }
    }

    public boolean validateStudents(Student... students) {
        if (students == null) {
            logger.error("StudentValidator received a null argument");
            return false;
        }

        for (Student student : students) {
            if (!validateOneStudent(student)) {
                String errorMessage = String.format("Validation failed for student: %s", student);
                logger.error(errorMessage);
                return false;
            }
        }
        return true;
    }

    private boolean validateOneStudent(Student student) {
        if (student == null) {
            logger.error("Validation failed: Student cannot be null.");
            return false;
        }

        try {
            validateName(student.getFirstName(), "First name");
            validateName(student.getLastName(), "Last name");
            logger.info("Validation successful for student: {}", student);
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return false;
        }
        return true;
    }
}
