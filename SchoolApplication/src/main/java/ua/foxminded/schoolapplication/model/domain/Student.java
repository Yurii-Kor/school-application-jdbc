package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
	private Long studentId;
	private Long groupId;
	private String firstName;
	private String lastName;
}
