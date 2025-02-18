package ua.foxminded.schoolapplication.model.validation;

import ua.foxminded.schoolapplication.model.domain.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class CourseValidator {
	private static final Logger logger = LoggerFactory.getLogger(CourseValidator.class);

	private static final int MIN_NAME_LENGTH = 2;
	private static final int MAX_NAME_LENGTH = 100;
	private static final int MAX_DESCRIPTION_LENGTH = 1000;

	private static final String COURSE_NAME_REGEX = "^[A-Za-z0-9\\s\\-:,.'&]+$";
	private static final Pattern COURSE_NAME_PATTERN = Pattern.compile(COURSE_NAME_REGEX);

	public void validateCourseName(String courseName) {
		if (courseName == null || courseName.trim().isEmpty()) {
			throw new IllegalArgumentException("Course name cannot be null or empty.");
		}
		String trimmedName = courseName.trim();
		int length = trimmedName.length();
		if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(
					String.format("Course name must be between %d and %d characters, but was: %d",
							MIN_NAME_LENGTH,
							MAX_NAME_LENGTH,
							length));
		}
		if (!COURSE_NAME_PATTERN.matcher(trimmedName).matches()) {
			throw new IllegalArgumentException(
					String.format("Course name does not match the required format: %s", COURSE_NAME_REGEX));
		}
	}

	public void validateCourseDescription(String courseDescription) {
		if (courseDescription != null && courseDescription.length() > MAX_DESCRIPTION_LENGTH) {
			throw new IllegalArgumentException(
					String.format("Course description must be at most %d characters, but was: %d",
							MAX_DESCRIPTION_LENGTH,
							courseDescription.length()));
		}
	}

	public boolean validateCourses(Course... courses) {
		if (courses == null) {
			logger.error("CourseValidator received a null argument");
			return false;
		}
		for (Course course : courses) {
			if (!validateOneCourse(course)) {
				String errorMessage = String.format("Validation failed for course: %s", course);
				logger.error(errorMessage);
				return false;
			}
		}
		return true;
	}

	private boolean validateOneCourse(Course course) {
		if (course == null) {
			logger.error("Validation failed: Course cannot be null.");
			return false;
		}
		try {
			validateCourseName(course.getCourseName());
			validateCourseDescription(course.getCourseDescription());
			logger.info("Validation successful for course: {}", course);
		} catch (IllegalArgumentException e) {
			logger.error("Validation failed: {}", e.getMessage());
			return false;
		}
		return true;
	}
}
