package se.hrmsoftware.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import se.hrmsoftware.todo.model.Todo;
import se.hrmsoftware.todo.model.TodoList;
import se.hrmsoftware.todo.model.todoitem.CompleteTodoItem;
import se.hrmsoftware.todo.model.todoitem.CreateTodoItem;
import se.hrmsoftware.todo.model.todoitem.TodoItemCompleted;
import se.hrmsoftware.todo.model.todoitem.TodoItemCreated;

public class AxonTodos implements Todos {
	private final CommandGateway commandGateway;

	private final Map<String, List<Todo>> todos = new ConcurrentHashMap<>();

	public AxonTodos(CommandGateway commandGateway) {
		this.commandGateway = commandGateway;
	}

	@Override
	public List<TodoList> lists() {
		List<TodoList> lists = new ArrayList<>();
		todos.entrySet().stream().map(entry -> new TodoList(entry.getKey(), entry.getValue().size())).forEach(lists::add);
		return lists;
	}

	@Override
	public Optional<List<Todo>> todosFor(String listName) {
		return Optional.ofNullable(todos.get(listName));
	}

	@Override
	public TodoList createList(String name) {
		todos.put(name, new ArrayList<>());
		return new TodoList(name, 0);
	}

	@Override
	public Todo addTodo(String listName, String todoText) {
		String id = UUID.randomUUID().toString();
		commandGateway.send(new CreateTodoItem(id, listName, todoText));
		return new Todo(id, todoText);
	}

	@Override
	public boolean removeTodo(String list, String todoId) {
		Optional<Todo> todo = todosFor(list).map(l -> l.stream().filter(item -> item.getId().equals(todoId)).findFirst().get());
		todo.ifPresent(item -> commandGateway.send(new CompleteTodoItem(todoId)));
		return todo.isPresent();
	}

	@EventHandler
	public void on(TodoItemCreated event) {
		List<Todo> todosForList = todos.get(event.getList());
		if (todosForList == null) {
			todosForList = new CopyOnWriteArrayList<>();
			todos.put(event.getList(), todosForList);
		}
		todosForList.add(new Todo(event.getId(), event.getTodo()));
	}

	@EventHandler
	public void on(TodoItemCompleted event) {
		for (Map.Entry<String, List<Todo>> entry : todos.entrySet()) {
			entry.getValue().stream().filter(item -> item.getId().equals(event.getId())).findFirst().map(
					entry.getValue()::remove);
		}
	}
}
