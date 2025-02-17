package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
	private int studentId;
	private int groupId;
	private String firstName;
	private String lastName;
}
