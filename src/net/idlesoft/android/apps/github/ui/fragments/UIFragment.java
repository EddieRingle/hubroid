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

package net.idlesoft.android.apps.github.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public abstract
class UIFragment<D extends DataFragment> extends BaseFragment
{
	private
	String mDataFragmentTag;

	protected
	D mDataFragment;

	public
	UIFragment(Class<D> clazz)
	{
		mDataFragmentTag = clazz.getName();
	}

	@Override
	public
	void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (mDataFragmentTag == null) {
			throw new IllegalStateException("A Logic fragment tag must be set in the constructor.");
		}
	}

	@Override
	public
	void setRetainInstance(boolean retain)
	{
		throw new IllegalStateException("UIFragments may not be retained. Use a DataFragment instead.");
	}

	@Override
	public
	void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (!mDataFragmentTag.equals("")) {
			FragmentManager fm = getFragmentManager();
			mDataFragment = (D) fm.findFragmentByTag(mDataFragmentTag);
			if (mDataFragment == null || (mDataFragment.getArguments() != getArguments())) {
				mDataFragment = (D) D.instantiate(getContext(), mDataFragmentTag);
				mDataFragment.setArguments(getArguments());
			}
			mDataFragment.setUIFragment(this);
			getFragmentManager().beginTransaction().add(mDataFragment, mDataFragmentTag).commit();
		}

		getBaseActivity().theActionBar().setTitle("");

		mDataFragment.onUIFragmentReady();
	}
}
