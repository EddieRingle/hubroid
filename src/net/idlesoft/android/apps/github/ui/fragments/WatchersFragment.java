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

import android.accounts.AccountsException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.UserListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import net.idlesoft.android.apps.github.utils.DataTask;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.WatcherService;

import java.io.IOException;
import java.util.ArrayList;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;

public
class WatchersFragment
		extends UIFragment<WatchersFragment.WatchersDataFragment>
{
	public static final int LIST_WATCHERS = 1;

	protected
	class ListHolder
	{
		ArrayList<User> users;
		CharSequence title;

		int type;
		PageIterator<User> request;
	}

	public static
	class WatchersDataFragment extends DataFragment
	{
		ArrayList<ListHolder> userLists;
		ListViewPager.MultiListPagerAdapter pagerAdapter;
		Repository targetRepository;
		int currentItem;

		public
		int findListIndexByType(int listType)
		{
			if (userLists == null) return -1;

			for (ListHolder holder : userLists) {
				if (holder.type == listType)
					return userLists.indexOf(holder);
			}

			return -1;
		}
	}

	ListViewPager mViewPager;
	TitlePageIndicator mTitlePageIndicator;

	public
	WatchersFragment()
	{
		super(WatchersDataFragment.class);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

		View v = inflater.inflate(R.layout.viewpager_fragment, container, false);

		if (v != null) {
			mViewPager = (ListViewPager) v.findViewById(R.id.vp_pages);
			mTitlePageIndicator = (TitlePageIndicator) v.findViewById(R.id.tpi_header);
		}

		return v;
	}

	public
	void fetchData(final boolean freshen)
	{
		/* Display a repository's watchers */
		final IdleList<User> list;
		final ListHolder holder;
		final int index = mDataFragment.findListIndexByType(LIST_WATCHERS);

		if (index >= 0)
			list = mViewPager.getAdapter().getList(index);
		else
			list = new IdleList<User>(getContext());

		list.setAdapter(new UserListAdapter(getBaseActivity()));

		if (index >= 0 && !freshen) {
			holder = mDataFragment.userLists.get(index);
			list.setTitle(holder.title);
			list.getListAdapter().fillWithItems(holder.users);
			list.getListAdapter().notifyDataSetChanged();
		} else {
			if (index >= 0)
				holder = mDataFragment.userLists.get(index);
			else
				holder = new ListHolder();
			holder.type = LIST_WATCHERS;
			holder.title = getString(R.string.watchers);
			list.setTitle(holder.title);
			holder.users = new ArrayList<User>();
			if (index < 0)
				mDataFragment.userLists.add(holder);

			final DataTask.Executable watchersExecutable =
					new DataTask.Executable()
					{
						@Override
						public
						void onTaskStart()
						{
							list.getProgressBar().setVisibility(View.VISIBLE);
							list.setFooterShown(true);
							list.setListShown(true);
						}

						@Override
						public
						void onTaskCancelled()
						{
						}

						@Override
						public
						void onTaskComplete()
						{
							list.setListShown(false);
							list.getProgressBar().setVisibility(View.GONE);
							list.getListAdapter().fillWithItems(holder.users);
							list.getListAdapter().notifyDataSetChanged();
							list.setListShown(true);
						}

						@Override
						public
						void runTask() throws InterruptedException
						{
							try {
								final WatcherService ws =
										new WatcherService(getBaseActivity().getGHClient());
								holder.request =
										ws.pageWatchers(new RepositoryId(
												mDataFragment.targetRepository
															 .getOwner().getLogin(),
												mDataFragment.targetRepository.getName()));
								holder.users.addAll(holder.request.next());
							} catch (IOException e) {
								e.printStackTrace();
							} catch (AccountsException e) {
								e.printStackTrace();
							}
						}
					};

			mDataFragment.executeNewTask(watchersExecutable);
			if (index < 0)
				mViewPager.getAdapter().addList(list);
		}

		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public
			void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				final User target = holder.users.get(position);
				final Bundle args = new Bundle();
				args.putString(ARG_TARGET_USER, GsonUtils.toJson(target));
				getBaseActivity().startFragmentTransaction();
				getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
														   R.id.fragment_container,
														   args);
				getBaseActivity().finishFragmentTransaction();
			}
		});
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final Bundle args = getArguments();
		final String repositoryJson;
		if (args != null) {
			repositoryJson = args.getString(ARG_TARGET_REPO);
			if (repositoryJson != null) {
				mDataFragment.targetRepository = GsonUtils.fromJson(repositoryJson,
																	Repository.class);
			}
		}

		if (mDataFragment.userLists == null)
			mDataFragment.userLists = new ArrayList<ListHolder>();

		if (mDataFragment.pagerAdapter == null)
			mDataFragment.pagerAdapter = new ListViewPager.MultiListPagerAdapter(getContext());

		mViewPager.setAdapter(mDataFragment.pagerAdapter);
		mTitlePageIndicator.setViewPager(mViewPager);

		fetchData(false);
	}

	@Override
	public
	void onPause()
	{
		super.onPause();

		mDataFragment.currentItem = mViewPager.getCurrentItem();
	}

	@Override
	public
	void onResume()
	{
		super.onResume();

		mViewPager.setCurrentItem(mDataFragment.currentItem);
	}

	@Override
	public
	void onCreateActionBar(ActionBar bar)
	{
		super.onCreateActionBar(bar);

		bar.setTitle(R.string.watchers);
		bar.setSubtitle(mDataFragment.targetRepository.getOwner().getLogin() + "/"
				+ mDataFragment.targetRepository.getName());
	}

	@Override
	public
	void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.actionbar_action_refresh:
			fetchData(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
