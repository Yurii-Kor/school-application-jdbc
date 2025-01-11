package ua.foxminded.schoolapplication;

import ua.foxminded.schoolapplication.model.dao.ConnectionPool;
import ua.foxminded.schoolapplication.model.dao.DAOException;
import ua.foxminded.schoolapplication.model.dao.DaoInitializer;
import ua.foxminded.schoolapplication.model.dao.GroupDao;
import ua.foxminded.schoolapplication.model.domain.Group;
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

        // Step 1: Add a new group
        Group newGroup = new Group(0, "TestGroup-11");
        groupDao.addGroups(newGroup);
        System.out.println("Added group: " + newGroup);

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

        // Step 5: Delete the group
        groupDao.deleteGroup(updatedGroup.getGroupId());
        System.out.println("Deleted group with ID: " + updatedGroup.getGroupId());

        // Step 6: Attempt to retrieve the deleted group
        Group deletedGroup = groupDao.findGroupById(updatedGroup.getGroupId());
        System.out.println("Deleted group retrieval attempt: " + deletedGroup);

        ConnectionPool.getDataSource().close();
        System.out.println("Finish!");
        
        GroupValidator validator = new GroupValidator();
        System.out.println(validator.validateGroups(new Group(1, "A-1")));
    }
}
