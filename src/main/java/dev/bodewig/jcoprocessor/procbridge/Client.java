package dev.bodewig.jcoprocessor.procbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executor;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Connects to a Server and sends requests
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class Client implements AutoCloseable {

	/**
	 * Constant to not set a timeout
	 */
	public static final long FOREVER = 0;

	private final Executor executor;
	private Socket socket;

	private final long timeout;

	/**
	 * Creates a new Client connecting to the given host and port
	 *
	 * @param host the Server host
	 * @param port the Server port
	 * @throws ClientException if connecting failed
	 */
	public Client(String host, int port) throws ClientException {
		this(host, port, FOREVER, null);
	}

	/**
	 * Creates a new Client connecting to the given host and port within a given
	 * time
	 *
	 * @param host     the Server host
	 * @param port     the Server port
	 * @param timeout  the connection timeout, 0 for forever
	 * @param executor the executor
	 * @throws ClientException if connecting failed
	 */
	public Client(String host, int port, long timeout, Executor executor) throws ClientException {
		this.timeout = timeout;
		this.executor = executor;
		Instant tryUntil = Instant.now().plus(Duration.ofMillis(timeout));
		IOException ex;
		do {
			try {
				ex = null;
				socket = new Socket(host, port);
			} catch (IOException e) {
				ex = e;
			}
		} while (!Thread.currentThread().isInterrupted() && ex != null
				&& (timeout <= 0 || Instant.now().isBefore(tryUntil)));
		if (ex != null) {
			throw new ClientException(ex);
		}
	}

	/**
	 * Get the executor
	 *
	 * @return the executor
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Get the host
	 *
	 * @return the host
	 */
	public final String getHost() {
		return socket.getInetAddress().toString();
	}

	/**
	 * Get the port
	 *
	 * @return the port
	 */
	public final int getPort() {
		return socket.getPort();
	}

	/**
	 * Get the timeout
	 *
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, Boolean payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, Double payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, Integer payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, JSONArray payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, JSONObject payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, Long payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	public final <T> T request(String method, String payload)
			throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	/**
	 * Sends a request to the target port
	 * <p>
	 * This method should not be used directly but is visible for other packages
	 *
	 * @param <T>     the expected return type for comfort (supports Boolean,
	 *                Double, Integer, JSONArray, JSONObject, Long, String)
	 * @param method  the requested method
	 * @param payload the request payload
	 * @return the response payload
	 * @throws ClientException  if an exception occurs in the Client
	 * @throws TimeoutException if the request reaches the configured timeout
	 * @throws ServerException  if an exception occurs in the Server
	 */
	@SuppressWarnings("unchecked")
	public final <T> T request(String method, Object payload)
			throws ClientException, TimeoutException, ServerException {
		if (socket.isClosed()) {
			throw new ClientException(new SocketException("Socket already closed"));
		}

		// final 1-sized array for usage in lambda
		final StatusCode[] respStatusCode = { null };
		final Object[] respPayload = { null };
		final Throwable[] innerException = { null };

		Runnable task = () -> {
			try {
				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();

				Protocol.writeRequest(os, method, payload);
				Map.Entry<StatusCode, Object> entry;
				do {
					entry = Protocol.readResponse(is).orElse(null);
				} while (!Thread.currentThread().isInterrupted() && !socket.isInputShutdown() && entry == null);
				respStatusCode[0] = entry.getKey();
				respPayload[0] = entry.getValue();

			} catch (Exception ex) {
				innerException[0] = ex;
			}
		};
		if (timeout <= 0) {
			task.run();
		} else {
			TimeoutExecutor guard = new TimeoutExecutor(timeout, executor);
			guard.execute(task);
		}

		if (innerException[0] != null) {
			throw new RuntimeException(innerException[0]);
		}

		if (respStatusCode[0] != StatusCode.GOOD_RESPONSE) {
			throw new ServerException((String) respPayload[0]);
		}

		return (T) respPayload[0];
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
