package se.hrmsoftware.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Before;
import org.junit.Test;

import se.hrmsoftware.todo.model.todoitem.CompleteTodoItem;
import se.hrmsoftware.todo.model.todoitem.CreateTodoItem;
import se.hrmsoftware.todo.model.todoitem.TodoItemCompleted;
import se.hrmsoftware.todo.model.todoitem.TodoItemCreated;

import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

public class AxonTodosTest {
	private AxonTodos todos;

	private List<Object> sentCommands;

	@Before
	public void setUp() throws Exception {
		sentCommands = new ArrayList<>();
		CommandGateway commandGateway = new CommandGateway() {
			@Override
			public <R> void send(Object command, CommandCallback<R> callback) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <R> R sendAndWait(Object command) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void send(Object command) {
				sentCommands.add(command);
			}
		};
		todos = new AxonTodos(commandGateway);

		todos.on(new TodoItemCreated("1", "1", "1"));
		todos.on(new TodoItemCreated("2", "1", "2"));
		todos.on(new TodoItemCompleted("1"));
		todos.on(new TodoItemCreated("3", "2", "1"));
		todos.on(new TodoItemCompleted("3"));
		todos.on(new TodoItemCreated("4", "2", "2"));
	}

	@Test
	public void testLists() throws Exception {
		assertEquals(2, todos.lists().size());
	}

	@Test
	public void testTodosFor() throws Exception {
		assertTrue(todos.todosFor("1").isPresent());
		assertEquals("2", todos.todosFor("1").get().get(0).getId());
		assertEquals("2", todos.todosFor("1").get().get(0).getText());
	}

	@Test
	public void testCreateList() throws Exception {
		assertFalse(todos.lists().stream().filter(list -> list.getName().equals("foo")).findFirst().isPresent());
		todos.createList("foo");
		assertTrue(todos.lists().stream().filter(list -> list.getName().equals("foo")).findFirst().isPresent());
	}

	@Test
	public void testAddTodo() throws Exception {
		todos.addTodo("foo", "bar");
		assertThat(sentCommands, hasItem(isA(CreateTodoItem.class)));
	}

	@Test
	public void testRemoveTodo() throws Exception {
		todos.removeTodo("1", "2");
		assertThat(sentCommands, hasItem(isA(CompleteTodoItem.class)));
	}
}