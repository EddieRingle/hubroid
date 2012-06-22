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
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.UserListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import net.idlesoft.android.apps.github.utils.DataTask;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;

public
class FollowersFollowingFragment
		extends UIFragment<FollowersFollowingFragment.FollowersFollowingDataFragment>
{
	private static final int LIST_FOLLOWERS = 1;
	private static final int LIST_FOLLOWING = 2;

	class ListHolder
	{
		ArrayList<User> users;
		CharSequence title;

		int type;
		PageIterator<User> request;
	}

	public static
	class FollowersFollowingDataFragment extends DataFragment
	{
		ArrayList<ListHolder> userLists;
		ListViewPager.MultiListPagerAdapter pagerAdapter;
		User targetUser;
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

	private
	ListViewPager mViewPager;
	private
	TitlePageIndicator mTitlePageIndicator;

	public
	FollowersFollowingFragment()
	{
		super(FollowersFollowingDataFragment.class);
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

	void fetchData(final boolean freshen)
	{
		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's followers */
			final IdleList<User> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_FOLLOWERS);

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
				holder.type = LIST_FOLLOWERS;
				holder.title = getString(R.string.followers);
				list.setTitle(holder.title);
				holder.users = new ArrayList<User>();
				if (index < 0)
					mDataFragment.userLists.add(holder);

				final DataTask.Executable followersExecutable =
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
									final UserService us =
											new UserService(getBaseActivity().getGHClient());
									holder.request = us.pageFollowers(
											mDataFragment.targetUser.getLogin());
									holder.users.addAll(holder.request.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(followersExecutable);
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

		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's following */
			final IdleList<User> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_FOLLOWING);

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
				holder.type = LIST_FOLLOWING;
				holder.title = getString(R.string.following);
				list.setTitle(holder.title);
				holder.users = new ArrayList<User>();
				if (index < 0)
					mDataFragment.userLists.add(holder);

				final DataTask.Executable followingExecutable =
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
									final UserService us =
											new UserService(getBaseActivity().getGHClient());
									holder.request = us.pageFollowing(
											mDataFragment.targetUser.getLogin());
									holder.users.addAll(holder.request.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(followingExecutable);
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

		if (mDataFragment.userLists == null || refresh)
			mDataFragment.userLists = new ArrayList<ListHolder>();

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
	void onCreateActionBar(ActionBar bar)
	{
		super.onCreateActionBar(bar);

		bar.setTitle(mDataFragment.targetUser.getLogin());
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
