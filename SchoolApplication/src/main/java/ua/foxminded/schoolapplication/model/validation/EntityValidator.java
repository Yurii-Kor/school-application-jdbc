package ua.foxminded.schoolapplication.model.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EntityValidator<T> {
	private static final Logger logger = LoggerFactory.getLogger(EntityValidator.class);

	public void validateEntities(T... entities) throws ValidationException {
		if (entities == null || entities.length == 0) {
			String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
			String errorMessage = String.format("Method '%s' received invalid data", methodName);
			logger.warn(errorMessage);
			throw new ValidationException(errorMessage);
		}

		for (var entity : entities) {
			validateEntity(entity);
		}
	}

	private void validateEntity(T entity) throws ValidationException {
		if (entity == null) {
			throw new ValidationException("Entity cannot be null.");
		}

		for (var field : entity.getClass().getDeclaredFields()) {
			boolean wasAccessible = field.canAccess(entity);
			field.setAccessible(true);

			try {
				var getter = getGetterMethod(entity.getClass(), field);

				var fieldType = getter.invoke(entity).getClass();
				if (fieldType.equals(Long.class)) {
					validateLongField((Long) getter.invoke(entity), field.getName());
				} else if (fieldType.equals(String.class)) {
					validateStringField((String) getter.invoke(entity), field);
				} else {
					throw new ValidationException("Unexpected field type: " + fieldType.getSimpleName());
				}
			} catch (Exception e) {
				logger.error("Failed to validate field: {}", field.getName(), e);
				throw new ValidationException("Internal error validating field: " + field.getName(), e);
			} finally {
				field.setAccessible(wasAccessible);
			}
		}

		logger.info("Validation successful for entity: {}", entity);
	}

	private void validateLongField(Long value, String fieldName) throws ValidationException {
		if (value < 0) {
			throw new ValidationException(String.format("Field '%s' cannot be negative (value: %d)", fieldName, value));
		}
	}

	private void validateStringField(String value, Field field) throws ValidationException {
		var annotation = field.getAnnotation(StringValidationParameters.class);

		if (annotation == null) {
			throw new ValidationException(
					"Missing @StringValidationParameters annotation for field: " + field.getName());
		}

		FieldStringValidator.validate(value,
				annotation.minLength(),
				annotation.maxLength(),
				annotation.pattern(),
				annotation.isNullPossible());
	}

	private Method getGetterMethod(Class<?> clazz, Field field) throws ValidationException {
		String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
		try {
			return clazz.getMethod(getterName);
		} catch (NoSuchMethodException e) {
			logger.warn("No getter found for field: {}", field.getName());
			throw new ValidationException("No getter found for field: " + field.getName());
		}
	}
}
