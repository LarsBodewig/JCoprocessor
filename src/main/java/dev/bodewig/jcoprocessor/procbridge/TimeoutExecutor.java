package dev.bodewig.jcoprocessor.procbridge;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public final class TimeoutExecutor implements Executor {

	protected final Executor base;
	protected final long timeout;

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

	public Executor getBaseExecutor() {
		return base;
	}

	public long getTimeout() {
		return timeout;
	}

}
