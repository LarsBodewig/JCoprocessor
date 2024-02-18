package dev.bodewig.jcoprocessor.procbridge;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server implements IDelegate {

	protected ExecutorService executor;
	protected PrintStream logger;
	protected final int port;

	protected ServerSocket serverSocket;

	protected boolean started;

	public Server(int port) {
		this.port = port;
		this.started = false;
		this.executor = null;
		this.serverSocket = null;
		this.logger = System.err;
	}

	public PrintStream getLogger() {
		return logger;
	}

	public final int getPort() {
		return port;
	}

	public final synchronized boolean isStarted() {
		return started;
	}

	public void setLogger(PrintStream logger) {
		this.logger = logger;
	}

	public synchronized void start() {
		if (started) {
			throw new IllegalStateException("server already started");
		}

		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			throw new ServerException(e);
		}
		this.serverSocket = serverSocket;

		final ExecutorService executor = Executors.newCachedThreadPool();
		this.executor = executor;
		executor.execute(() -> {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					Connection conn = new Connection(this, socket, this);
					synchronized (Server.this) {
						if (!started) {
							return; // finish listener
						}
						executor.execute(conn);
					}
				} catch (IOException ignored) {
					return; // finish listener
				}
			}
		});

		started = true;
	}

	public synchronized void stop() {
		if (!started) {
			throw new IllegalStateException("server does not started");
		}

		executor.shutdown();
		executor = null;

		try {
			serverSocket.close();
		} catch (IOException ignored) {
		}
		serverSocket = null;

		this.started = false;
	}

}
