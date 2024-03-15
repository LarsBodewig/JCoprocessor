package dev.bodewig.jcoprocessor;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.bodewig.jcoprocessor.procbridge.Server;
import dev.bodewig.jcoprocessor.util.Numeric;

class NumberTest {

	static class EchoServer extends Server {

		public EchoServer(int port) {
			super(port);
		}

		@Override
		public Object handleRequest(String method, Object payload) {
			switch (method) {
			case "Double": {
				double request = Numeric.doubleValue(payload);
				return request;
			}
			case "Integer": {
				int request = Numeric.intValue(payload);
				return request;
			}
			case "Long": {
				long request = Numeric.longValue(payload);
				return request;
			}
			default:
				throw new IllegalArgumentException("Unexpected method: " + method);
			}
		}

		public static void main(String[] args) {
			if (args.length <= 0) {
				throw new IllegalArgumentException("Missing argument: port");
			}
			int port = Integer.valueOf(args[0]);
			EchoServer server = new EchoServer(port);
			server.start();
		}
	}

	static JCoprocess process;

	@BeforeAll
	static void setup() throws IOException {
		JCoprocessManager.terminateOnShutdown();
		process = JCoprocessManager.spawn(EchoServer.class);
	}

	@Test
	void test_double() throws IOException {
		Double request = Double.valueOf(0.5);
		Object response = process.request("Double", request, Double.class);
		assertInstanceOf(Double.class, response);
	}

	@Test
	void test_long() throws IOException {
		Long request = Long.valueOf(7);
		Object response = process.request("Long", request, Long.class);
		assertInstanceOf(Long.class, response);
	}

	@Test
	void test_integer() throws IOException {
		Integer request = Integer.valueOf(3);
		Object response = process.request("Integer", request, Integer.class);
		assertInstanceOf(Integer.class, response);
	}

	@AfterAll
	static void tearDown() {
		JCoprocessManager.kill(process);
	}
}
