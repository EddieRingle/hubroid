/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.fragments;

import android.os.Bundle;
import android.os.Handler;

import java.util.List;
import java.util.concurrent.*;

public
class DataFragment extends BaseFragment
{
	public static
	class DataTask
	{
		public static abstract
		class DataTaskRunnable implements Runnable
		{
			public DataTask task;

			@Override
			public final
			void run()
			{
				try {
					if (task.callbacks != null) {
						task.handler.post(new Runnable()
						{
							@Override
							public
							void run()
							{
								task.callbacks.onTaskStart();
							}
						});
					}

					runTask();

					if (task.callbacks != null) {
						task.handler.post(new Runnable()
						{
							@Override
							public
							void run()
							{
								task.callbacks.onTaskComplete();
							}
						});
					}
				} catch (InterruptedException e) {
					if (task.callbacks != null) {
						task.handler.post(new Runnable()
						{
							@Override
							public
							void run()
							{
								task.callbacks.onTaskCancelled();
							}
						});
					}
				}
			}

			public abstract
			void runTask() throws InterruptedException;
		}

		public static
		interface DataTaskCallbacks
		{
			public
			void onTaskStart();

			public
			void onTaskCancelled();

			public
			void onTaskComplete();
		}


		private
		DataTaskRunnable runnable;

		private
		Handler handler;

		protected
		DataTaskCallbacks callbacks;

		public
		DataTask(DataTaskRunnable runnable, Handler handler, DataTaskCallbacks callbacks)
		{
			this.runnable = runnable;
			this.handler = handler;
			this.callbacks = callbacks;
		}
	};

	public static
	class DataTaskExecutor
	{
		public
		ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

		public
		void execute(DataTask dataTask)
		{
			mThreadPool.execute(dataTask.runnable);
		}

		public
		void shutdown()
		{
			mThreadPool.shutdown();
		}

		public
		List<Runnable> shutdownNow()
		{
			return mThreadPool.shutdownNow();
		}
	}

	private
	Handler mHandler = new Handler();

	private
	DataTaskExecutor mThreadPool = new DataTaskExecutor();

	private
	boolean mRecreated;

	@Override
	public
	void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	public
	void onUIFragmentReady()
	{
	}

	@Override
	public
	void onStart()
	{
		super.onStart();

		mRecreated = false;
	}

	@Override
	public
	void onDetach()
	{
		super.onDetach();

		mRecreated = true;
	}

	public
	boolean isRecreated()
	{
		return mRecreated;
	}

	public
	UIFragment<DataFragment> getUIFragment()
	{
		return (UIFragment<DataFragment>) getTargetFragment();
	}

	public
	Handler getHandler()
	{
		return mHandler;
	}

	public
	DataTaskExecutor getThreadPool()
	{
		return mThreadPool;
	}

	public
	DataTask executeNewTask(DataTask.DataTaskRunnable runnable,
							DataTask.DataTaskCallbacks callbacks)
	{
		final DataTask task = new DataTask(runnable, getHandler(), callbacks);
		runnable.task = task;
		getThreadPool().execute(task);
		return task;
	}
}
