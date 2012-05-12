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
import android.widget.*;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.DashboardListAdapter;

import java.util.ArrayList;

import static android.view.View.OnClickListener;

public
class DashboardFragment extends BaseFragment
{
	private static int instanceCount = 0;

	public static final int CHOICE_EVENTS = 0x0001;

	public static final int CHOICE_REPOS = 0x0002;

	public static final int CHOICE_PROFILE = 0x0003;

	public static final int CHOICE_USERS = 0x0004;

	public static final int CHOICE_GISTS = 0x0005;

	public static final int CHOICE_ORGS = 0x0006;

	protected
	void showFragment(final int selection)
	{
		getBaseActivity().startFragmentTransaction();
		switch (selection) {
		case CHOICE_EVENTS:
			getBaseActivity().addFragmentToTransaction(EventsFragment.class,
													   R.id.fragment_container, null);
			break;
		case CHOICE_REPOS:
			getBaseActivity().addFragmentToTransaction(RepositoriesFragment.class,
													   R.id.fragment_container, null);
			break;
		case CHOICE_PROFILE:
			getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
													   R.id.fragment_container, null);
			break;
		case CHOICE_USERS:
			getBaseActivity().addFragmentToTransaction(FollowersFollowingFragment.class,
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

		if (!isMultiPane()) {
			final ListView dashboardList = (ListView) v.findViewById(R.id.lv_dashboard);
			final DashboardListAdapter adapter = new DashboardListAdapter(getBaseActivity());
			final ArrayList<DashboardListAdapter.DashboardEntry> entries = new ArrayList
					<DashboardListAdapter.DashboardEntry>();
			DashboardListAdapter.DashboardEntry entry = new DashboardListAdapter.DashboardEntry();

			/* Events */
			entry.icon = getResources().getDrawable(R.drawable.ic_dash_events);
			entry.label = getString(R.string.dash_events);
			entries.add(entry);

			if (!isAnon) {
				/* Repositories */
				entry = new DashboardListAdapter.DashboardEntry();
				entry.icon = getResources().getDrawable(R.drawable.ic_dash_repos);
				entry.label = getString(R.string.dash_repos);
				entries.add(entry);
			}

			if (!isAnon) {
				/* Profile */
				entry = new DashboardListAdapter.DashboardEntry();
				entry.icon = getResources().getDrawable(R.drawable.ic_dash_profile);
				entry.label = getString(R.string.dash_profile);
				entries.add(entry);
			}

			if (!isAnon) {
				/* Users */
				entry = new DashboardListAdapter.DashboardEntry();
				entry.icon = getResources().getDrawable(R.drawable.ic_dash_users);
				entry.label = getString(R.string.dash_users);
				entries.add(entry);
			}

			adapter.addAll(entries);

			dashboardList.setAdapter(adapter);

			dashboardList.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final DashboardListAdapter.DashboardEntry entry = entries.get(position);
					if (entry.label.equals(getString(R.string.dash_events)))
						showFragment(CHOICE_EVENTS);
					else if (entry.label.equals(getString(R.string.dash_repos)))
						showFragment(CHOICE_REPOS);
					else if (entry.label.equals(getString(R.string.dash_profile)))
						showFragment(CHOICE_PROFILE);
					else if (entry.label.equals(getString(R.string.dash_users)))
						showFragment(CHOICE_USERS);
				}
			});
		} else {

		}

		return v;
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

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

		getBaseActivity().getSupportActionBar().setTitle(R.string.app_name);
		getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getBaseActivity().getSupportActionBar().setHomeButtonEnabled(false);
	}
}
