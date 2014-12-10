package se.hrmsoftware.todo;

import se.hrmsoftware.todo.model.Todo;
import se.hrmsoftware.todo.model.TodoList;

import java.util.List;
import java.util.Optional;

public interface Todos {
	List<TodoList> lists();

	Optional<List<Todo>> todosFor(String listName);

	TodoList createList(String name);

	Todo addTodo(String list, String todoText);

	boolean removeTodo(String list, String todoId);
}
