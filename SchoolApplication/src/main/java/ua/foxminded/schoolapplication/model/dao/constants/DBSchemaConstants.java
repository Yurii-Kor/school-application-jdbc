package ua.foxminded.schoolapplication.model.dao.constants;

public enum DBSchemaConstants {
	GROUPS_TABLE("groups"), 
	GROUP_ID("group_id"), 
	GROUP_NAME("group_name"),

	STUDENTS_TABLE("students"), 
	STUDENT_ID("student_id"), 
	STUDENT_GROUP_ID("group_id"),
	STUDENT_FIRST_NAME("first_name"), 
	STUDENT_LAST_NAME("last_name"),

	COURSES_TABLE("courses"), 
	COURSE_ID("course_id"), 
	COURSE_NAME("course_name"),
	COURSE_DESCRIPTION("course_description"),

	STUDENTS_COURSES_TABLE("students_courses"), 
	STUDENTS_COURSES_STUDENT_ID("student_id"),
	STUDENTS_COURSES_COURSE_ID("course_id");

	private final String value;

	DBSchemaConstants(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
