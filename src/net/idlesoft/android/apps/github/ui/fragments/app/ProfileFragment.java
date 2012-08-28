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

package net.idlesoft.android.apps.github.ui.fragments.app;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.services.GitHubApiService;
import net.idlesoft.android.apps.github.ui.activities.app.RepositoriesActivity;
import net.idlesoft.android.apps.github.ui.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.BaseFragment;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_USERS_GET_USER;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_ACCOUNT;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_ERROR;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_RESULT_JSON;
import static net.idlesoft.android.apps.github.services.GitHubApiService.PARAM_LOGIN;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.ARG_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_USER;
import static net.idlesoft.android.apps.github.utils.StringUtils.isStringEmpty;

public class ProfileFragment extends BaseFragment {

    private static final String EXTRA_USER = "extra_user";

    private boolean mRefreshing;

    private ArrayList<InfoListAdapter.InfoHolder> mHolders;

    private ProgressBar mProgress;

    private ImageView mGravatarView;

    private LinearLayout mContent;

    private IdleList<InfoListAdapter.InfoHolder> mListView;

    private User mUser;

    private BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }

            if (intent.getBooleanExtra(EXTRA_ERROR, false)) {
                getBaseActivity().popShortToast("An error occurred.");
            } else {
                if (intent.getAction().equals(ACTION_USERS_GET_USER)) {
                    final String userJson = intent.getStringExtra(EXTRA_RESULT_JSON);
                    if (userJson != null) {
                        mUser = GsonUtils.fromJson(userJson, User.class);
                        buildHolders(mUser);
                        buildUI(mUser);

                        mProgress.setVisibility(GONE);
                        mContent.setVisibility(VISIBLE);
                    }
                }
            }
        }
    };

    public void buildHolders(final User user) {
        if (user == null) {
            return;
        }

        mHolders = new ArrayList<InfoListAdapter.InfoHolder>();

        InfoListAdapter.InfoHolder holder;

		/* Email */
        if (!isStringEmpty(user.getEmail())) {
            holder = new InfoListAdapter.InfoHolder();
            holder.primary = "Email";
            holder.secondary = user.getEmail();
            holder.onClick = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL,
                            new String[]{mHolders.get(position).secondary});
                    getBaseActivity().startActivity(
                            Intent.createChooser(intent, "Send mail..."));
                }
            };
            mHolders.add(holder);
        }

		/* Blog/Website */
        if (!isStringEmpty(user.getBlog())) {
            holder = new InfoListAdapter.InfoHolder();
            holder.primary = "Blog";
            holder.secondary = user.getBlog();
            holder.onClick = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(mHolders.get(position).secondary));
                    getBaseActivity().startActivity(intent);
                }
            };
            mHolders.add(holder);
        }

		/* Company */
        if (!isStringEmpty(user.getCompany())) {
            holder = new InfoListAdapter.InfoHolder();
            holder.primary = "Company";
            holder.secondary = user.getCompany();
            mHolders.add(holder);
        }

		/* Followers/Following */
        holder = new InfoListAdapter.InfoHolder();
        holder.primary = "Followers/Following";
        holder.secondary = Integer.toString(user.getFollowing());
        holder.onClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString(ARG_TARGET_USER, GsonUtils.toJson(user));
            }
        };
        mHolders.add(holder);
    }

    public void buildUI(final User user) {
        if (user == null) {
            return;
        }

        mListView.setAdapter(new InfoListAdapter(getBaseActivity()));
        mListView.getListAdapter().fillWithItems(mHolders);
        mListView.getListAdapter().notifyDataSetChanged();

        if (user != null) {
            final AQuery aq = new AQuery(getBaseActivity());
            aq.id(mGravatarView).image(user.getAvatarUrl(), true, true, 200, R.drawable.gravatar,
                    null, AQuery.FADE_IN_NETWORK, 1.0f);

            final TextView tvLogin = (TextView) mContent.findViewById(R.id.tv_user_login);
            tvLogin.setText(user.getLogin());

            final TextView tvFullName = (TextView) mContent.findViewById(R.id.tv_user_fullname);
            if (!isStringEmpty(user.getName())) {
                tvFullName.setText(user.getName());
            } else {
                tvFullName.setVisibility(GONE);
            }
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final InfoListAdapter.InfoHolder holder;
                try {
                    holder = mHolders.get(position);
                    if (holder.onClick != null) {
                        holder.onClick.onItemClick(parent, view, position, id);
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                final InfoListAdapter.InfoHolder holder;
                try {
                    holder = mHolders.get(position);
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

    public void startLoading(final boolean forceRefresh) {
        final Intent getProfileIntent = new Intent(getBaseActivity(), GitHubApiService.class);
        getProfileIntent.setAction(ACTION_USERS_GET_USER);
        getProfileIntent.putExtra(ARG_ACCOUNT, getBaseActivity().getCurrentUserAccount());
        getProfileIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
        getBaseActivity().startService(getProfileIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }

        View v = inflater.inflate(R.layout.profile, container, false);

        if (v != null) {
            mProgress = (ProgressBar) v.findViewById(R.id.progress);
            mContent = (LinearLayout) v.findViewById(R.id.content);
            mListView = (IdleList<InfoListAdapter.InfoHolder>) v.findViewById(R.id.lv_user_info);
            mGravatarView = (ImageView) v.findViewById(R.id.gravatar);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final IntentFilter profileFilter = new IntentFilter(ACTION_USERS_GET_USER);
        getBaseActivity().registerReceiver(mProfileReceiver, profileFilter);

        if (savedInstanceState == null) {
            mContent.setVisibility(GONE);
            mProgress.setVisibility(VISIBLE);
            startLoading(false);
        } else {
            if (savedInstanceState.containsKey(EXTRA_USER)) {
                mUser = GsonUtils.fromJson(savedInstanceState.getString(EXTRA_USER), User.class);
                buildHolders(mUser);
                buildUI(mUser);
                mProgress.setVisibility(GONE);
                mContent.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mProfileReceiver != null) {
            try {
                getBaseActivity().unregisterReceiver(mProfileReceiver);
            } catch (IllegalArgumentException e) {
                /* Ignore it. */
            }
        }
    }

    @Override
    public void onCreateActionBar(ActionBar bar, Menu menu, MenuInflater inflater) {
        super.onCreateActionBar(bar, menu, inflater);

        bar.setTitle(getTargetUser().getLogin());
        bar.setSubtitle(null);

        menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionbar_action_refresh) {
            mContent.setVisibility(GONE);
            mProgress.setVisibility(VISIBLE);
            mRefreshing = true;

            startLoading(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUser != null) {
            outState.putString(EXTRA_USER, GsonUtils.toJson(mUser));
        }
    }
}