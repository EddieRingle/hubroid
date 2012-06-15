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
import net.idlesoft.android.apps.github.utils.DataTask;

public
class DataFragment extends BaseFragment
{
	private
	Handler mHandler = new Handler();

	private
	DataTask.Executor mThreadPool = new DataTask.Executor();

	private
	boolean mRecreated;

	private
	UIFragment<? extends DataFragment> mUIFragment;

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
	void onPause()
	{
		super.onPause();

		mRecreated = true;
		mUIFragment = null;
	}

	public
	boolean isRecreated()
	{
		return mRecreated;
	}

	public
	UIFragment<? extends DataFragment> getUIFragment()
	{
		return mUIFragment;
	}

	public
	void setUIFragment(UIFragment<? extends DataFragment> fragment)
	{
		mUIFragment = fragment;
	}

	public
	Handler getHandler()
	{
		return mHandler;
	}

	public
	DataTask.Executor getThreadPool()
	{
		return mThreadPool;
	}

	public
	DataTask executeNewTask(DataTask.Executable runnable)
	{
		final DataTask task = new DataTask(runnable, getHandler());
		getThreadPool().execute(task);
		return task;
	}
}