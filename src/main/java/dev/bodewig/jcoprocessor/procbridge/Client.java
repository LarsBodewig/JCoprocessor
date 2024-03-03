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

import org.json.JSONObject;

public class Client implements AutoCloseable {

	public static final long FOREVER = 0;

	private final Executor executor;
	private Socket socket;

	private final long timeout;

	public Client(String host, int port) throws ClientException {
		this(host, port, FOREVER, null);
	}

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

	public Executor getExecutor() {
		return executor;
	}

	public final String getHost() {
		return socket.getInetAddress().toString();
	}

	public final int getPort() {
		return socket.getPort();
	}

	public long getTimeout() {
		return timeout;
	}

	public final JSONObject request(String method, JSONObject payload)
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

		return (JSONObject) respPayload[0];
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
