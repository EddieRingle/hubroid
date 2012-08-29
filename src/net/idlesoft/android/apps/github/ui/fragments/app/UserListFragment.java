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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.services.GitHubApiService;
import net.idlesoft.android.apps.github.ui.activities.app.ProfileActivity;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.ContextListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.PagedListFragment;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_USERS_LIST_FOLLOWERS;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_USERS_LIST_FOLLOWING;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_ACCOUNT;
import static net.idlesoft.android.apps.github.services.GitHubApiService.PARAM_LOGIN;

public class UserListFragment extends PagedListFragment<User> {

    public static final int LIST_FOLLOWERS = 1;

    public static final int LIST_FOLLOWING = 2;

    public static final String ARG_USER_LIST_TYPE = "list_type";

    private int mListType;

    public UserListFragment() {
        super(new TypeToken<List<User>>() {});
    }

    @Override
    public PagedListBroadcastReceiver onCreateBroadcastReceiver() {
        return new PagedListBroadcastReceiver() {
            @Override
            public List<User> handleReceive(Context context, Intent intent,
                    List<User> items) {
                return items;
            }
        };
    }

    @Override
    public IntentFilter onCreateIntentFilter() {
        final IntentFilter filter = new IntentFilter();

        /*
         * Make sure we only receive what we might actually want.
         */
        switch (mListType) {
            case LIST_FOLLOWERS:
                filter.addAction(ACTION_USERS_LIST_FOLLOWERS);
                break;
            case LIST_FOLLOWING:
                filter.addAction(ACTION_USERS_LIST_FOLLOWING);
                break;
        }

        return filter;
    }

    @Override
    public Intent onCreateServiceIntent() {
        final Intent getUsersIntent = new Intent(getBaseActivity(), GitHubApiService.class);
        getUsersIntent.putExtra(ARG_ACCOUNT, getBaseActivity().getCurrentUserAccount());

        switch (mListType) {
            case LIST_FOLLOWERS:
                getUsersIntent.setAction(ACTION_USERS_LIST_FOLLOWERS);
                getUsersIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
                break;
            case LIST_FOLLOWING:
                getUsersIntent.setAction(ACTION_USERS_LIST_FOLLOWING);
                getUsersIntent.putExtra(PARAM_LOGIN, getTargetUser().getLogin());
                break;
        }

        return getUsersIntent;
    }

    @Override
    public BaseListAdapter<User> onCreateListAdapter() {
        return new ContextListAdapter(getBaseActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mListType = args.getInt(ARG_USER_LIST_TYPE, LIST_FOLLOWERS);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        /*
         * Send the user off to the Profile activity
         */
        final User target = getListAdapter().getWrappedAdapter().getItem(position);
        final Bundle args = new Bundle();
        args.putString(ARG_TARGET_USER, GsonUtils.toJson(target));
        final Intent startProfile = new Intent(getBaseActivity(), ProfileActivity.class);
        startProfile.putExtras(args);
        getBaseActivity().startActivity(startProfile);
    }

    @Override
    public void onCreateActionBar(ActionBar bar, Menu menu, MenuInflater inflater) {
        super.onCreateActionBar(bar, menu, inflater);

        bar.setTitle(getTargetUser().getLogin());

        switch (mListType) {
            case LIST_FOLLOWERS:
                bar.setSubtitle(R.string.followers);
                break;
            case LIST_FOLLOWING:
                bar.setSubtitle(R.string.following);
                break;
        }
    }
}