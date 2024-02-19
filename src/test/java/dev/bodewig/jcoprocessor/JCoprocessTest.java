package dev.bodewig.jcoprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import dev.bodewig.jcoprocessor.procbridge.Server;

@TestInstance(Lifecycle.PER_CLASS)
class JCoprocessTest {

	static class TestServer extends Server {

		public TestServer(int port) {
			super(port);
		}

		@Override
		public JSONObject handleRequest(String method, JSONObject payload) {
			return new JSONObject().put("method", method).put("payload", payload);
		}

		public static void main(String[] args) {
			if (args.length <= 0) {
				throw new IllegalArgumentException("Missing argument: port");
			}
			int port = Integer.valueOf(args[0]);
			TestServer server = new TestServer(port);
			server.start();
		}
	}

	JCoprocess process;

	@BeforeAll
	void setup() throws IOException {
		JCoprocessManager.terminateOnShutdown();
		process = JCoprocessManager.spawn(TestServer.class);
	}

	@Test
	void test() throws IOException {
		JSONObject expected = new JSONObject().put("method", "testMethod").put("payload",
				new JSONObject().put("testPayloadKey", 2));
		JSONObject actual = process.request("testMethod", new JSONObject().put("testPayloadKey", 2));
		assertEquals(expected, actual);
	}

	@AfterAll
	void tearDown() {
		JCoprocessManager.kill(process);
	}
}
