package se.hrmsoftware.todo.model.todoitem;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

public class TodoItem extends AbstractAnnotatedAggregateRoot<String> {
	@AggregateIdentifier
	private String id;

	public TodoItem() {
	}

	@CommandHandler
	public TodoItem(CreateTodoItem command) {
		apply(new TodoItemCreated(command.getId(), command.getListName(), command.getTodo()));
	}

	@CommandHandler
	public void complete(CompleteTodoItem command) {
		apply(new TodoItemCompleted(command.getId()));
	}

	@EventHandler
	public void on(TodoItemCreated event) {
		id = event.getId();
	}
}
