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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.RepositoryListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import net.idlesoft.android.apps.github.utils.DataTask;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.WatcherService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;

public
class RepositoriesFragment extends UIFragment<RepositoriesFragment.RepositoriesDataFragment>
{
	public static final int LIST_YOURS = 1;
	public static final int LIST_WATCHED = 2;

	protected
	class ListHolder
	{
		ArrayList<Repository> repositories;
		CharSequence title;

		int type;
		PageIterator<Repository> request;
	}

	public static
	class RepositoriesDataFragment extends DataFragment
	{
		ArrayList<ListHolder> repositoryLists;
		ListViewPager.MultiListPagerAdapter pagerAdapter;
		User targetUser;
		int currentItem;

		public
		int findListIndexByType(int listType)
		{
			if (repositoryLists == null) return -1;

			for (ListHolder holder : repositoryLists) {
				if (holder.type == listType)
					return repositoryLists.indexOf(holder);
			}

			return -1;
		}
	}

	ListViewPager mViewPager;
	TitlePageIndicator mTitlePageIndicator;

	public
	RepositoriesFragment()
	{
		super(RepositoriesDataFragment.class);
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
		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's repositories */
			final IdleList<Repository> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_YOURS);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Repository>(getContext());

			list.setAdapter(new RepositoryListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.repositoryLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.repositories);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.repositoryLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_YOURS;
				holder.title = mDataFragment.targetUser.getLogin();
				list.setTitle(holder.title);
				holder.repositories = new ArrayList<Repository>();
				if (index < 0)
					mDataFragment.repositoryLists.add(holder);

				final DataTask.Executable yoursExecutable =
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
								list.getListAdapter().fillWithItems(holder.repositories);
								list.getListAdapter().notifyDataSetChanged();
								list.setListShown(true);
							}

							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final RepositoryService rs =
											new RepositoryService(getBaseActivity().getGHClient());
									if (!mDataFragment.targetUser.getLogin().equals(
											getBaseActivity().getCurrentContextLogin())) {
										holder.request = rs.pageRepositories(
												mDataFragment.targetUser.getLogin());
									} else {
										final Map<String, String> filter = new HashMap<String, String>();
										if (mDataFragment.targetUser.getLogin().equals(
												getBaseActivity().getCurrentUserLogin())) {
											filter.put("type", "owner");
											filter.put("sort", "pushed");
											holder.request = rs.pageRepositories(filter);
										} else {
											holder.request = rs.pageOrgRepositories(mDataFragment.targetUser.getLogin(), filter);
										}
									}
									holder.repositories.addAll(holder.request.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(yoursExecutable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Repository target = holder.repositories.get(position);
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_REPO, GsonUtils.toJson(target));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(RepositoryFragment.class,
															   R.id.fragment_container,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			});
		}

		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's watched repositories */
			final IdleList<Repository> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_WATCHED);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Repository>(getContext());

			list.setAdapter(new RepositoryListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.repositoryLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.repositories);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.repositoryLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_WATCHED;
				holder.title = getString(R.string.repositories_watched);
				list.setTitle(holder.title);
				holder.repositories = new ArrayList<Repository>();
				if (index < 0)
					mDataFragment.repositoryLists.add(holder);

				final DataTask.Executable watchedExecutable =
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
								list.getListAdapter().fillWithItems(holder.repositories);
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
									holder.request = ws.pageWatched(
											mDataFragment.targetUser.getLogin());
									holder.repositories.addAll(holder.request.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(watchedExecutable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Repository target = holder.repositories.get(position);
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_REPO, GsonUtils.toJson(target));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(RepositoryFragment.class,
															   R.id.fragment_container,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			});
		}
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final Bundle args = getArguments();
		String userJson = null;
		boolean refresh = getBaseActivity().getRefreshPrevious();

		if (args != null) {
			userJson = args.getString(HubroidConstants.ARG_TARGET_USER);
			if (userJson != null) {
				mDataFragment.targetUser = GsonUtils.fromJson(userJson, User.class);
			}
		}

		if (mDataFragment.targetUser == null || userJson == null) {
			mDataFragment.targetUser = new User();
			mDataFragment.targetUser.setLogin(getBaseActivity().getCurrentContextLogin());
		}

		if (mDataFragment.repositoryLists == null || refresh)
			mDataFragment.repositoryLists = new ArrayList<ListHolder>();

		getBaseActivity().getSupportActionBar().setTitle(R.string.repositories);

		if (mDataFragment.pagerAdapter == null || refresh)
			mDataFragment.pagerAdapter = new ListViewPager.MultiListPagerAdapter(getContext());

		mViewPager.setAdapter(mDataFragment.pagerAdapter);
		mTitlePageIndicator.setViewPager(mViewPager);

		fetchData(refresh);
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
