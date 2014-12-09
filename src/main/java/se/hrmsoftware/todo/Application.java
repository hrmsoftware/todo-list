package se.hrmsoftware.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static spark.Spark.get;
import static spark.Spark.setPort;
import static spark.Spark.stop;

/**
 * The main application.
 */
public class Application {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	/**
	 * This bootstraps the application.
	 * @param args .
	 */
	public static void main(String[] args) {
		assignServerPort(7777);
		registerShutdownHook(() -> stop());
		registerRoutes();
	}

	/**
	 * The routes (REST-style) of the application.
	 */
	private static void registerRoutes() {
		get("/", (request, response) -> 345);
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
		setPort(Optional.ofNullable(getProperty("port")).map(portFun).orElse(defaultPort));
	}

	/**
	 * @param hook some code that will be run when the application shuts down.
	 */
	private static void registerShutdownHook(Runnable hook) {
		Runtime.getRuntime().addShutdownHook(new Thread(hook));
	}
}
