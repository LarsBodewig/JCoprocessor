package dev.bodewig.jcoprocessor;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import dev.bodewig.jcoprocessor.procbridge.Client;
import dev.bodewig.jcoprocessor.procbridge.Server;

/**
 * A Client-Server pair to run tasks in a co-process
 *
 * @author Lars Bodewig
 */
public class JCoprocess {

	private static final Object PORT_LOCK = new Object();
	private static final Object BUILDER_LOCK = new Object();

	/**
	 * The command list for the ProcessBuilder
	 */
	protected final List<String> command;

	/**
	 * The Server process
	 */
	protected Process process;

	/**
	 * The Client
	 */
	protected Client client;

	/**
	 * Creates a new JCoprocess that is not yet running
	 * <p>
	 * The Server class needs to declare a main method that takes the port to run
	 * the Server on as an argument.
	 *
	 * @param server the Server class
	 */
	public JCoprocess(Class<? extends Server> server) {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = server.getName();

		command = new LinkedList<>();
		command.add(javaBin);
		command.add("-cp");
		command.add(classpath);
		command.add(className);
	}

	/**
	 * Finds a free port and starts the Server before connecting the Client
	 *
	 * @param timeoutMillis the connection timeout, 0 for forever
	 * @throws IOException if starting the Server fails
	 */
	public void start(long timeoutMillis) throws IOException {
		synchronized (PORT_LOCK) {
			int port = findFreePort();
			command.add(Integer.toString(port));
			synchronized (BUILDER_LOCK) {
				ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
				process = builder.start();
			}
			try {
				client = new Client(/* localhost */ null, port, timeoutMillis, null);
			} catch (RuntimeException e) {
				stop();
				throw e;
			}
		}
	}

	/**
	 * Closes the socket and destroys the Server process
	 */
	public synchronized void stop() {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private static int findFreePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	/**
	 * Sends a request to the target Server and casts the result to the correct
	 * Number type
	 *
	 * @param <T>    the expected return type for comfort (Double, Integer, Long)
	 * @param method the requested method
	 * @param type   Double, Integer or Long
	 * @return the response payload
	 */
	public <T extends Number> T request(String method, Class<T> type) {
		return request(method, null, type);
	}

	/**
	 * Sends a request to the target Server
	 *
	 * @param <T>    the expected return type for comfort (supports Boolean, Double,
	 *               Integer, JSONArray, JSONObject, Long, String)
	 * @param method the requested method
	 * @return the response payload
	 */
	public <T> T request(String method) {
		return request(method, (Object) null);
	}

	/**
	 * Sends a request to the target Server and casts the result to the correct
	 * Number type
	 *
	 * @param <T>     the expected return type for comfort (Double, Integer, Long)
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @param type    Double, Integer or Long
	 * @return the response payload
	 */
	public synchronized <T extends Number> T request(String method, Object payload, Class<T> type) {
		if (!process.isAlive()) {
			throw new IllegalStateException("Process is not alive");
		}
		return client.request(method, payload, type);
	}

	/**
	 * Sends a request to the target Server
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return the response payload
	 */
	public synchronized <T> T request(String method, Object payload) {
		if (!process.isAlive()) {
			throw new IllegalStateException("Process is not alive");
		}
		return client.request(method, payload);
	}
}
