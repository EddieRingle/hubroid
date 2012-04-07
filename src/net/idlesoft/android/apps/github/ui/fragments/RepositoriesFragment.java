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

import android.accounts.AccountsException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.TitlePageIndicator;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.RepositoryListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.WatcherService;

import java.io.IOException;
import java.util.ArrayList;

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
		User targetUser;
		int currentItem;
		int currentItemScroll;

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
	int mCurrentPage;

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

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final Bundle args = getArguments();
		final String userJson;
		if (args != null) {
			userJson = args.getString(HubroidConstants.ARG_TARGET_USER, null);
			if (userJson != null) {
				mDataFragment.targetUser = GsonUtils.fromJson(userJson, User.class);
			}
		}
		if (mDataFragment.targetUser == null) {
			mDataFragment.targetUser = new User();
			mDataFragment.targetUser.setLogin(getBaseActivity().getCurrentUserLogin());
		}

		if (mDataFragment.repositoryLists == null)
			mDataFragment.repositoryLists = new ArrayList<ListHolder>();

		ListViewPager.MultiListPagerAdapter adapter =
				new ListViewPager.MultiListPagerAdapter(getContext());

		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's repositories */
			final IdleList<Repository> list = new IdleList<Repository>(getContext());
			final ListHolder holder;

			list.setAdapter(new RepositoryListAdapter(getContext()));

			final int index = mDataFragment.findListIndexByType(LIST_YOURS);

			if (mDataFragment.isRecreated() && index >= 0) {
				holder = mDataFragment.repositoryLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.repositories);
			} else {
				holder = new ListHolder();
				holder.type = LIST_YOURS;
				holder.title = mDataFragment.targetUser.getLogin();
				list.setTitle(holder.title);
				holder.repositories = new ArrayList<Repository>();

				mDataFragment.repositoryLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable yoursRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final RepositoryService rs =
											new RepositoryService(getBaseActivity().getGHClient());
									holder.request = rs.pageRepositories(
											mDataFragment.targetUser.getLogin());
									holder.repositories.addAll(holder.request.next());
									list.getListAdapter().fillWithItems(holder.repositories);
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				final DataFragment.DataTask.DataTaskCallbacks yoursCallbacks =
						new DataFragment.DataTask.DataTaskCallbacks()
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
								list.getListAdapter().notifyDataSetChanged();
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(yoursRunnable, yoursCallbacks);
			}

			adapter.addList(list);
		}

		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's watched repositories */
			final IdleList<Repository> list = new IdleList<Repository>(getContext());
			final ListHolder holder;

			list.setAdapter(new RepositoryListAdapter(getContext()));

			final int index = mDataFragment.findListIndexByType(LIST_WATCHED);

			if (mDataFragment.isRecreated() && index >= 0) {
				holder = mDataFragment.repositoryLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.repositories);
			} else {
				holder = new ListHolder();
				holder.type = LIST_WATCHED;
				holder.title = getString(R.string.repositories_watched);
				list.setTitle(holder.title);
				holder.repositories = new ArrayList<Repository>();

				mDataFragment.repositoryLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable watchedRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
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
									list.getListAdapter().fillWithItems(holder.repositories);
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				final DataFragment.DataTask.DataTaskCallbacks watchedCallbacks =
						new DataFragment.DataTask.DataTaskCallbacks()
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
								list.getListAdapter().notifyDataSetChanged();
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(watchedRunnable, watchedCallbacks);
			}

			adapter.addList(list);
		}

		mViewPager.setAdapter(adapter);
		mTitlePageIndicator.setViewPager(mViewPager);

		mViewPager.setCurrentItem(mDataFragment.currentItem);
	}

	@Override
	public
	void onDestroy()
	{
		super.onDestroy();

		mDataFragment.currentItem = mViewPager.getCurrentItem();
	}
}
