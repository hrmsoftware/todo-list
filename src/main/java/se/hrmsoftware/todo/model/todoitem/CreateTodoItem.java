package se.hrmsoftware.todo.model.todoitem;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

public class CreateTodoItem {
	@TargetAggregateIdentifier
	private final String id;
	private final String listName;
	private final String todo;

	public CreateTodoItem(String id, String listName, String todo) {
		isValid(id);
		isValid(listName);
		isValid(todo);
		this.id = id;
		this.listName = listName;
		this.todo = todo;
	}

	public String getId() {
		return id;
	}

	public String getListName() {
		return listName;
	}

	public String getTodo() {
		return todo;
	}

	private static void isValid(String str) {
		if(str == null || str.isEmpty()) {
			throw new IllegalArgumentException("value may not be null or empty");
		}
	}
}
