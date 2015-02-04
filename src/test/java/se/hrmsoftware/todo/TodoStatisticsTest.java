package se.hrmsoftware.todo;

import org.junit.Before;
import org.junit.Test;

import se.hrmsoftware.todo.model.todoitem.TodoItemCompleted;
import se.hrmsoftware.todo.model.todoitem.TodoItemCreated;

import static org.junit.Assert.assertEquals;

public class TodoStatisticsTest {
	private TodoStatistics stats;

	@Before
	public void setUp() throws Exception {
		stats = new TodoStatistics();

		stats.on(new TodoItemCreated("test", "test", "foo"));
		stats.on(new TodoItemCreated("test2", "test", "bar"));
		stats.on(new TodoItemCompleted("test"));
		stats.on(new TodoItemCreated("test3", "flurp", "durp"));
		stats.on(new TodoItemCompleted("test2"));
	}

	@Test
	public void shouldCountTotals() throws Exception {
		assertEquals(3, stats.get().getTotalCreated());
		assertEquals(2, stats.get().getTotalCompleted());
	}
}