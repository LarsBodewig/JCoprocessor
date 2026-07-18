package dev.bodewig.jcoprocessor;

import dev.bodewig.jcoprocessor.procbridge.Server;
import dev.bodewig.jcoprocessor.util.Numeric;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NumberTest {

    static class EchoServer extends Server {

        public EchoServer(int port) {
            super(port);
        }

        @Override
        public Object handleRequest(String method, Object payload) {
            return switch (method) {
                case "Double" -> Numeric.doubleValue(payload);
                case "Integer" -> Numeric.intValue(payload);
                case "Long" -> Numeric.longValue(payload);
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        }

        public static void main(String[] args) {
            if (args.length == 0) {
                throw new IllegalArgumentException("Missing argument: port");
            }
            int port = Integer.parseInt(args[0]);
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
    void test_double() {
        Double request = Double.valueOf(0.5);
        Object response = process.request("Double", request, Double.class);
        assertInstanceOf(Double.class, response);
    }

    @Test
    void test_long() {
        Long request = Long.valueOf(7);
        Object response = process.request("Long", request, Long.class);
        assertInstanceOf(Long.class, response);
    }

    @Test
    void test_integer() {
        Integer request = Integer.valueOf(3);
        Object response = process.request("Integer", request, Integer.class);
        assertInstanceOf(Integer.class, response);
    }

    @AfterAll
    static void tearDown() {
        JCoprocessManager.kill(process);
    }
}
