package se.hrmsoftware.todo.model.todoitem;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

public class CompleteTodoItem {
	@TargetAggregateIdentifier
	private final String id;

	public CompleteTodoItem(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
