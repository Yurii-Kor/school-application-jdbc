package ua.foxminded.schoolapplication.view;

import java.util.List;
import java.util.Scanner;

import ua.foxminded.schoolapplication.model.dao.GroupStudentsDao;
import ua.foxminded.schoolapplication.model.dao.StudentDao;
import ua.foxminded.schoolapplication.model.dao.StudentsCoursesDao;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ObjectNotFoundDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.StudentCourseAlreadyExistsDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationDAOException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

public class MenuActions {
	private final GroupStudentsDao groupStudentsDao;
	private final StudentsCoursesDao studentsCoursesDao;
	private final StudentDao studentDao;
	private final Scanner scanner;

	public MenuActions(Scanner scanner) {
		this.groupStudentsDao = new GroupStudentsDao();
		this.studentsCoursesDao = new StudentsCoursesDao();
		this.studentDao = new StudentDao();
		this.scanner = scanner;
	}

	public void findGroupsByStudentCount() {
		System.out.print("Enter the max number of students: ");
		int maxStudents = scanner.nextInt();
		scanner.nextLine();

		try {
			List<Group> groups = groupStudentsDao.findGroupsWithStudentCountLessOrEqual(maxStudents);

			if (groups.isEmpty()) {
				System.out.println("No groups found with student count ≤ " + maxStudents);
			} else {
				System.out.println("Groups with student count ≤ " + maxStudents + ":");
				groups.forEach(group -> System.out.println(group));
			}
		} catch (DAOException e) {
			System.err.println("An error occurred while retrieving groups: " + e.getMessage());
		}
	}

	public void findStudentsByCourseName() {
		System.out.print("Enter course name: ");
		String courseName = scanner.nextLine().trim();

		try {
			List<Student> students = studentsCoursesDao.findStudentsByCourseName(courseName);

			if (students.isEmpty()) {
				System.out.println("No students found for course: " + courseName);
			} else {
				System.out.println("Students enrolled in course '" + courseName + "':");
				students.forEach(student -> System.out.println(student));
			}
		} catch (DAOException e) {
			System.err.println("An error occurred while retrieving students: " + e.getMessage());
		}
	}

	public void addNewStudent() {
		System.out.print("Enter group ID: ");
		Long groupId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter first name: ");
		String firstName = scanner.nextLine().trim();

		System.out.print("Enter last name: ");
		String lastName = scanner.nextLine().trim();

		Student student = new Student(0L, groupId, firstName, lastName);

		try {
			studentDao.addStudents(student);
			System.out.println(
					"Student added successfully: " + firstName + " " + lastName + " (Group ID: " + groupId + ")");
		} catch (GroupIdDAOException e) {
			System.err.println("Error: Invalid group ID. This group does not exist.");
		} catch (ValidationDAOException e) {
			System.err.println("Error: A required field is missing. Please ensure all fields are filled.");
		} catch (DAOException e) {
			System.err.println("An unexpected error occurred while adding the student: " + e.getMessage());
		}
	}

	public void deleteStudentById() {
		System.out.print("Enter STUDENT_ID to delete: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		try {
			studentDao.deleteStudent(studentId);
			System.out.println("Student with ID " + studentId + " was deleted successfully.");
		} catch (ObjectNotFoundDAOException e) {
			System.err.println("Error: No student found with ID " + studentId + ".");
		} catch (DAOException e) {
			System.err.println("An unexpected error occurred while deleting the student: " + e.getMessage());
		}
	}

	public void addStudentToCourse() {
		System.out.print("Enter STUDENT_ID: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter COURSE_ID: ");
		Long courseId = scanner.nextLong();
		scanner.nextLine();

		try {
			studentsCoursesDao.addStudentCourse(studentId, courseId);
			System.out.println("Student with ID " + studentId + " successfully added to course ID " + courseId + ".");
		} catch (StudentCourseAlreadyExistsDAOException e) {
			System.err.println("Error: The student is already enrolled in this course.");
		} catch (DAOException e) {
			System.err.println("An error occurred while adding the student to the course: " + e.getMessage());
		}
	}

	public void removeStudentFromCourse() {
		System.out.print("Enter STUDENT_ID: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter COURSE_ID to remove: ");
		Long courseId = scanner.nextLong();
		scanner.nextLine();

		try {
			studentsCoursesDao.deleteStudentCourse(studentId, courseId);
			System.out.println("Student with ID " + studentId + " removed from course ID " + courseId + ".");
		} catch (ObjectNotFoundDAOException e) {
			System.err.println("Error: No such enrollment found. The student is not registered for this course.");
		} catch (DAOException e) {
			System.err.println("An error occurred while removing the student from the course: " + e.getMessage());
		}
	}
}
