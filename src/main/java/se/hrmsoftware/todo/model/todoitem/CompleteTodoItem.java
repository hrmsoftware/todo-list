package se.hrmsoftware.todo.model.todoitem;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

public class CompleteTodoItem {
	@TargetAggregateIdentifier
	private final String id;

	public CompleteTodoItem(String id) {
		isValid(id);
		this.id = id;
	}

	private static void isValid(String str) {
		if(str == null || str.isEmpty()) {
			throw new IllegalArgumentException("value may not be null or empty");
		}
	}

	public String getId() {
		return id;
	}
}
