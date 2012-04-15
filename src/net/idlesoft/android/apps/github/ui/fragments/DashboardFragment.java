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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import net.idlesoft.android.apps.github.R;

import static android.view.View.OnClickListener;

public
class DashboardFragment extends BaseFragment
{
	private int mActiveButton = 0;

	private static int instanceCount = 0;

	public static final int ACTIVE_BUTTON_EVENTS = 0x0001;

	public static final int ACTIVE_BUTTON_REPOS = 0x0002;

	public static final int ACTIVE_BUTTON_PROFILE = 0x0003;

	public static final int ACTIVE_BUTTON_USERS = 0x0004;

	public static final int ACTIVE_BUTTON_GISTS = 0x0005;

	public static final int ACTIVE_BUTTON_ORGS = 0x0006;

	public
	void setActiveButton(int pButtonId)
	{
		final View v = getView();

		if (pButtonId != mActiveButton) {
			switch (pButtonId) {
			case R.id.btn_dash_events:
				mActiveButton = ACTIVE_BUTTON_EVENTS;
				break;
			case R.id.btn_dash_gists:
				mActiveButton = ACTIVE_BUTTON_GISTS;
				break;
			case R.id.btn_dash_orgs:
				mActiveButton = ACTIVE_BUTTON_ORGS;
				break;
			case R.id.btn_dash_profile:
				mActiveButton = ACTIVE_BUTTON_PROFILE;
				break;
			case R.id.btn_dash_repos:
				mActiveButton = ACTIVE_BUTTON_REPOS;
				break;
			case R.id.btn_dash_users:
				mActiveButton = ACTIVE_BUTTON_USERS;
				break;
			}
		}

		/* Don't do anything else if we're not multi-paned */
		if (!isMultiPane()) return;

		/* First, reset all the buttons to be un-highlighted */
		((Button) v.findViewById(R.id.btn_dash_events))
				.setBackgroundResource(R.color.dashboard_button_normal);
		((Button) v.findViewById(R.id.btn_dash_repos))
				.setBackgroundResource(R.color.dashboard_button_normal);
		((Button) v.findViewById(R.id.btn_dash_profile))
				.setBackgroundResource(R.color.dashboard_button_normal);
		((Button) v.findViewById(R.id.btn_dash_users))
				.setBackgroundResource(R.color.dashboard_button_normal);
		((Button) v.findViewById(R.id.btn_dash_gists))
				.setBackgroundResource(R.color.dashboard_button_normal);
		((Button) v.findViewById(R.id.btn_dash_orgs))
				.setBackgroundResource(R.color.dashboard_button_normal);

		/* Now highlight the specified button */
		if (mActiveButton != 0)
			((Button) v.findViewById(pButtonId))
					.setBackgroundResource(R.color.dashboard_button_highlighted);
	}

	protected
	void showFragments()
	{
		getBaseActivity().startFragmentTransaction();
		switch (mActiveButton) {
		case ACTIVE_BUTTON_EVENTS:
			getBaseActivity().addFragmentToTransaction(EventsFragment.class,
													   R.id.fragment_container, null);
			break;
		case ACTIVE_BUTTON_REPOS:
			getBaseActivity().addFragmentToTransaction(RepositoriesFragment.class,
													   R.id.fragment_container, null);
			break;
		case ACTIVE_BUTTON_PROFILE:
			getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
													   R.id.fragment_container, null);
			break;
		}
		getBaseActivity().finishFragmentTransaction(!isMultiPane());
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.dashboard_fragment, container, false);

		boolean isAnon = getBaseActivity().getCurrentUserLogin().equals("");

		Button feedsBtn = (Button) v.findViewById(R.id.btn_dash_events);
		feedsBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				if (mActiveButton != ACTIVE_BUTTON_EVENTS || !isMultiPane()) {
					setActiveButton(R.id.btn_dash_events);
					showFragments();
				}
			}
		});

		Button reposBtn = (Button) v.findViewById(R.id.btn_dash_repos);
		if (isAnon) {
			reposBtn.setVisibility(View.GONE);
		} else {
			reposBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public
				void onClick(View v)
				{
					if (mActiveButton != ACTIVE_BUTTON_REPOS || !isMultiPane()) {
						setActiveButton(R.id.btn_dash_repos);
						showFragments();
					}
				}
			});
		}

		Button profileBtn = (Button) v.findViewById(R.id.btn_dash_profile);
		if (isAnon) {
			profileBtn.setVisibility(View.GONE);
		} else {
			profileBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public
				void onClick(View v)
				{
					if (mActiveButton != ACTIVE_BUTTON_PROFILE || !isMultiPane()) {
						setActiveButton(R.id.btn_dash_profile);
						showFragments();
					}
				}
			});
		}

		Button usersBtn = (Button) v.findViewById(R.id.btn_dash_users);
		if (isAnon) {
			usersBtn.setVisibility(View.GONE);
		} else {
			usersBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public
				void onClick(View v)
				{
					setActiveButton(R.id.btn_dash_users);
				}
			});
		}

		Button gistsBtn = (Button) v.findViewById(R.id.btn_dash_gists);
		gistsBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public
			void onClick(View v)
			{
				setActiveButton(R.id.btn_dash_gists);
			}
		});

		Button orgsBtn = (Button) v.findViewById(R.id.btn_dash_orgs);
		if (isAnon) {
			orgsBtn.setVisibility(View.GONE);
		} else {
			orgsBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public
				void onClick(View v)
				{
					setActiveButton(R.id.btn_dash_orgs);
				}
			});
		}

		return v;
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			mActiveButton = savedInstanceState.getInt("activeButton", 0);
		}

		if (isMultiPane()) {
			getBigFragmentContainer().setVisibility(View.INVISIBLE);
			getFragmentContainerDivider().setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public
	void onResume()
	{
		super.onResume();

		getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getBaseActivity().getSupportActionBar().setHomeButtonEnabled(false);

		switch (mActiveButton) {
			case ACTIVE_BUTTON_EVENTS:
				setActiveButton(R.id.btn_dash_events);
				break;
			case ACTIVE_BUTTON_GISTS:
				setActiveButton(R.id.btn_dash_gists);
				break;
			case ACTIVE_BUTTON_ORGS:
				setActiveButton(R.id.btn_dash_orgs);
				break;
			case ACTIVE_BUTTON_PROFILE:
				setActiveButton(R.id.btn_dash_profile);
				break;
			case ACTIVE_BUTTON_REPOS:
				setActiveButton(R.id.btn_dash_repos);
				break;
			case ACTIVE_BUTTON_USERS:
				setActiveButton(R.id.btn_dash_users);
				break;
		}

		if (isMultiPane())
			showFragments();
	}

	@Override
	public
	void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putInt("activeButton", mActiveButton);
	}
}
