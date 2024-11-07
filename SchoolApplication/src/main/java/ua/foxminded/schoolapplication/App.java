package ua.foxminded.schoolapplication;

import ua.foxminded.schoolapplication.dao.DaoInitializer;
import ua.foxminded.schoolapplication.dao.ConnectionPool;
import ua.foxminded.schoolapplication.dao.DAOException;

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
        
        ConnectionPool.getInstance().close();
        System.out.println("Finish!");
    }
}
