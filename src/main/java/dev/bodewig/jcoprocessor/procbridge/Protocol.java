package dev.bodewig.jcoprocessor.procbridge;

import static dev.bodewig.jcoprocessor.procbridge.ProtocolException.INCOMPATIBLE_VERSION;
import static dev.bodewig.jcoprocessor.procbridge.ProtocolException.INCOMPLETE_DATA;
import static dev.bodewig.jcoprocessor.procbridge.ProtocolException.INVALID_BODY;
import static dev.bodewig.jcoprocessor.procbridge.ProtocolException.INVALID_STATUS_CODE;
import static dev.bodewig.jcoprocessor.procbridge.ProtocolException.UNRECOGNIZED_PROTOCOL;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

/**
 * Reads and writes requests and responses according to the ProcBridge protocol
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public final class Protocol {

	private static final byte[] FLAG = { 'p', 'b' };

	private static Optional<Map.Entry<StatusCode, JSONObject>> read(InputStream stream)
			throws IOException, ProtocolException {
		int b;

		// 1. FLAG
		b = stream.read();
		if (b == -1) {
			return Optional.empty();
		}
		if (b != FLAG[0]) {
			throw new ProtocolException(UNRECOGNIZED_PROTOCOL);
		}
		b = stream.read();
		if (b == -1 || b != FLAG[1]) {
			throw new ProtocolException(UNRECOGNIZED_PROTOCOL);
		}

		// 2. VERSION
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		if (b != Versions.CURRENT[0]) {
			throw new ProtocolException(INCOMPATIBLE_VERSION);
		}
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		if (b != Versions.CURRENT[1]) {
			throw new ProtocolException(INCOMPATIBLE_VERSION);
		}

		// 3. STATUS CODE
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		StatusCode statusCode = StatusCode.fromRawValue(b);
		if (statusCode == null) {
			throw new ProtocolException(INVALID_STATUS_CODE);
		}

		// 4. RESERVED BYTES (2 bytes)
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}

		// 5. LENGTH (little endian)
		int bodyLen;
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		bodyLen = b;
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		bodyLen |= (b << 8);
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		bodyLen |= (b << 16);
		b = stream.read();
		if (b == -1) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}
		bodyLen |= (b << 24);

		// 6. JSON OBJECT
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int readCount;
		int restCount = bodyLen;
		byte[] buf = new byte[Math.min(bodyLen, 1024 * 1024)];
		while ((readCount = stream.read(buf, 0, Math.min(buf.length, restCount))) != -1) {
			buffer.write(buf, 0, readCount);
			restCount -= readCount;
			if (restCount == 0) {
				break;
			}
		}

		if (buffer.size() != bodyLen) {
			throw new ProtocolException(INCOMPLETE_DATA);
		}

		buffer.flush();
		buf = buffer.toByteArray();

		try {
			String jsonText = new String(buf, StandardCharsets.UTF_8);
			JSONObject body = new JSONObject(jsonText);
			return Optional.of(new AbstractMap.SimpleEntry<>(statusCode, body));
		} catch (Exception ex) {
			throw new ProtocolException(INVALID_BODY);
		}
	}

	/**
	 * Reads a request from the given InputStream if available
	 *
	 * @param stream the stream to read from
	 * @return an optional tuple of the request method and payload
	 * @throws IOException       if reading fails
	 * @throws ProtocolException if the data does not match the protocol
	 */
	public static Optional<Map.Entry<String, Object>> readRequest(InputStream stream)
			throws IOException, ProtocolException {
		return read(stream).map(entry -> {
			StatusCode statusCode = entry.getKey();
			JSONObject body = entry.getValue();
			if (statusCode != StatusCode.REQUEST) {
				throw new ProtocolException(INVALID_STATUS_CODE);
			}
			String method = body.optString(Key.METHOD.toString());
			Object payload = body.opt(Key.PAYLOAD.toString());
			return new AbstractMap.SimpleEntry<>(method, payload);
		});
	}

	/**
	 * Reads a response from the given InputStream if available
	 *
	 * @param stream the stream to read from
	 * @return an optional tuple of the response StatusCode and payload
	 * @throws IOException       if reading fails
	 * @throws ProtocolException if the data does not match the protocol
	 */
	public static Optional<Map.Entry<StatusCode, Object>> readResponse(InputStream stream)
			throws IOException, ProtocolException {
		return read(stream).map(entry -> {
			StatusCode statusCode = entry.getKey();
			JSONObject body = entry.getValue();
			if (statusCode == StatusCode.GOOD_RESPONSE) {
				return new AbstractMap.SimpleEntry<>(StatusCode.GOOD_RESPONSE,
						body.opt(Key.PAYLOAD.toString()));
			} else if (statusCode == StatusCode.BAD_RESPONSE) {
				return new AbstractMap.SimpleEntry<>(StatusCode.BAD_RESPONSE, body.optString(Key.MESSAGE.toString()));
			} else {
				throw new ProtocolException(INVALID_STATUS_CODE);
			}
		});
	}

	private static void write(OutputStream stream, StatusCode statusCode, JSONObject body) throws IOException {
		// 1. FLAG 'p', 'b'
		stream.write(FLAG);

		// 2. VERSION
		stream.write(Versions.CURRENT);

		// 3. STATUS CODE
		stream.write(statusCode.rawValue);

		// 4. RESERVED BYTES (2 bytes)
		stream.write(0);
		stream.write(0);

		// make json object
		byte[] buf = body.toString().getBytes(StandardCharsets.UTF_8);

		// 5. LENGTH (4-byte, little endian)
		int len = buf.length;
		int b0 = len & 0xff;
		int b1 = (len & 0xff00) >> 8;
		int b2 = (len & 0xff0000) >> 16;
		int b3 = (len & 0xff000000) >> 24;
		stream.write(b0);
		stream.write(b1);
		stream.write(b2);
		stream.write(b3);

		// 6. JSON OBJECT
		stream.write(buf);

		stream.flush();
	}

	/**
	 * Writes a non-successful response to the given OutputStream
	 *
	 * @param stream    the stream to write to
	 * @param exception the Exception to send
	 * @throws IOException if writing fails
	 */
	public static void writeBadResponse(OutputStream stream, Exception exception) throws IOException {
		JSONObject body = new JSONObject();
		if (exception != null) {
			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				exception.printStackTrace(pw);
				body.put(Key.MESSAGE.toString(), sw.toString());
			}
		}
		write(stream, StatusCode.BAD_RESPONSE, body);
	}

	/**
	 * Writes a successful response to the given OutputStream
	 *
	 * @param stream  the stream to write to
	 * @param payload the response payload
	 * @throws IOException if writing fails
	 */
	public static void writeGoodResponse(OutputStream stream, Object payload) throws IOException {
		JSONObject body = new JSONObject();
		if (payload != null) {
			body.put(Key.PAYLOAD.toString(), payload);
		}
		write(stream, StatusCode.GOOD_RESPONSE, body);
	}

	/**
	 * Writes a request to the given OutputStream
	 *
	 * @param stream  the stream to write to
	 * @param method  the method to request
	 * @param payload the request payload
	 * @throws IOException if writing fails
	 */
	public static void writeRequest(OutputStream stream, String method, Object payload) throws IOException {
		JSONObject body = new JSONObject();
		if (method != null) {
			body.put(Key.METHOD.toString(), method);
		}
		if (payload != null) {
			body.put(Key.PAYLOAD.toString(), payload);
		}
		write(stream, StatusCode.REQUEST, body);
	}

	private Protocol() {
	}
}
