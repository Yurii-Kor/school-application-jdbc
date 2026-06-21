package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.foxminded.schoolapplication.model.validation.StringValidationParameters;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
	private Long groupId;

	@StringValidationParameters(minLength = 3, maxLength = 21, pattern = "^[A-Za-z]+-\\d+$")
	private String groupName;
}
