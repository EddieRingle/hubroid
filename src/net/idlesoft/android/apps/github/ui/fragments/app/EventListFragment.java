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
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.EventListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.PagedListFragment;

import org.eclipse.egit.github.core.event.Event;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_EVENTS_LIST_TIMELINE;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_ACCOUNT;

public class EventListFragment extends PagedListFragment<Event> {

    public static final int LIST_USER_PRIVATE = 1;

    public static final int LIST_USER_PUBLIC = 2;

    public static final int LIST_TIMELINE = 3;

    public static final String ARG_EVENT_LIST_TYPE = "list_type";

    private int mListType;

    public EventListFragment() {
        super(new TypeToken<List<Event>>(){});
    }

    @Override
    public PagedListBroadcastReceiver onCreateBroadcastReceiver() {
        return new PagedListBroadcastReceiver() {
            @Override
            public List<Event> handleReceive(Context context, Intent intent,
                    List<Event> items) {
                return items;
            }
        };
    }

    @Override
    public IntentFilter onCreateIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EVENTS_LIST_TIMELINE);
        return filter;
    }

    @Override
    public Intent onCreateServiceIntent() {
        final Intent getEventsIntent = new Intent(getBaseActivity(), GitHubApiService.class);
        getEventsIntent.putExtra(ARG_ACCOUNT, getBaseActivity().getCurrentUserAccount());

        switch (mListType) {
            case LIST_TIMELINE:
                getEventsIntent.setAction(ACTION_EVENTS_LIST_TIMELINE);
                break;
        }

        return getEventsIntent;
    }

    @Override
    public BaseListAdapter<Event> onCreateListAdapter() {
        return new EventListAdapter(getBaseActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mListType = args.getInt(ARG_EVENT_LIST_TYPE, LIST_TIMELINE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    }

    @Override
    public void onCreateActionBar(ActionBar bar, Menu menu, MenuInflater inflater) {
        super.onCreateActionBar(bar, menu, inflater);

        bar.setTitle(getTargetUser().getLogin());

        switch (mListType) {
            case LIST_TIMELINE:
                bar.setTitle(R.string.events_timeline);
                break;
        }
    }
}