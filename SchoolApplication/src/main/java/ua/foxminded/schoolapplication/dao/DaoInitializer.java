package ua.foxminded.schoolapplication.dao;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DaoInitializer.class);
    private static final String MIGRATION_LOCATION = "classpath:db/migration";

    public void initializeDatabase() throws DAOException {
        logger.debug("Starting database migration.");

        try {
            Flyway flyway = configureFlyway();
            flyway.migrate();
            logger.info("Database migration completed successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize database with Flyway", e);
            throw new DAOException("Failed to initialize database", e);
        }
    }

    private static Flyway configureFlyway() {
        logger.debug("Configuring Flyway with data source and migration location.");
        return Flyway.configure()
                .dataSource(ConnectionPool.getInstance().getDataSource())
                .locations(MIGRATION_LOCATION)
                .load();
    }
}
