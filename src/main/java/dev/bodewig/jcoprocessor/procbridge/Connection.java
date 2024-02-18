package dev.bodewig.jcoprocessor.procbridge;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.json.JSONObject;

public final class Connection implements Runnable {

	private final Server server;
	private final IDelegate delegate;
	private final Socket socket;

	public Connection(Server server, Socket socket, IDelegate delegate) {
		this.server = server;
		this.socket = socket;
		this.delegate = delegate;
	}

	@Override
	public void run() {
		try (OutputStream os = socket.getOutputStream(); InputStream is = socket.getInputStream()) {

			Map.Entry<String, JSONObject> req = Protocol.readRequest(is);
			String method = req.getKey();
			JSONObject payload = req.getValue();

			JSONObject result = null;
			Exception exception = null;
			try {
				result = delegate.handleRequest(method, payload);
			} catch (Exception ex) {
				exception = ex;
			}

			if (exception != null) {
				Protocol.writeBadResponse(os, exception.getMessage());
			} else {
				Protocol.writeGoodResponse(os, result);
			}
		} catch (Exception ex) {
			if (this.server.logger != null) {
				ex.printStackTrace(this.server.logger);
			}
		}
	}

}