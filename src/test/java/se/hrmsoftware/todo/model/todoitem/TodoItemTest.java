package se.hrmsoftware.todo.model.todoitem;

import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

public class TodoItemTest {
	private FixtureConfiguration fixture;

	@Before
	public void setUp() throws Exception {
		fixture = Fixtures.newGivenWhenThenFixture(TodoItem.class);
	}

	@Test
	public void testCreateTodoItem() throws Exception {
		fixture.given()
				.when(new CreateTodoItem("id", "list 1", "item 1"))
				.expectEvents(new TodoItemCreated("id", "list 1", "item 1"));
	}

	@Test
	public void testCompleteTodoItem() throws Exception {
		fixture.given(new TodoItemCreated("id", "list 1", "item 1"))
				.when(new CompleteTodoItem("id"))
				.expectEvents(new TodoItemCompleted("id"));
	}
}
