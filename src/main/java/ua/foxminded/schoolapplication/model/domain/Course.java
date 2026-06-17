package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.foxminded.schoolapplication.model.validation.StringValidationParameters;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
	private Long courseId;

	@StringValidationParameters(minLength = 2, maxLength = 100, pattern = "^[A-Za-z0-9\\s\\-:,.'&]+$")
	private String courseName;

	@StringValidationParameters(isNullPossible = true, maxLength = 1000)
	private String courseDescription;
}
