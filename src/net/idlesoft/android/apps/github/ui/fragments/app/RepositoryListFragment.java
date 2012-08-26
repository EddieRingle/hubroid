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

import com.google.gson.reflect.TypeToken;

import com.actionbarsherlock.app.ActionBar;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.services.GitHubApiService;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.RepositoryListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.PagedListFragment;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_REPOS_LIST_ORG_OWNED;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_REPOS_LIST_SELF_OWNED;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_REPOS_LIST_USER_OWNED;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_REPOS_LIST_USER_WATCHED;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_ACCOUNT;
import static net.idlesoft.android.apps.github.services.GitHubApiService.PARAM_LOGIN;

public class RepositoryListFragment extends PagedListFragment<Repository> {

    public static final int LIST_USER = 1;

    public static final int LIST_WATCHED = 2;

    public static final String ARG_LIST_TYPE = "list_type";

    private int mListType;

    public RepositoryListFragment() {
        super(new TypeToken<List<Repository>>() {
        });
    }

    @Override
    public PagedListBroadcastReceiver onCreateBroadcastReceiver() {
        return new PagedListBroadcastReceiver() {
            @Override
            public List<Repository> handleReceive(Context context, Intent intent,
                    List<Repository> items) {
                if (items == null) {
                    return null;
                }

                ArrayList<Repository> processed = new ArrayList<Repository>();

                if (intent.getAction().equals(ACTION_REPOS_LIST_USER_WATCHED)) {
                    /*
                     * In the case of a list of repositories a user is watching, we want to strip
                     * out all repositories belonging to the current context.
                     */
                    for (Repository repo : items) {
                        if (repo == null || repo.getOwner() == null) {
                            continue;
                        }
                        if (repo.getOwner().getLogin().equals(
                                getBaseActivity().getCurrentContextLogin())) {
                            continue;
                        }
                        processed.add(repo);
                    }
                    return processed;
                } else {
                    return items;
                }
            }
        };
    }

    @Override
    public IntentFilter onCreateIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPOS_LIST_ORG_OWNED);
        filter.addAction(ACTION_REPOS_LIST_SELF_OWNED);
        filter.addAction(ACTION_REPOS_LIST_USER_OWNED);
        filter.addAction(ACTION_REPOS_LIST_USER_WATCHED);
        return filter;
    }

    @Override
    public Intent onCreateServiceIntent() {
        final Intent getRepositoriesIntent = new Intent(getBaseActivity(), GitHubApiService.class);
        getRepositoriesIntent.putExtra(ARG_ACCOUNT, getBaseActivity().getCurrentUserAccount());

        switch (mListType) {
            case LIST_USER:
                if (!getTargetUser().getLogin().equals(
                        getBaseActivity().getCurrentContextLogin())) {
                    getRepositoriesIntent.setAction(ACTION_REPOS_LIST_USER_OWNED);
                    getRepositoriesIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
                } else {
                    final Map<String, String> filter = new HashMap<String, String>();
                    if (getTargetUser().getLogin().equals(
                            getBaseActivity().getCurrentUserLogin())) {
                        getRepositoriesIntent.setAction(ACTION_REPOS_LIST_SELF_OWNED);
                    } else {
                        getRepositoriesIntent.setAction(ACTION_REPOS_LIST_ORG_OWNED);
                        getRepositoriesIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
                    }
                }
                break;
            case LIST_WATCHED:
                getRepositoriesIntent.setAction(ACTION_REPOS_LIST_USER_WATCHED);
                getRepositoriesIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
                break;
        }

        return getRepositoriesIntent;
    }

    @Override
    public BaseListAdapter<Repository> onCreateListAdapter() {
        return new RepositoryListAdapter(getBaseActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mListType = args.getInt(ARG_LIST_TYPE, LIST_USER);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        /*
         * Send the user off to the Repository activity
         */
        final Repository target = getListAdapter().getWrappedAdapter().getItem(position);
        final Bundle args = new Bundle();
        args.putString(ARG_TARGET_REPO, GsonUtils.toJson(target));
        /* TODO: Send the user off to the Repository activity */
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        bar.setTitle(getTargetUser().getLogin());

        switch (mListType) {
            case LIST_USER:
                bar.setSubtitle(R.string.repositories);
                break;
            case LIST_WATCHED:
                bar.setSubtitle(R.string.repositories_watched);
                break;
        }
    }
}