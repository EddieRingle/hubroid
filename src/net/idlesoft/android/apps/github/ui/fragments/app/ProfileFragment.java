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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.BaseFragment;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.utils.AsyncLoader;
import net.idlesoft.android.apps.github.utils.RequestCache;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import static net.idlesoft.android.apps.github.HubroidConstants.LOADER_PROFILE;
import static net.idlesoft.android.apps.github.utils.StringUtils.isStringEmpty;

public class ProfileFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<User> {

    private ArrayList<InfoListAdapter.InfoHolder> mHolders;

    private ProgressBar mProgress;

    private ImageView mGravatarView;

    private LinearLayout mContent;

    private IdleList<InfoListAdapter.InfoHolder> mListView;

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

        getBaseActivity().getSupportLoaderManager().initLoader(LOADER_PROFILE, null, this);
    }

    @Override
    public Loader<User> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<User>(getBaseActivity()) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                mContent.setVisibility(GONE);
                mProgress.setVisibility(VISIBLE);
            }

            @Override
            public User loadInBackground() {
                return RequestCache.getUser(getBaseActivity(),
                        getTargetUser().getLogin(),
                        isReset());
            }

            @Override
            protected void onStopLoading() {
                super.onStopLoading();

                mProgress.setVisibility(GONE);
                mContent.setVisibility(VISIBLE);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<User> userLoader, User user) {
        buildHolders(user);

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

        mProgress.setVisibility(GONE);
        mContent.setVisibility(VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<User> userLoader) {
    }

    public void buildHolders(final User user) {
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

		/* Repositories */
        holder = new InfoListAdapter.InfoHolder();
        holder.primary = "Repositories";
        holder.secondary =
                "Owns " + Integer.toString(user.getOwnedPrivateRepos() + user.getPublicRepos());
        holder.onClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString(ARG_TARGET_USER, GsonUtils.toJson(user));
            }
        };
        mHolders.add(holder);

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

		/* Activity */
        holder = new InfoListAdapter.InfoHolder();
        holder.primary = "Public Activity";
        holder.onClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString(ARG_TARGET_USER, GsonUtils.toJson(user));
            }
        };
        mHolders.add(holder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionbar_action_refresh) {
            if (getBaseActivity().getSupportLoaderManager().getLoader(LOADER_PROFILE) != null) {
                getBaseActivity().getSupportLoaderManager().getLoader(LOADER_PROFILE).reset();
                getBaseActivity().getSupportLoaderManager().getLoader(LOADER_PROFILE).forceLoad();
            } else {
                getBaseActivity().getSupportLoaderManager().initLoader(LOADER_PROFILE, null, this);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}