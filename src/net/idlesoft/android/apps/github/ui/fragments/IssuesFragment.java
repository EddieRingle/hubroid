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
import android.graphics.Bitmap;
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
import net.idlesoft.android.apps.github.ui.adapters.IssuesListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import net.idlesoft.android.apps.github.utils.DataTask;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_ISSUE;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;

public
class IssuesFragment extends UIFragment<IssuesFragment.IssuesDataFragment>
{
	public static final int LIST_OPEN = 1;
	public static final int LIST_CLOSED = 2;
	public static final int LIST_ASSIGNED = 4;
	public static final int LIST_MENTIONED = 8;

	protected
	class ListHolder
	{
		ArrayList<Issue> issues;
		CharSequence title;
		ArrayList<Bitmap> gravatars;

		int type;
		PageIterator<Issue> request;
	}

	public static
	class IssuesDataFragment extends DataFragment
	{
		ArrayList<ListHolder> issuesLists;
		ListViewPager.MultiListPagerAdapter pagerAdapter;
		Repository targetRepository;
		int currentItem;

		public
		int findListIndexByType(int listType)
		{
			if (issuesLists == null) return -1;

			for (ListHolder holder : issuesLists) {
				if (holder.type == listType)
					return issuesLists.indexOf(holder);
			}

			return -1;
		}
	}

	ListViewPager mViewPager;
	TitlePageIndicator mTitlePageIndicator;

	public
	IssuesFragment()
	{
		super(IssuesDataFragment.class);
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
		{
			/* Display open issues */
			final IdleList<Issue> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_OPEN);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Issue>(getContext());

			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.issuesLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.issuesLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_OPEN;
				holder.title = getString(R.string.issues_open);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();
				if (index < 0)
					mDataFragment.issuesLists.add(holder);

				final DataTask.Executable openExecutable =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}

							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final Repository target = mDataFragment.targetRepository;
									final IssueService is =
											new IssueService(getBaseActivity().getGHClient());
									PageIterator<Issue> itr =
											is.pageIssues(target.getOwner().getLogin(),
														  target.getName());
									holder.issues.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(openExecutable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					if (getBaseActivity() == null) return;
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_ISSUE, GsonUtils.toJson(holder.issues.get(position)));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(IssueFragment.class,
															   R.id.fragment_container,
															   args);
					getBaseActivity().finishFragmentTransaction(true);
				}
			});
		}

		{
			/* Display closed issues */
			final IdleList<Issue> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_CLOSED);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Issue>(getContext());

			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.issuesLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.issuesLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_CLOSED;
				holder.title = getString(R.string.issues_closed);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();
				if (index < 0)
					mDataFragment.issuesLists.add(holder);

				final DataTask.Executable closedExecutable =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}

							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final Repository target = mDataFragment.targetRepository;
									final IssueService is =
											new IssueService(getBaseActivity().getGHClient());
									Map<String, String> filter = new HashMap<String, String>();
									filter.put("state", "closed");
									PageIterator<Issue> itr =
											is.pageIssues(target.getOwner().getLogin(),
														  target.getName(),
														  filter);
									holder.issues.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(closedExecutable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_ISSUE, GsonUtils.toJson(holder.issues.get(position)));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(IssueFragment.class,
															   R.id.fragment_container,
															   args);
					getBaseActivity().finishFragmentTransaction(true);
				}
			});
		}

		if (!getBaseActivity().getCurrentUserLogin().equals("")) {
			/* Display issues assigned to current user */
			final IdleList<Issue> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_ASSIGNED);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Issue>(getContext());

			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.issuesLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.issuesLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_ASSIGNED;
				holder.title = getString(R.string.issues_assigned);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();
				if (index < 0)
					mDataFragment.issuesLists.add(holder);

				final DataTask.Executable assignedExecutable =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}

							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final Repository target = mDataFragment.targetRepository;
									final IssueService is =
											new IssueService(getBaseActivity().getGHClient());
									Map<String, String> filter = new HashMap<String, String>();
									filter.put("assignee", getBaseActivity().getCurrentUserLogin());
									PageIterator<Issue> itr =
											is.pageIssues(target.getOwner().getLogin(),
														  target.getName(),
														  filter);
									holder.issues.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(assignedExecutable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_ISSUE, GsonUtils.toJson(holder.issues.get(position)));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(IssueFragment.class,
															   R.id.fragment_container, args);
					getBaseActivity().finishFragmentTransaction(true);
				}
			});
		}

		if (!getBaseActivity().getCurrentUserLogin().equals("")) {
			/* Display issues mentioning the current user */
			final IdleList<Issue> list;
			final ListHolder holder;
			final int index = mDataFragment.findListIndexByType(LIST_MENTIONED);

			if (index >= 0)
				list = mViewPager.getAdapter().getList(index);
			else
				list = new IdleList<Issue>(getContext());

			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			if (index >= 0 && !freshen) {
				holder = mDataFragment.issuesLists.get(index);
				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				if (index >= 0)
					holder = mDataFragment.issuesLists.get(index);
				else
					holder = new ListHolder();
				holder.type = LIST_MENTIONED;
				holder.title = getString(R.string.issues_mentioned);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();
				if (index < 0)
					mDataFragment.issuesLists.add(holder);

				final DataTask.Executable mentionedRunnable =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}

							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final Repository target = mDataFragment.targetRepository;
									final IssueService is =
											new IssueService(getBaseActivity().getGHClient());
									Map<String, String> filter = new HashMap<String, String>();
									filter.put("mentioned", getBaseActivity().getCurrentUserLogin());
									PageIterator<Issue> itr =
											is.pageIssues(target.getOwner().getLogin(),
														  target.getName(),
														  filter);
									holder.issues.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				mDataFragment.executeNewTask(mentionedRunnable);
				if (index < 0)
					mViewPager.getAdapter().addList(list);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_ISSUE, GsonUtils.toJson(holder.issues.get(position)));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(IssueFragment.class,
															   R.id.fragment_container,
															   args);
					getBaseActivity().finishFragmentTransaction(true);
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
		final String repositoryJson;
		if (args != null) {
			repositoryJson = args.getString(ARG_TARGET_REPO);
			if (repositoryJson != null) {
				mDataFragment.targetRepository = GsonUtils.fromJson(repositoryJson, Repository.class);
			}
		}

		if (mDataFragment.issuesLists == null)
			mDataFragment.issuesLists = new ArrayList<ListHolder>();

		if (mDataFragment.pagerAdapter == null)
			mDataFragment.pagerAdapter = new ListViewPager.MultiListPagerAdapter(getContext());

		mViewPager.setAdapter(mDataFragment.pagerAdapter);
		mTitlePageIndicator.setViewPager(mViewPager);

		fetchData(getBaseActivity().getRefreshPrevious());
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
	}

	@Override
	public
	void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.actionbar_action_refresh).setVisible(true);

		menu.findItem(R.id.actionbar_action_add).setVisible(true);
		menu.findItem(R.id.actionbar_action_add).setTitle(R.string.actionbar_action_add_issue);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.actionbar_action_refresh:
			fetchData(true);
			return true;
		case R.id.actionbar_action_add:
			final Bundle args = new Bundle();
			args.putString(ARG_TARGET_REPO, GsonUtils.toJson(mDataFragment.targetRepository));

			getBaseActivity().startFragmentTransaction();
			getBaseActivity().addFragmentToTransaction(NewIssueFragment.class,
													   R.id.fragment_container,
													   args);
			getBaseActivity().finishFragmentTransaction();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
