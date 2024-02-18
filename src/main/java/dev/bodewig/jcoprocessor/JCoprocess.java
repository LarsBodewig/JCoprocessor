package dev.bodewig.jcoprocessor;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import dev.bodewig.jcoprocessor.procbridge.Client;
import dev.bodewig.jcoprocessor.procbridge.Server;

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

		command = new LinkedList<String>();
		command.add(javaBin);
		command.add("-cp");
		command.add(classpath);
		command.add(className);
	}

	public void start() throws IOException {
		synchronized (PORT_LOCK) {
			int port = findFreePort();
			command.add(Integer.toString(port));
			synchronized (BUILDER_LOCK) {
				ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
				process = builder.start();
			}
			client = new Client(/* localhost */ null, port);
		}
	}

	public synchronized void stop() {
		process.destroy();
	}

	private static int findFreePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	public synchronized JSONObject request(String method, JSONObject payload) {
		if (!process.isAlive()) {
			throw new IllegalStateException("Process is not alive");
		}
		return client.request(method, payload);
	}
}