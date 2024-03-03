package dev.bodewig.jcoprocessor.procbridge;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class Connection implements Runnable {

	private static final Logger logger = Logger.getLogger(Connection.class.getName());

	private final Server server;
	private final Socket socket;

	public Connection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		try (OutputStream os = socket.getOutputStream(); InputStream is = socket.getInputStream()) {

			while (!Thread.currentThread().isInterrupted()) {
				Map.Entry<String, JSONObject> req;
				do {
					req = Protocol.readRequest(is).orElse(null);
				} while (!Thread.currentThread().isInterrupted() && !socket.isInputShutdown() && req == null);
				String method = req.getKey();
				JSONObject payload = req.getValue();

				JSONObject result = null;
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