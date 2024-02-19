package dev.bodewig.jcoprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.json.JSONObject;

import dev.bodewig.jcoprocessor.procbridge.Server;

public class JCoprocessManager {

	private static final Set<JCoprocess> processes = new HashSet<>();

	public static synchronized JCoprocess spawn(Class<? extends Server> klass) throws IOException {
		JCoprocess process = new JCoprocess(klass);
		process.start();
		processes.add(process);
		return process;
	}

	public static synchronized <T> Future<Void> broadcast(String method, JSONObject payload) {
		return CompletableFuture.allOf(processes.parallelStream().map(p -> p.request(method, payload))
				.toArray(size -> new CompletableFuture[size]));
	}

	public static synchronized void kill(JCoprocess p) {
		if (!processes.remove(p)) {
			throw new IllegalArgumentException("Unknown Process");
		}
		p.stop();
	}

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

	public static void terminateOnShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread(JCoprocessManager::terminate));
	}
}
