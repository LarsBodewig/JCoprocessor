package dev.bodewig.jcoprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.bodewig.jcoprocessor.procbridge.Client;
import dev.bodewig.jcoprocessor.procbridge.Server;

/**
 * Static utility class to manage JCoprocesses
 *
 * @author Lars Bodewig
 */
public class JCoprocessManager {

	private JCoprocessManager() {
	}

	private static final Set<JCoprocess> processes = new HashSet<>();

	/**
	 * Creates a new JCoprocess
	 *
	 * @param klass         the Server class
	 * @param timeoutMillis the connection timeout, 0 for forever
	 * @return the JCoprocess instance
	 * @throws IOException if starting the JCoprocess fails
	 */
	public static synchronized JCoprocess spawn(Class<? extends Server> klass, long timeoutMillis) throws IOException {
		JCoprocess process = new JCoprocess(klass);
		process.start(timeoutMillis);
		processes.add(process);
		return process;
	}

	/**
	 * Creates a new JCoprocess
	 *
	 * @param klass the Server class
	 * @return the JCoprocess instance
	 * @throws IOException if starting the JCoprocess fails
	 */
	public static JCoprocess spawn(Class<? extends Server> klass) throws IOException {
		return spawn(klass, Client.FOREVER);
	}

	/**
	 * Sends a request over all registered processes
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, Boolean payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, Double payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, Integer payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, JSONArray payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, JSONObject payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, Long payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static Future<Void> broadcast(String method, String payload) {
		return broadcast(method, (Object) payload);
	}

	/**
	 * Sends a request over all registered JCoprocesses
	 * <p>
	 * This method should not be used directly but is visible for other packages
	 *
	 * @param method  the requested method
	 * @param payload the request payload (supports Boolean, Double, Integer,
	 *                JSONArray, JSONObject, Long, String)
	 * @return Future completing when all requests were answered
	 */
	public static synchronized Future<Void> broadcast(String method, Object payload) {
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			return CompletableFuture.allOf(processes.parallelStream()
					.map(p -> CompletableFuture.supplyAsync(() -> p.request(method, payload), executor))
					.toArray(size -> new CompletableFuture[size]));
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * Stop a specific JCoprocess created by the JCoprocessManager
	 *
	 * @param p the process
	 */
	public static synchronized void kill(JCoprocess p) {
		if (!processes.remove(p)) {
			throw new IllegalArgumentException("Unknown Process");
		}
		p.stop();
	}

	/**
	 * Stops all JCoprocesses created by the JCoprocessManager
	 */
	public static synchronized void terminate() {
		List<Exception> suppressed = new ArrayList<>();
		Iterator<JCoprocess> i = processes.iterator();
		while (i.hasNext()) {
			try {
				JCoprocess p = i.next();
				i.remove();
				p.stop();
			} catch (Exception e) {
				suppressed.add(e);
			}
		}
		if (!suppressed.isEmpty()) {
			RuntimeException e = new RuntimeException("Could not stop all processes");
			suppressed.forEach(e::addSuppressed);
			throw e;
		}
	}

	/**
	 * Registers a shutdown hook terminating all JCoprocesses created by the
	 * JCoprocessManager
	 */
	public static void terminateOnShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread(JCoprocessManager::terminate));
	}
}
