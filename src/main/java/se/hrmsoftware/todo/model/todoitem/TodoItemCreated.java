package se.hrmsoftware.todo.model.todoitem;

public class TodoItemCreated {
	private final String id;
	private final String list;
	private final String todo;

	public TodoItemCreated(String id, String list, String todo) {
		this.id = id;
		this.list = list;
		this.todo = todo;
	}

	public String getId() {
		return id;
	}

	public String getList() {
		return list;
	}

	public String getTodo() {
		return todo;
	}
}
