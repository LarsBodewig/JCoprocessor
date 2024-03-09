package dev.bodewig.jcoprocessor.procbridge;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A connection between a Server handling a request and a socket
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class Connection implements Runnable {

	private static final Logger logger = Logger.getLogger(Connection.class.getName());

	private final Server server;
	private final Socket socket;

	/**
	 * Creates a new Connection between the given Server and socket
	 *
	 * @param server the Server handling the request
	 * @param socket the socket to read from and write to
	 */
	public Connection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		try (OutputStream os = socket.getOutputStream(); InputStream is = socket.getInputStream()) {

			while (!Thread.currentThread().isInterrupted()) {
				Map.Entry<String, Object> req;
				do {
					req = Protocol.readRequest(is).orElse(null);
				} while (!Thread.currentThread().isInterrupted() && !socket.isInputShutdown() && req == null);
				String method = req.getKey();
				Object payload = req.getValue();

				Object result = null;
				Exception exception = null;
				try {
					result = server.handleRequest(method, payload);
				} catch (Exception ex) {
					exception = ex;
				}

				if (exception != null) {
					Protocol.writeBadResponse(os, exception);
				} else {
					Protocol.writeGoodResponse(os, result);
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception occured in connection on port " + server.getPort(), ex);
		}
	}
}