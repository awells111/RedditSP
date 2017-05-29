package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.util.ArrayList;

public class PrioritisedCachedThreadPool {

	private final ArrayList<Task> mTasks = new ArrayList<>(16);
	private final Executor mExecutor = new Executor();

	private final int mMaxThreads;
	private final String mThreadName;
	private int mThreadNameCount = 0;

	private int mRunningThreads, mIdleThreads;

	public PrioritisedCachedThreadPool(int threads, String threadName) {
		mMaxThreads = threads;
		mThreadName = threadName;
	}

	public void add(Task task) {

		synchronized(mTasks) {
			mTasks.add(task);
			mTasks.notifyAll();

			if(mIdleThreads < 1 && mRunningThreads < mMaxThreads) {
				mRunningThreads++;
				new Thread(mExecutor, mThreadName + " " + (mThreadNameCount++)).start();
			}
		}
	}

	public static abstract class Task {

		public boolean isHigherPriorityThan(Task o) {
			return getPrimaryPriority() < o.getPrimaryPriority()
					|| getSecondaryPriority() < o.getSecondaryPriority();
		}

		public abstract int getPrimaryPriority();
		public abstract int getSecondaryPriority();
		public abstract void run();
	}

	private final class Executor implements Runnable {

		@Override
		public void run() {

			while(true) {

				Task taskToRun = null;

				synchronized(mTasks) {

					if(mTasks.isEmpty()) {

						mIdleThreads++;

						try {
							mTasks.wait(30000);
						} catch(InterruptedException e) {
							throw new RuntimeException(e);
						} finally {
							mIdleThreads--;
						}

						if(mTasks.isEmpty()) {
							mRunningThreads--;
							return;
						}
					}

					int taskIndex = -1;
					for(int i = 0; i < mTasks.size(); i++) {
						if(taskToRun == null || mTasks.get(i).isHigherPriorityThan(taskToRun)) {
							taskToRun = mTasks.get(i);
							taskIndex = i;
						}
					}

					mTasks.remove(taskIndex);
				}

				assert taskToRun != null;
				taskToRun.run();
			}
		}
	}
}
