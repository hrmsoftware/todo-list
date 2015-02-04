package se.hrmsoftware.todo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;
import spark.Spark;
import spark.SparkBase;
import spark.utils.IOUtils;

import se.hrmsoftware.todo.model.todoitem.TodoItem;

import com.google.gson.Gson;

import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static spark.Spark.*;

/**
 * The main application.
 */
public class Application {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);
	private static Todos TODOS;
	private static TodoStatistics STATISTICS;
	private static Gson GSON = new Gson();

	/**
	 * This bootstraps the application.
	 * @param args .
	 */
	public static void main(String[] args) {
		setupAxon();
		assignServerPort(7777);
		registerShutdownHook(SparkBase::stop);
		registerRoutes();
	}

	/**
	 * The routes (REST-style) of the application.
	 */
	private static void registerRoutes() {

		before((request, response) -> {
			response.type("application/json; charset=UTF-8");
			response.header("Access-Control-Allow-Origin", "*");
		});

		get("/", (req, resp) ->
						TODOS.lists(),
				jsonTransformer());

		// Load the index.html
		get("/html", (req, resp) -> {
			resp.type("text/html");
			return IOUtils.toString(Application.class.getResourceAsStream("/views/index.html"));
		});

		get("/stats", (req, resp) ->
						STATISTICS.get(),
				jsonTransformer());

		post("/", (req, resp) ->
						TODOS.createList(req.body()),
				jsonTransformer());

		get("/:list", (req, resp) ->
						TODOS.todosFor(req.params(":list"))
								.orElseThrow(() -> new RuntimeException("No such list")),
				jsonTransformer());
		post("/:list", (req, resp) ->
						TODOS.addTodo(req.params(":list"), req.body()),
				jsonTransformer());

		delete("/:list/:id", (req, resp) -> {
					if (!TODOS.removeTodo(req.params(":list"), req.params(":id"))) {
						throw new RuntimeException("No such todo!");
					}
					return TODOS.todosFor(req.params(":list"))
							.orElseThrow(() -> new RuntimeException("No such list"));
				},
				jsonTransformer());

		exception(Exception.class, (e, request, response) -> {
			Map<String, Object> msg = new HashMap<>();
			msg.put("type", e.getClass().getSimpleName());
			msg.put("message", e.getMessage());
			response.status(500);
			try {
				response.body(jsonTransformer().render(msg));
			}
			catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		});
	}

	private static ResponseTransformer jsonTransformer() {
		return GSON::toJson;
	}


	/**
	 * Read the value of system property 'port'.
	 * @param defaultPort the port to use if no system property is defined.
	 */
	private static void assignServerPort(int defaultPort) {
		Function<String, Integer> portFun = s -> {
			LOG.info("Overriding default port with {}", s);
			return valueOf(s);
		};
		Spark.port(Optional.ofNullable(getProperty("port")).map(portFun).orElse(defaultPort));
	}


	/**
	 * @param hook some code that will be run when the application shuts down.
	 */
	private static void registerShutdownHook(Runnable hook) {
		Runtime.getRuntime().addShutdownHook(new Thread(hook));
	}

	/**
	 * Setup the Axon Framework
	 */
	private static void setupAxon() {
		CommandBus commandBus = new SimpleCommandBus();
		CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

		final File eventDirectory = new File("target/events");
		EventStore eventStore = new FileSystemEventStore(new SimpleEventFileResolver(eventDirectory));

		EventBus eventBus = new SimpleEventBus();
		EventSourcingRepository<TodoItem> repository = new EventSourcingRepository<>(TodoItem.class, eventStore);
		repository.setEventBus(eventBus);

		AggregateAnnotationCommandHandler.subscribe(TodoItem.class, repository, commandBus);
		AxonTodos axonTodos = new AxonTodos(commandGateway);
		TODOS = axonTodos;
		AnnotationEventListenerAdapter.subscribe(TODOS, eventBus);

		STATISTICS = new TodoStatistics();
		AnnotationEventListenerAdapter.subscribe(STATISTICS, eventBus);

		replayEvents(eventDirectory, eventStore, axonTodos, STATISTICS);
	}

	private static void replayEvents(File eventDirectory, EventStore eventStore, Object... eventHandlers) {
		new FileEventReplayer(eventDirectory, eventStore).replayEvents(eventHandlers);
	}

	public static class FileEventReplayer {
		private final File eventDirectory;
		private final EventStore eventStore;

		public FileEventReplayer(File eventDirectory, EventStore eventStore) {
			this.eventDirectory = eventDirectory;
			this.eventStore = eventStore;
		}

		public void replayEvents(Object... eventHandlers) {
			// hack to replay events from the event store
			AnnotationEventListenerAdapter[] adapters = asList(eventHandlers).stream().map(AnnotationEventListenerAdapter::new).toArray(AnnotationEventListenerAdapter[]::new);

			for (String file : new File(eventDirectory, TodoItem.class.getSimpleName()).list()) {
				DomainEventStream stream = eventStore.readEvents(TodoItem.class.getSimpleName(),
						file.substring(0, file.lastIndexOf('.')));
				while (stream.hasNext()) {
					DomainEventMessage message = stream.next();
					for (AnnotationEventListenerAdapter adapter : adapters) {
						adapter.handle(message);
					}
				}
			}
		}
	}
}
