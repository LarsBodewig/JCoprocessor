package dev.bodewig.jcoprocessor.procbridge;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Opens a socket to listen for Client requests and handles them asynchronously
 * <p>
 * Servers should declare a main method accepting the port number as argument to
 * create and start a Server instance as JCoprocess
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public abstract class Server implements IDelegate {

	/**
	 * The executor used to handle requests
	 */
	protected ExecutorService executor;

	/**
	 * The port to listen to
	 */
	protected final int port;

	/**
	 * The socket used to listen
	 */
	protected ServerSocket serverSocket;

	/**
	 * If the Server is running
	 */
	protected boolean started;

	/**
	 * Creates a new Server that is not yet running
	 *
	 * @param port the port to listen to
	 */
	public Server(int port) {
		this.port = port;
		this.started = false;
		this.executor = null;
		this.serverSocket = null;
	}

	/**
	 * Get the port
	 *
	 * @return the port
	 */
	public final int getPort() {
		return port;
	}

	/**
	 * Return if the Server is running
	 *
	 * @return if the Server is running
	 */
	public final synchronized boolean isStarted() {
		return started;
	}

	/**
	 * Creates a new Executor and opens a socket to accept requests until
	 * interrupted
	 *
	 * @throws IllegalStateException if the Server is already running
	 */
	public synchronized void start() {
		if (started) {
			throw new IllegalStateException("Server already started");
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
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = serverSocket.accept();
					Connection conn = new Connection(this, socket);
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

	/**
	 * Stops the Server by shutting down the executor and closing the socket
	 *
	 * @throws IllegalStateException if the Server is not running
	 */
	public synchronized void stop() {
		if (!started) {
			throw new IllegalStateException("Server has not started");
		}

		executor.shutdown();
		executor = null;

		try {
			serverSocket.close();
		} catch (IOException ignored) {
		}
		serverSocket = null;

		started = false;
	}
}
