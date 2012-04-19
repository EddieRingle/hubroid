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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.TitlePageIndicator;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.EventListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.IssuesListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		Repository targetRepository;
		int currentItem;
		int currentItemScroll;
		int currentItemScrollTop;

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
	int mCurrentPage;

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
			final IdleList<Issue> list = new IdleList<Issue>(getContext());
			final ListHolder holder;

			/*list.setOnItemClickListener(new OnEventListItemClickListener());*/
			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_OPEN);

			if (index >= 0) {
				holder = mDataFragment.issuesLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_OPEN;
				holder.title = getString(R.string.issues_open);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();

				mDataFragment.issuesLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable openRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
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

				final DataFragment.DataTask.DataTaskCallbacks openCallbacks =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(openRunnable, openCallbacks);
			}

			mViewPager.getAdapter().addList(list);
		}

		{
			/* Display closed issues */
			final IdleList<Issue> list = new IdleList<Issue>(getContext());
			final ListHolder holder;

			/*list.setOnItemClickListener(new OnEventListItemClickListener());*/
			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_CLOSED);

			if (index >= 0) {
				holder = mDataFragment.issuesLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_CLOSED;
				holder.title = getString(R.string.issues_closed);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();

				mDataFragment.issuesLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable closedRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
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

				final DataFragment.DataTask.DataTaskCallbacks closedCallbacks =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(closedRunnable, closedCallbacks);
			}

			mViewPager.getAdapter().addList(list);
		}

		if (!getBaseActivity().getCurrentUserLogin().equals("")) {
			/* Display issues assigned to current user */
			final IdleList<Issue> list = new IdleList<Issue>(getContext());
			final ListHolder holder;

			/*list.setOnItemClickListener(new OnEventListItemClickListener());*/
			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_ASSIGNED);

			if (index >= 0) {
				holder = mDataFragment.issuesLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_ASSIGNED;
				holder.title = getString(R.string.issues_assigned);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();

				mDataFragment.issuesLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable assignedRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
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

				final DataFragment.DataTask.DataTaskCallbacks assignedCallbacks =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(assignedRunnable, assignedCallbacks);
			}

			mViewPager.getAdapter().addList(list);
		}

		if (!getBaseActivity().getCurrentUserLogin().equals("")) {
			/* Display issues mentioning the current user */
			final IdleList<Issue> list = new IdleList<Issue>(getContext());
			final ListHolder holder;

			/*list.setOnItemClickListener(new OnEventListItemClickListener());*/
			list.setAdapter(new IssuesListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_MENTIONED);

			if (index >= 0) {
				holder = mDataFragment.issuesLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.issues);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_MENTIONED;
				holder.title = getString(R.string.issues_mentioned);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.issues = new ArrayList<Issue>();

				mDataFragment.issuesLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable mentionedRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
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

				final DataFragment.DataTask.DataTaskCallbacks mentionedCallbacks =
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
								list.getListAdapter().fillWithItems(holder.issues);
								list.getListAdapter().notifyDataSetChanged();
								list.getProgressBar().setVisibility(View.GONE);
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(mentionedRunnable, mentionedCallbacks);
			}

			mViewPager.getAdapter().addList(list);
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
			repositoryJson = args.getString(ARG_TARGET_REPO, null);
			if (repositoryJson != null) {
				mDataFragment.targetRepository = GsonUtils.fromJson(repositoryJson, Repository.class);
			}
		}

		if (mDataFragment.issuesLists == null)
			mDataFragment.issuesLists = new ArrayList<ListHolder>();

		ListViewPager.MultiListPagerAdapter adapter =
				new ListViewPager.MultiListPagerAdapter(getContext());

		mViewPager.setAdapter(adapter);
		mTitlePageIndicator.setViewPager(mViewPager);

		fetchData(false);
	}

	@Override
	public
	void onPause()
	{
		super.onPause();

		mDataFragment.currentItem = mViewPager.getCurrentItem();

		mDataFragment.currentItemScroll = mViewPager.getAdapter().getList(mDataFragment.currentItem)
													.getFirstVisiblePosition();
		mDataFragment.currentItemScrollTop = mViewPager.getAdapter()
													   .getList(mDataFragment.currentItem)
													   .getChildAt(0).getTop();
	}

	@Override
	public
	void onResume()
	{
		super.onResume();

		mViewPager.setCurrentItem(mDataFragment.currentItem);
		mViewPager.getAdapter().getList(mDataFragment.currentItem)
				  .setSelectionFromTop(mDataFragment.currentItemScroll,
									   mDataFragment.currentItemScrollTop);
	}
}
