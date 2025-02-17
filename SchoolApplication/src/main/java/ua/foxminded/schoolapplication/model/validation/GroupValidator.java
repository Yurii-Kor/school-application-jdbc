package ua.foxminded.schoolapplication.model.validation;

import ua.foxminded.schoolapplication.model.domain.Group;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupValidator {
	private static final Logger logger = LoggerFactory.getLogger(GroupValidator.class);

	private static final int MIN_NAME_LENGTH = 2;
	private static final int MAX_NAME_LENGTH = 50;

	private static final String GROUP_NAME_REGEX = "^[A-Za-z]+-\\d+$";
	private static final Pattern GROUP_NAME_PATTERN = Pattern.compile(GROUP_NAME_REGEX);

	public void validateGroupName(String groupName) {
		if (groupName == null || groupName.trim().isEmpty()) {
			throw new IllegalArgumentException("Group name cannot be null or empty.");
		}

		int length = groupName.trim().length();
		if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(
					String.format("Group name must be between %d and %d characters, but was: %d",
							MIN_NAME_LENGTH,
							MAX_NAME_LENGTH,
							length));
		}

		if (!GROUP_NAME_PATTERN.matcher(groupName).matches()) {
			throw new IllegalArgumentException(
					String.format("Group name does not match the required format: %s", GROUP_NAME_REGEX));
		}
	}

	public boolean validateGroups(Group... groups) {
		if (groups == null) {
			logger.error("GroupValidator received a null argument");
			return false;
		}

		for (Group group : groups) {
			if (!validateOneGroup(group)) {
				String errorMessage = String.format("Validation failed for group: %s", group);
				logger.error(errorMessage);
				return false;
			}
		}

		return true;
	}

	private boolean validateOneGroup(Group group) {
		if (group == null) {
			logger.error("Validation failed: Group cannot be null.");
			return false;
		}

		try {
			validateGroupName(group.getGroupName());
			logger.info("Validation successful for group: {}", group);
		} catch (IllegalArgumentException e) {
			logger.error("Validation failed: {}", e.getMessage());
			return false;
		}

		return true;
	}
}
