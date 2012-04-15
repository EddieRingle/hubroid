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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.utils.StringUtils.isStringEmpty;

public
class RepositoryFragment extends UIFragment<RepositoryFragment.RepositoryDataFragment>
{
	public static
	class RepositoryDataFragment extends DataFragment
	{
		ArrayList<InfoListAdapter.InfoHolder> holders;
		Repository targetRepo;
	}

	private
	ProgressBar mProgress;
	private
	LinearLayout mContent;
	private
	IdleList<InfoListAdapter.InfoHolder> mListView;

	public
	RepositoryFragment()
	{
		super(RepositoryDataFragment.class);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

		View v = inflater.inflate(R.layout.repository, container, false);

		if (v != null) {
			mProgress = (ProgressBar) v.findViewById(R.id.progress);
			mContent = (LinearLayout) v.findViewById(R.id.content);
			mListView =
					(IdleList<InfoListAdapter.InfoHolder>) v.findViewById(R.id.lv_repository_info);
		}

		return v;
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
				mDataFragment.targetRepo = GsonUtils.fromJson(repositoryJson, Repository.class);
			}
		}
		if (mDataFragment.targetRepo == null) {
			/* uh-oh. */
		}

		mListView.setAdapter(new InfoListAdapter(getBaseActivity()));

		if (mDataFragment.holders != null) {
			mListView.getListAdapter().fillWithItems(mDataFragment.holders);
			mListView.getListAdapter().notifyDataSetChanged();
			buildUI(mDataFragment.targetRepo);
		} else {
			final DataFragment.DataTask.DataTaskRunnable repositoryRunnable =
					new DataFragment.DataTask.DataTaskRunnable()
			{
				@Override
				public
				void runTask() throws InterruptedException
				{
					try {
						final RepositoryService rs =
								new RepositoryService(getBaseActivity().getGHClient());
						mDataFragment.targetRepo =
								rs.getRepository(mDataFragment.targetRepo.getOwner().getLogin(),
												 mDataFragment.targetRepo.getName());
						buildHolders(mDataFragment.targetRepo);
					} catch (AccountsException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			final DataFragment.DataTask.DataTaskCallbacks repositoryCallbacks =
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
					buildUI(mDataFragment.targetRepo);
					mProgress.setVisibility(View.GONE);
					mContent.setVisibility(View.VISIBLE);
				}
			};

			mDataFragment.executeNewTask(repositoryRunnable, repositoryCallbacks);
		}
	}

	public
	void buildHolders(final Repository repository)
	{
		mDataFragment.holders = new ArrayList<InfoListAdapter.InfoHolder>();

		InfoListAdapter.InfoHolder holder;

		if (!isStringEmpty(repository.getOwner().getLogin())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Owner";
			holder.secondary = repository.getOwner().getLogin();

			holder.onClick = new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final User u = mDataFragment.targetRepo.getOwner();
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_USER, GsonUtils.toJson(u));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(ProfileFragment.class,
															   R.id.fragment_container_more,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			};

			mDataFragment.holders.add(holder);
		}

		if (!isStringEmpty(repository.getDescription())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Description";
			holder.secondary = repository.getDescription();
			mDataFragment.holders.add(holder);
		}

		if (!isStringEmpty(repository.getHomepage())) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Homepage";
			holder.secondary = repository.getHomepage();

			holder.onClick = new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					String target = mDataFragment.holders.get(position).secondary;
					if (target.indexOf("://") == -1)
						target = "http://" + target;
					intent.setData(Uri.parse(target));
					getBaseActivity().startActivity(intent);
				}
			};

			mDataFragment.holders.add(holder);
		}

		if (repository.isFork() && repository.getParent() != null) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Forked From";
			holder.secondary =
					repository.getParent().getOwner().getLogin() +
					"/" +
					repository.getParent().getName();

			holder.onClick = new AdapterView.OnItemClickListener()
			{
				@Override
				public
				void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					final Repository target = repository.getParent();
					final Bundle args = new Bundle();
					args.putString(ARG_TARGET_REPO, GsonUtils.toJson(target));
					getBaseActivity().startFragmentTransaction();
					getBaseActivity().addFragmentToTransaction(RepositoryFragment.class,
															   R.id.fragment_container_more,
															   args);
					getBaseActivity().finishFragmentTransaction();
				}
			};

			mDataFragment.holders.add(holder);
		}

		if (repository.isHasIssues()) {
			holder = new InfoListAdapter.InfoHolder();
			holder.primary = "Issues";
			holder.secondary = Integer.toString(repository.getOpenIssues()) + " open issues";
			mDataFragment.holders.add(holder);
		}

		holder = new InfoListAdapter.InfoHolder();
		holder.primary = "Forks";
		holder.secondary = Integer.toString(repository.getForks());
		mDataFragment.holders.add(holder);

		holder = new InfoListAdapter.InfoHolder();
		holder.primary = "Watchers";
		holder.secondary = Integer.toString(repository.getWatchers());
		mDataFragment.holders.add(holder);
	}

	public
	void buildUI(final Repository repository)
	{
		if (repository != null) {
			mDataFragment.targetRepo = repository;

			final TextView tvName = (TextView) mContent.findViewById(R.id.tv_repository_name);
			tvName.setText(repository.getName());

			final TextView tvDescription = (TextView) mContent.findViewById(R.id.tv_repository_description);
			if (!isStringEmpty(repository.getDescription())) {
				tvDescription.setText(repository.getDescription());
			} else {
				tvDescription.setVisibility(View.GONE);
			}
		}

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public
			void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				final InfoListAdapter.InfoHolder holder = mDataFragment.holders.get(position);

				if (holder.onClick != null) {
					holder.onClick.onItemClick(parent, view, position, id);
				}
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public
			boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				final InfoListAdapter.InfoHolder holder = mDataFragment.holders.get(position);

				if (holder.onLongClick != null) {
					return holder.onLongClick.onItemLongClick(parent, view, position, id);
				}

				return false;
			}
		});
	}
}
