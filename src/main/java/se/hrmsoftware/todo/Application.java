package se.hrmsoftware.todo;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.TODO;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;
import spark.SparkBase;
import spark.utils.IOUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import se.hrmsoftware.todo.model.todoitem.TodoItem;

import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * The main application.
 */
public class Application {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);
	private static Todos TODOS;
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

		exception(Exception.class, new ExceptionHandler() {
			@Override
			public void handle(Exception e, Request request, Response response) {
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
		EventStore eventStore = new FileSystemEventStore(new SimpleEventFileResolver(new File("target/events")));
		EventBus eventBus = new SimpleEventBus();
		EventSourcingRepository<TodoItem> repository = new EventSourcingRepository<>(TodoItem.class, eventStore);
		repository.setEventBus(eventBus);

		AggregateAnnotationCommandHandler.subscribe(TodoItem.class, repository, commandBus);
		TODOS = new AxonTodos(commandGateway);
		AnnotationEventListenerAdapter.subscribe(TODOS, eventBus);
	}
}
