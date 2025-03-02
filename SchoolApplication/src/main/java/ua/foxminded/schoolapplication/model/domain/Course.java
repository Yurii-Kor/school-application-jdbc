package ua.foxminded.schoolapplication.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
	private Long courseId;
	private String courseName;
	private String courseDescription;
}
