package ua.foxminded.schoolapplication;

import static org.mockito.Mockito.mockStatic;

import java.util.Properties;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.mockito.MockedStatic;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import ua.foxminded.schoolapplication.util.PropertiesLoader;

public class GlobalTestListener implements TestExecutionListener {
	private static final String POOL_CONFIG = "hikari.properties";
	private static final String MIGRATION_CONFIG = "flyway.properties";

	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:15.1"));

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		POSTGRES_CONTAINER.start();

		Properties hikariProperties = PropertiesLoader.loadProperties(POOL_CONFIG);
		hikariProperties.setProperty("jdbcUrl", POSTGRES_CONTAINER.getJdbcUrl());
		hikariProperties.setProperty("username", POSTGRES_CONTAINER.getUsername());
		hikariProperties.setProperty("password", POSTGRES_CONTAINER.getPassword());

		Properties flywayProperties = PropertiesLoader.loadProperties(MIGRATION_CONFIG);
		flywayProperties.setProperty("flyway.url", POSTGRES_CONTAINER.getJdbcUrl());
		flywayProperties.setProperty("flyway.user", POSTGRES_CONTAINER.getUsername());
		flywayProperties.setProperty("flyway.password", POSTGRES_CONTAINER.getPassword());

		mockPropertiesLoader(hikariProperties, flywayProperties);

		System.out.println("🔹 Testcontainers PostgreSQL started: " + POSTGRES_CONTAINER.getJdbcUrl());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		POSTGRES_CONTAINER.stop();
		System.out.println("🔹 Testcontainers PostgreSQL stopped.");
	}

	private void mockPropertiesLoader(Properties connectionPoolProp, Properties migrationProp) {
		MockedStatic<PropertiesLoader> mockedStatic = mockStatic(PropertiesLoader.class);

		mockedStatic.when(() -> PropertiesLoader.loadProperties(POOL_CONFIG)).thenReturn(connectionPoolProp);

		mockedStatic.when(() -> PropertiesLoader.loadProperties(MIGRATION_CONFIG)).thenReturn(migrationProp);
	}
}
