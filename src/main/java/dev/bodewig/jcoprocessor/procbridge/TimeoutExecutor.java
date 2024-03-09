package dev.bodewig.jcoprocessor.procbridge;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * Executor implementation that stops a task after reaching a timeout
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public final class TimeoutExecutor implements Executor {

	/**
	 * The base executor
	 */
	protected final Executor base;

	/**
	 * The timeout
	 */
	protected final long timeout;

	/**
	 * Create a new TimeoutExecutor that executes a runnable until the timout is
	 * reached
	 *
	 * @param timeout the time before the executor ends the task
	 * @param base    the base executor
	 */
	public TimeoutExecutor(long timeout, Executor base) {
		this.timeout = timeout;
		this.base = base;
	}

	@Override
	public void execute(Runnable task) throws TimeoutException {
		final Semaphore semaphore = new Semaphore(0);
		final boolean[] isTimeout = { false };

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				isTimeout[0] = true;
				semaphore.release();
			}
		}, timeout);

		Runnable runnable = () -> {
			try {
				task.run();
			} finally {
				semaphore.release();
			}
		};
		if (base != null) {
			base.execute(runnable);
		} else {
			new Thread(runnable).start();
		}

		try {
			semaphore.acquire();
			if (isTimeout[0]) {
				throw new TimeoutException();
			}
		} catch (InterruptedException ignored) {
		} finally {
			timer.cancel();
		}
	}

	/**
	 * Get the base executor
	 *
	 * @return the base executor
	 */
	public Executor getBaseExecutor() {
		return base;
	}

	/**
	 * Get the timeout
	 *
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}
}
