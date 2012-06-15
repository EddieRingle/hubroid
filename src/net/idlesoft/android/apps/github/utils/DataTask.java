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
};