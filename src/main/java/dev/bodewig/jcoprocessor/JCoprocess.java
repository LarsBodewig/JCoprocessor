package dev.bodewig.jcoprocessor;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.bodewig.jcoprocessor.procbridge.Client;
import dev.bodewig.jcoprocessor.procbridge.ClientException;
import dev.bodewig.jcoprocessor.procbridge.Server;
import dev.bodewig.jcoprocessor.procbridge.ServerException;
import dev.bodewig.jcoprocessor.procbridge.TimeoutException;

public class JCoprocess {

	private static final Object PORT_LOCK = new Object();
	private static final Object BUILDER_LOCK = new Object();

	protected final List<String> command;
	protected Process process;
	protected Client client;

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

	public <T> T request(String method, Boolean payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, Double payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, Integer payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, JSONArray payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, JSONObject payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, Long payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public <T> T request(String method, String payload) throws ClientException, TimeoutException, ServerException {
		return request(method, (Object) payload);
	}

	public synchronized <T> T request(String method, Object payload) {
		if (!process.isAlive()) {
			throw new IllegalStateException("Process is not alive");
		}
		return client.request(method, payload);
	}
}
