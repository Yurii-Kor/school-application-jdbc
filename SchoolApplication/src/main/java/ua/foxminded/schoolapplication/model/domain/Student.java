package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.foxminded.schoolapplication.model.validation.StringValidationParameters;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
	private Long studentId;

	private Long groupId;

	@StringValidationParameters(minLength = 2, maxLength = 50, pattern = "^[A-Za-z]+$")
	private String firstName;

	@StringValidationParameters(minLength = 2, maxLength = 50, pattern = "^[A-Za-z]+$")
	private String lastName;
}
