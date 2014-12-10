package se.hrmsoftware.todo.model;

/**
 * A todo (*doh*)
 */
public class Todo {

	private final String id;
	private final String text;

	public Todo(String id, String text) {
		this.id = id;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

}
