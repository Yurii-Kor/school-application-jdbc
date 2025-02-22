package ua.foxminded.schoolapplication.view;

import ua.foxminded.schoolapplication.model.dao.DaoInitializer;
import ua.foxminded.schoolapplication.model.dao.ConnectionPool;
import ua.foxminded.schoolapplication.model.dao.exception.DAOException;

import java.util.Scanner;

public class MainMenu {
	private final DaoInitializer dbInitializer;
	private final MenuActions menuActions;
	private final Scanner scanner;

	public MainMenu() {
		this.dbInitializer = new DaoInitializer();
		this.scanner = new Scanner(System.in);
		this.menuActions = new MenuActions(scanner);
	}

	public void start() {
		try {
			dbInitializer.initializeDatabase();
			System.out.println("Database initialized successfully.");

			runMenu();
			closeApplication();
		} catch (DAOException e) {
			System.err.println("Failed to initialize the database. The application will not start.");
			return;
		}
	}

	private void runMenu() {
		while (true) {
			printMenu();
			String choice = scanner.nextLine().trim().toLowerCase();

			if (choice.equals("a")) {
				menuActions.findGroupsByStudentCount();
			} else if (choice.equals("b")) {
				menuActions.findStudentsByCourseName();
			} else if (choice.equals("c")) {
				menuActions.addNewStudent();
			} else if (choice.equals("d")) {
				menuActions.deleteStudentById();
			} else if (choice.equals("e")) {
				menuActions.addStudentToCourse();
			} else if (choice.equals("f")) {
				menuActions.removeStudentFromCourse();
			} else if (choice.equals("q")) {
				System.out.println("Exiting application...");
				break;
			} else {
				System.out.println("Invalid choice. Please select a valid option.");
			}
		}
	}

	private void printMenu() {
		String lineSeparator = System.lineSeparator();
		StringBuilder menu = new StringBuilder();
		menu.append(lineSeparator)
				.append("=== Main Menu ===")
				.append(lineSeparator)
				.append(" a. Find all groups with less or equal students' number")
				.append(lineSeparator)
				.append(" b. Find all students related to the course with the given name")
				.append(lineSeparator)
				.append(" c. Add a new student")
				.append(lineSeparator)
				.append(" d. Delete a student by STUDENT_ID")
				.append(lineSeparator)
				.append(" e. Add a student to the course")
				.append(lineSeparator)
				.append(" f. Remove the student from one of their courses")
				.append(lineSeparator)
				.append(" q. Exit")
				.append(lineSeparator)
				.append("Choose an option: ");

		System.out.print(menu);
	}

	private void closeApplication() {
		scanner.close();
		ConnectionPool.closeSource();
		System.out.println("Resources released. Application closed.");
	}
}
