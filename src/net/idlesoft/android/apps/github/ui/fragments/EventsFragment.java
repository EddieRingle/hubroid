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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.viewpagerindicator.TitlePageIndicator;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.EventListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.ListViewPager;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

import java.io.IOException;
import java.util.ArrayList;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;

public
class EventsFragment extends UIFragment<EventsFragment.EventsDataFragment>
{
	public static final int LIST_RECEIVED = 1;
	public static final int LIST_PUBLIC = 2;
	public static final int LIST_TIMELINE = 4;

	protected
	class ListHolder
	{
		ArrayList<Event> events;
		CharSequence title;
		ArrayList<Bitmap> gravatars;

		int type;
		PageIterator<Event> request;
	}

	public static
	class EventsDataFragment extends DataFragment
	{
		ArrayList<ListHolder> eventLists;
		User targetUser;
		int currentItem;
		int currentItemScroll;

		public
		int findListIndexByType(int listType)
		{
			if (eventLists == null) return -1;

			for (ListHolder holder : eventLists) {
				if (holder.type == listType)
					return eventLists.indexOf(holder);
			}

			return -1;
		}
	}

	ListViewPager mViewPager;
	TitlePageIndicator mTitlePageIndicator;
	int mCurrentPage;

	public
	EventsFragment()
	{
		super(EventsDataFragment.class);
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
			userJson = args.getString(ARG_TARGET_USER, null);
			if (userJson != null) {
				mDataFragment.targetUser = GsonUtils.fromJson(userJson, User.class);
			}
		}
		if (mDataFragment.targetUser == null) {
			mDataFragment.targetUser = new User();
			mDataFragment.targetUser.setLogin(getBaseActivity().getCurrentUserLogin());
		}

		if (mDataFragment.eventLists == null)
			mDataFragment.eventLists = new ArrayList<ListHolder>();

		ListViewPager.MultiListPagerAdapter adapter =
				new ListViewPager.MultiListPagerAdapter(getContext());

		if (mDataFragment.targetUser.getLogin().equals(getBaseActivity().getCurrentUserLogin())
				&& !mDataFragment.targetUser.getLogin().equals("")) {
			/* Display received events */
			final IdleList<Event> list = new IdleList<Event>(getContext());
			final ListHolder holder;

			list.setAdapter(new EventListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_RECEIVED);

			if (index >= 0) {
				holder = mDataFragment.eventLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.events);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_RECEIVED;
				holder.title = getString(R.string.events_received);
				list.setTitle(holder.title);
				holder.gravatars = new ArrayList<Bitmap>();
				holder.events = new ArrayList<Event>();

				mDataFragment.eventLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable receivedRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
				{
					@Override
					public
					void runTask() throws InterruptedException
					{
						try {
							Log.d("hubroid", "RECEIVED TASK");
							final EventService es =
									new EventService(getBaseActivity().getGHClient());
							PageIterator<Event> itr =
									es.pageUserReceivedEvents(mDataFragment.targetUser.getLogin());
							holder.events.addAll(itr.next());
						} catch (IOException e) {
							e.printStackTrace();
						} catch (AccountsException e) {
							e.printStackTrace();
						}
					}
				};

				final DataFragment.DataTask.DataTaskCallbacks receivedCallbacks =
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
						list.getListAdapter().fillWithItems(holder.events);
						list.getListAdapter().notifyDataSetChanged();
						list.getProgressBar().setVisibility(View.GONE);
						list.setListShown(true);
					}
				};

				mDataFragment.executeNewTask(receivedRunnable, receivedCallbacks);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Event e = holder.events.get(position);
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_USER, GsonUtils.toJson(e.getActor()));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
															   R.id.fragment_container_more,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			});

			adapter.addList(list);
		}

		if (!mDataFragment.targetUser.getLogin().equals("")) {
			/* Display a user's public events */
			final IdleList<Event> list = new IdleList<Event>(getContext());
			final ListHolder holder;

			list.setAdapter(new EventListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_PUBLIC);

			if (index >= 0) {
				holder = mDataFragment.eventLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.events);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_PUBLIC;
				holder.title = getString(R.string.events_public);
				list.setTitle(holder.title);
				holder.events = new ArrayList<Event>();

				mDataFragment.eventLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable publicRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
							@Override
							public
							void runTask() throws InterruptedException
							{
								Log.d("hubroid", "PUBLIC TASKS");
								try {
									final EventService es =
											new EventService(getBaseActivity().getGHClient());
									PageIterator<Event> itr =
											es.pageUserEvents(mDataFragment.targetUser.getLogin());
									holder.events.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				final DataFragment.DataTask.DataTaskCallbacks receivedCallbacks =
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
								list.getListAdapter().fillWithItems(holder.events);
								list.getListAdapter().notifyDataSetChanged();
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(publicRunnable, receivedCallbacks);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Event e = holder.events.get(position);
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_USER, GsonUtils.toJson(e.getActor()));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
															   R.id.fragment_container_more,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			});

			adapter.addList(list);
		}

		{ /* This bit causes some weird issue with egit-github at the moment, disabling */
			/* Display timeline events */
			final IdleList<Event> list = new IdleList<Event>(getContext());
			final ListHolder holder;

			list.setAdapter(new EventListAdapter(getBaseActivity()));

			final int index = mDataFragment.findListIndexByType(LIST_TIMELINE);

			if (index >= 0) {
				holder = mDataFragment.eventLists.get(index);

				list.setTitle(holder.title);
				list.getListAdapter().fillWithItems(holder.events);
				list.getListAdapter().notifyDataSetChanged();
			} else {
				holder = new ListHolder();
				holder.type = LIST_TIMELINE;
				holder.title = getString(R.string.events_timeline);
				list.setTitle(holder.title);
				holder.events = new ArrayList<Event>();

				mDataFragment.eventLists.add(holder);

				final DataFragment.DataTask.DataTaskRunnable publicRunnable =
						new DataFragment.DataTask.DataTaskRunnable()
						{
							@Override
							public
							void runTask() throws InterruptedException
							{
								try {
									final EventService es =
											new EventService(getBaseActivity().getGHClient());
									PageIterator<Event> itr =
											es.pagePublicEvents(30);
									holder.events.addAll(itr.next());
								} catch (IOException e) {
									e.printStackTrace();
								} catch (AccountsException e) {
									e.printStackTrace();
								}
							}
						};

				final DataFragment.DataTask.DataTaskCallbacks receivedCallbacks =
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
								list.getListAdapter().fillWithItems(holder.events);
								list.getListAdapter().notifyDataSetChanged();
								list.setListShown(true);
							}
						};

				mDataFragment.executeNewTask(publicRunnable, receivedCallbacks);
			}

			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Event e = holder.events.get(position);
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_USER, GsonUtils.toJson(e.getActor()));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
															   R.id.fragment_container_more,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			});

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
