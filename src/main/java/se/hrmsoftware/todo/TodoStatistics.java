package se.hrmsoftware.todo;

import java.util.concurrent.atomic.AtomicLong;

import org.axonframework.eventhandling.annotation.EventHandler;

import se.hrmsoftware.todo.model.todoitem.TodoItemCompleted;
import se.hrmsoftware.todo.model.todoitem.TodoItemCreated;

public class TodoStatistics {
	private final AtomicLong created = new AtomicLong();
	private final AtomicLong completed = new AtomicLong();

	public Statistics get() {
		return new Statistics(created.get(), completed.get());
	}

	@EventHandler
	public void on(TodoItemCreated event) {
		created.incrementAndGet();
	}

	@EventHandler
	public void on(TodoItemCompleted event) {
		completed.incrementAndGet();
	}
}
