package se.hrmsoftware.todo.model;

/**
 * A named list of todos.
 */
public class TodoList {
	private final String name;
	private final int size;

	public TodoList(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}
}
