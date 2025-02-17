package ua.foxminded.schoolapplication.model.dao.constants;

public enum NotFoundConstants {
	NOT_FOUND(-1, "NOT_FOUND");

	private final int id;
	private final String name;

	NotFoundConstants(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
