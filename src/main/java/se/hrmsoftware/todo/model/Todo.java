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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final Todo todo = (Todo) o;

		if (!id.equals(todo.id))
			return false;
		if (!text.equals(todo.text))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + text.hashCode();
		return result;
	}
}
