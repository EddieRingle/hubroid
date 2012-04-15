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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragment;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;

public
class BaseFragment extends SherlockFragment
{
	protected
	Configuration mConfiguration;

	@Override
	public
	void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mConfiguration = getResources().getConfiguration();
	}

	@Override
	public
	void onResume()
	{
		super.onResume();

		getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getBaseActivity().getSupportActionBar().setHomeButtonEnabled(true);
	}

	public
	BaseActivity getBaseActivity()
	{
		return (BaseActivity) getSherlockActivity();
	}

	public
	Context getContext()
	{
		return getBaseActivity().getContext();
	}

	protected
	View getSidebarFragmentContainer()
	{
		return getBaseActivity().findViewById(R.id.fragment_navigation);
	}

	protected
	View getFragmentContainer()
	{
		return getBaseActivity().findViewById(R.id.fragment_container);
	}

	protected
	View getBigFragmentContainer()
	{
		return getBaseActivity().findViewById(R.id.fragment_container_more);
	}

	protected
	View getFragmentContainerDivider()
	{
		return getBaseActivity().findViewById(R.id.fragment_container_shadow);
	}

	protected
	boolean isMultiPane()
	{
		return getBaseActivity().isMultiPane();
	}
}
