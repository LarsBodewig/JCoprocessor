package dev.bodewig.jcoprocessor.procbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Executor;

import org.json.JSONObject;

public class Client implements AutoCloseable {

	public static final long FOREVER = 0;

	private final Executor executor;
	private final Socket socket;

	private final long timeout;

	public Client(String host, int port) throws ClientException {
		this(host, port, FOREVER, null);
	}

	public Client(String host, int port, long timeout, Executor executor) throws ClientException {
		try {
			this.socket = new Socket(host, port);
		} catch (IOException e) {
			throw new ClientException(e);
		}
		this.timeout = timeout;
		this.executor = executor;
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
		final StatusCode[] respStatusCode = { null };
		final Object[] respPayload = { null };
		final Throwable[] innerException = { null };

		Runnable task = () -> {
			try (OutputStream os = socket.getOutputStream(); InputStream is = socket.getInputStream()) {

				Protocol.writeRequest(os, method, payload);
				Map.Entry<StatusCode, Object> entry = Protocol.readResponse(is);
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

		return new JSONObject(respPayload[0]);
	}

	@Override
	public void close() throws Exception {
		socket.close();
	}
}
