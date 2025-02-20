package ua.foxminded.schoolapplication;

import ua.foxminded.schoolapplication.model.dao.ConnectionPool;
import ua.foxminded.schoolapplication.model.dao.CourseDao;
import ua.foxminded.schoolapplication.model.dao.DaoInitializer;
import ua.foxminded.schoolapplication.model.dao.GroupDao;
import ua.foxminded.schoolapplication.model.dao.StudentDao;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupIdDAOException;
import ua.foxminded.schoolapplication.model.dao.exception.GroupNameDAOException;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.model.validation.GroupValidator;

public class App {
	public static void main(String[] args) {
		DaoInitializer dbInitializer = new DaoInitializer();

		try {
			dbInitializer.initializeDatabase();
			System.out.println("Database initialized successfully.");
		} catch (DAOException e) {
			System.err.println("Error initializing database: " + e.getMessage());
			e.printStackTrace();
		}

		GroupDao groupDao = new GroupDao();
		StudentDao studentDao = new StudentDao();
		CourseDao сourseDao = new CourseDao();

		// Step 1: Add a new group
		Group newGroup = new Group(0, "TestGroup-11");
		try {
			groupDao.addGroups(newGroup);
			System.out.println("Added group: " + newGroup);
		} catch (GroupNameDAOException e) {
			System.out.println("DAOException catched");
		}

		// Step 2: Retrieve the added group by ID
		Group retrievedGroup = groupDao.findGroupById(newGroup.getGroupId());
		System.out.println("Retrieved group: " + retrievedGroup);

		// Step 3: Update the group name
		retrievedGroup.setGroupName("UpdatedTestGroup-12");
		groupDao.updateGroup(retrievedGroup);
		System.out.println("Updated group: " + retrievedGroup);

		// Step 4: Retrieve the updated group by ID
		Group updatedGroup = groupDao.findGroupById(retrievedGroup.getGroupId());
		System.out.println("Updated group retrieved: " + updatedGroup);

		// Step 5: Add a new student
		Student newStudent = new Student(0, updatedGroup.getGroupId(), "Jon", "Snow");
		try {
			studentDao.addStudents(newStudent);
			System.out.println("Added student: " + newStudent);
		} catch (GroupIdDAOException e) {
			System.out.println("DAOException catched");
		}

		// Step 6: Retrieve the added student by ID
		Student retrievedStudent = studentDao.findStudentById(newStudent.getStudentId());
		System.out.println("Retrieved student: " + retrievedStudent);

		// Step 7: Update the student data
		retrievedStudent.setFirstName("Jonny");
		retrievedStudent.setLastName("Snowball");
		studentDao.updateStudent(retrievedStudent);
		System.out.println("Updated student: " + retrievedStudent);

		// Step 8: Retrieve the updated group by ID
		Student updatedStudent = studentDao.findStudentById(retrievedStudent.getStudentId());
		System.out.println("Updated student retrieved: " + updatedStudent);

		// Step 9: Add a new course
		Course course = new Course(0, "Maths", "Introduction to Mathematics");
		try {
			сourseDao.addCourses(course);
			System.out.println("Added course: " + course);
		} catch (GroupIdDAOException e) {
			System.out.println("DAOException catched");
		}

		// Step 10: Retrieve the added course by ID
		Course retrievedCourse = сourseDao.findCourseById(course.getCourseId());
		System.out.println("Retrieved course: " + retrievedCourse);

		// Step 11: Update the course name
		retrievedCourse.setCourseName("UpdatedMathematics");
		сourseDao.updateCourse(retrievedCourse);
		System.out.println("Updated group: " + retrievedGroup);

		// Step 12: Retrieve the updated course by ID
		Course updatedCourse = сourseDao.findCourseById(retrievedCourse.getCourseId());
		System.out.println("Updated course retrieved: " + updatedCourse);

		// Step 13: Delete the course
		сourseDao.deleteCourse(course.getCourseId());
		System.out.println("Deleted course with ID: " + course.getCourseId());

		// Step 14: Delete the student
		studentDao.deleteStudent(updatedStudent.getStudentId());
		System.out.println("Deleted student with ID: " + updatedStudent.getStudentId());

		// Step 15: Delete the group
		groupDao.deleteGroup(updatedGroup.getGroupId());
		System.out.println("Deleted group with ID: " + updatedGroup.getGroupId());

		// Step 16: Attempt to retrieve the deleted group
		Group deletedGroup = groupDao.findGroupById(updatedGroup.getGroupId());
		System.out.println("Deleted group retrieval attempt: " + deletedGroup);

		// Step 17: Attempt to retrieve the deleted student
		Student deletedStudent = studentDao.findStudentById(updatedStudent.getStudentId());
		System.out.println("Deleted student retrieval attempt: " + deletedStudent);

		// Step 18: Attempt to retrieve the deleted student
		Course deletedCourse = сourseDao.findCourseById(course.getCourseId());
		System.out.println("Deleted course retrieval attempt: " + deletedCourse);

		ConnectionPool.closeSource();
		System.out.println("Finish!");

		GroupValidator validator = new GroupValidator();
		System.out.println(validator.validateGroups(new Group(1, "A-1")));
	}
}
