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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.GravatarView;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.utils.RequestCache;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.utils.StringUtils.isStringEmpty;

public
class ProfileFragment extends UIFragment<ProfileFragment.ProfileDataFragment>
{
	public static
	class ProfileDataFragment extends DataFragment
	{
		ArrayList<InfoListAdapter.InfoHolder> holders;
		User targetUser;
		Bitmap gravatarBitmap;
	}

	private
	ProgressBar mProgress;
	private
	GravatarView mGravatarView;
	private
	LinearLayout mContent;
	private
	IdleList<InfoListAdapter.InfoHolder> mListView;

	public
	ProfileFragment()
	{
		super(ProfileDataFragment.class);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

		View v = inflater.inflate(R.layout.profile, container, false);

		if (v != null) {
			mProgress = (ProgressBar) v.findViewById(R.id.progress);
			mContent = (LinearLayout) v.findViewById(R.id.content);
			mListView = (IdleList<InfoListAdapter.InfoHolder>) v.findViewById(R.id.lv_user_info);
			mGravatarView = (GravatarView) v.findViewById(R.id.gravatar);
			mGravatarView.setGravatarViewCallback(new GravatarView.GravatarViewCallback()
			{
				@Override
				public
				void OnGravatarFinishedLoading(Bitmap sourceBitmap)
				{
					mDataFragment.gravatarBitmap = sourceBitmap;
					mGravatarView.getImageView().setImageBitmap(sourceBitmap);
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

		final Bundle args = getArguments();
		final String userJson;
		if (args != null) {
			userJson = args.getString(ARG_TARGET_USER);
			if (userJson != null) {
				mDataFragment.targetUser = GsonUtils.fromJson(userJson, User.class);
			}
		}
		if (mDataFragment.targetUser == null) {
			mDataFragment.targetUser = new User();
			mDataFragment.targetUser.setLogin(getBaseActivity().getCurrentUserLogin());
		}

		mListView.setAdapter(new InfoListAdapter(getBaseActivity()));

		fetchData(false);
	}

	public
	void fetchData(final boolean freshen)
	{
		if (mDataFragment.holders != null) {
			mListView.getListAdapter().fillWithItems(mDataFragment.holders);
			mListView.getListAdapter().notifyDataSetChanged();
			buildHolders(mDataFragment.targetUser);
			buildUI(mDataFragment.targetUser);
		} else {
			final DataFragment.DataTask.DataTaskRunnable profileRunnable =
					new DataFragment.DataTask.DataTaskRunnable()
					{
						@Override
						public
						void runTask() throws InterruptedException
						{
							mDataFragment.targetUser =
									RequestCache.getUser(getBaseActivity(),
														 mDataFragment.targetUser.getLogin(),
														 freshen);
							buildHolders(mDataFragment.targetUser);
						}
					};

			final DataFragment.DataTask.DataTaskCallbacks profileCallbacks =
					new DataFragment.DataTask.DataTaskCallbacks()
					{
						@Override
						public
						void onTaskStart()
						{
							mContent.setVisibility(View.GONE);
							mProgress.setVisibility(View.VISIBLE);
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
							mListView.getListAdapter().fillWithItems(mDataFragment.holders);
							mListView.getListAdapter().notifyDataSetChanged();
							buildUI(mDataFragment.targetUser);
							mProgress.setVisibility(View.GONE);
							mContent.setVisibility(View.VISIBLE);
						}
					};

			mDataFragment.executeNewTask(profileRunnable, profileCallbacks);
		}
	}

	public
	void buildHolders(final User user)
	{
		mDataFragment.holders = new ArrayList<InfoListAdapter.InfoHolder>();

		InfoListAdapter.InfoHolder holder;

		if (!isStringEmpty(user.getEmail())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Email";
			holder.secondary = user.getEmail();

			holder.onClick = new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("message/rfc822");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mDataFragment.holders.get(position).secondary});
					getBaseActivity().startActivity(
							Intent.createChooser(intent, "Send mail..."));
				}
			};

			mDataFragment.holders.add(holder);
		}

		if (!isStringEmpty(user.getBlog())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Blog";
			holder.secondary = user.getBlog();

			holder.onClick = new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(mDataFragment.holders.get(position).secondary));
					getBaseActivity().startActivity(intent);
				}
			};

			mDataFragment.holders.add(holder);
		}

		if (!isStringEmpty(user.getCompany())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Company";
			holder.secondary = user.getCompany();

			mDataFragment.holders.add(holder);
		}

		holder = new InfoListAdapter.InfoHolder();
		holder.primary = "Repositories";
		holder.secondary =
				"Owns " + Integer.toString(user.getOwnedPrivateRepos() + user.getPublicRepos());

		holder.onClick = new AdapterView.OnItemClickListener()
		{
			@Override
			public
			void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				final Bundle args = new Bundle();
				args.putString(ARG_TARGET_USER, GsonUtils.toJson(user));
				getBaseActivity().startFragmentTransaction();
				getBaseActivity().addFragmentToTransaction(RepositoriesFragment.class,
														   R.id.fragment_container_more,
														   args);
				getBaseActivity().finishFragmentTransaction();
			}
		};

		mDataFragment.holders.add(holder);

		holder = new InfoListAdapter.InfoHolder();
		holder.primary = "Followers/Following";
		holder.secondary = Integer.toString(user.getFollowing());
		holder.onClick = new AdapterView.OnItemClickListener()
		{
			@Override
			public
			void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				final Bundle args = new Bundle();
				args.putString(ARG_TARGET_USER, GsonUtils.toJson(user));
				getBaseActivity().startFragmentTransaction();
				getBaseActivity().addFragmentToTransaction(FollowersFollowingFragment.class,
														   R.id.fragment_container_more,
														   args);
				getBaseActivity().finishFragmentTransaction();
			}
		};
		mDataFragment.holders.add(holder);
	}

	public
	void buildUI(final User user)
	{
		if (user != null) {
			mDataFragment.targetUser = user;

			mGravatarView.setDefaultResource(R.drawable.gravatar);
			if (mDataFragment.gravatarBitmap != null)
				mGravatarView.getImageView().setImageBitmap(mDataFragment.gravatarBitmap);

			final TextView tvLogin = (TextView) mContent.findViewById(R.id.tv_user_login);
			tvLogin.setText(user.getLogin());

			final TextView tvFullName = (TextView) mContent.findViewById(R.id.tv_user_fullname);
			if (!isStringEmpty(user.getName())) {
				tvFullName.setText(user.getName());
			} else {
				tvFullName.setVisibility(View.GONE);
			}
		}

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public
			void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				final InfoListAdapter.InfoHolder holder;
				try {
					holder = mDataFragment.holders.get(position);
					if (holder.onClick != null) {
						holder.onClick.onItemClick(parent, view, position, id);
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public
			boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				final InfoListAdapter.InfoHolder holder;
				try {
					holder = mDataFragment.holders.get(position);
					if (holder.onLongClick != null) {
						holder.onLongClick.onItemLongClick(parent, view, position, id);
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
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
			mDataFragment.holders = null;
			fetchData(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
