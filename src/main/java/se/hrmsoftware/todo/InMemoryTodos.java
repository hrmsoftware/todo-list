package se.hrmsoftware.todo;

import se.hrmsoftware.todo.model.Todo;
import se.hrmsoftware.todo.model.TodoList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class InMemoryTodos implements Todos {

	private final ConcurrentHashMap<String, ListOfTodos> storage = new ConcurrentHashMap<>();

	@Override
	public List<TodoList> lists() {
		return storage.entrySet().stream()
				.map((e) -> new TodoList(e.getKey(), e.getValue().size()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<List<Todo>> todosFor(String listName) {
		return ofNullable(storage.get(listName))
				.map(ListOfTodos::list);
	}

	@Override
	public TodoList createList(String name) {
		storage.putIfAbsent(name, new ListOfTodos());
		return of(storage.get(name)).map((e) -> new TodoList(name, e.size())).get();
	}

	@Override
	public Todo addTodo(String list, String todoText) {
		return ofNullable(storage.get(list))
				.map((e) -> e.add(todoText)) // Ugly side-effect ..
				.orElseThrow(() -> new RuntimeException("No such todo list"));
	}

	@Override
	public boolean removeTodo(String list, String todoId) {
		return ofNullable(storage.get(list))
				.map((e) -> e.remove(todoId))
				.orElse(false);
	}

	private static class ListOfTodos {
		private final List<Todo> todos = new ArrayList<>();

		public synchronized Todo add(String text) {
			Todo todo = new Todo("" + (size() + 1), text);
			todos.add(todo);
			return todo;
		}

		public synchronized boolean remove(String id) {
			return todos.removeIf((t) -> id.equals(t.getId()));
		}

		public synchronized int size() {
			return todos.size();
		}

		public List<Todo> list() {
			return new ArrayList<>(todos);
		}
	}

}
