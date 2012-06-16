/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.utils;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public
class DataTask
{
	public static
	class Executor
	{
		public ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

		public
		void execute(DataTask dataTask)
		{
			mThreadPool.execute(dataTask.getExecutable());
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

	public static abstract
	class Executable implements Runnable
	{
		public DataTask task;

		public
		void onTaskStart()
		{
		}

		public
		void onTaskComplete()
		{
		}

		public
		void onTaskCancelled()
		{
		}

		@Override
		public final
		void run()
		{
			try {
				task.getHandler().post(new Runnable()
				{
					@Override
					public
					void run()
					{
						Executable.this.onTaskStart();
					}
				});

				runTask();

				task.getHandler().post(new Runnable()
				{
					@Override
					public
					void run()
					{
						Executable.this.onTaskComplete();
					}
				});
			} catch (InterruptedException e) {
				task.getHandler().post(new Runnable()
				{
					@Override
					public
					void run()
					{
						Executable.this.onTaskCancelled();
					}
				});
			}
		}

		public abstract
		void runTask() throws InterruptedException;
	}


	private Executable mExecutable;

	private Handler mHandler;

	public
	DataTask(Executable executable, Handler handler)
	{
		mExecutable = executable;
		mHandler = handler;

		mExecutable.task = this;
	}

	public
	Executable getExecutable()
	{
		return mExecutable;
	}

	public
	Handler getHandler()
	{
		return mHandler;
	}
}