package dev.bodewig.jcoprocessor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.bodewig.jcoprocessor.procbridge.Server;

class JCoprocessTest {

	static class CalculatorServer extends Server {

		public CalculatorServer(int port) {
			super(port);
		}

		@Override
		public JSONObject handleRequest(String method, JSONObject payload) {
			DoubleStream values = payload.getJSONArray("values").toList().stream().map(Object::toString)
					.mapToDouble(Double::valueOf);
			double result = switch (method) {
			case "addition":
				yield values.sum();
			case "subtraction":
				yield values.reduce((left, right) -> left - right).getAsDouble();
			case "multiplication":
				yield values.reduce((left, right) -> left * right).getAsDouble();
			case "division":
				yield values.reduce((left, right) -> left / right).getAsDouble();
			default:
				throw new IllegalArgumentException("Unexpected method: " + method);
			};
			return new JSONObject().put("result", result);
		}

		public static void main(String[] args) {
			if (args.length <= 0) {
				throw new IllegalArgumentException("Missing argument: port");
			}
			int port = Integer.valueOf(args[0]);
			CalculatorServer server = new CalculatorServer(port);
			server.start();
		}
	}

	static JCoprocess process;

	@BeforeAll
	static void setup() throws IOException {
		JCoprocessManager.terminateOnShutdown();
		process = JCoprocessManager.spawn(CalculatorServer.class);
	}

	@Test
	void test_addition() throws IOException {
		JSONObject expected = new JSONObject().put("result", 5);
		JSONObject actual = process.request("addition", new JSONObject().put("values", new JSONArray().put(3).put(2)));
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void test_subtraction() throws IOException {
		JSONObject expected = new JSONObject().put("result", 1);
		JSONObject actual = process.request("subtraction",
				new JSONObject().put("values", new JSONArray().put(3).put(2)));
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void test_multiplication() throws IOException {
		JSONObject expected = new JSONObject().put("result", 6);
		JSONObject actual = process.request("multiplication",
				new JSONObject().put("values", new JSONArray().put(3).put(2)));
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void test_division() throws IOException {
		JSONObject expected = new JSONObject().put("result", 1.5);
		JSONObject actual = process.request("division", new JSONObject().put("values", new JSONArray().put(3).put(2)));
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void test_broadcast() throws IOException {
		Future<Void> result = JCoprocessManager.broadcast("division",
				new JSONObject().put("values", new JSONArray().put(3).put(2)));
		assertDoesNotThrow(() -> result.get());
	}

	@AfterAll
	static void tearDown() {
		JCoprocessManager.kill(process);
	}
}
