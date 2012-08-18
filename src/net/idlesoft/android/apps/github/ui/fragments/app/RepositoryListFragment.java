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

import android.accounts.AccountsException;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import net.idlesoft.android.apps.github.ui.adapters.BaseListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.RepositoryListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.PagedListFragment;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.WatcherService;

import java.io.IOException;
import java.util.*;

import static net.idlesoft.android.apps.github.HubroidConstants.*;

public
class RepositoryListFragment extends PagedListFragment<Repository>
{
	public static final int LIST_USER = 1;
	public static final int LIST_WATCHED = 2;

	public static final String ARG_LIST_TYPE = "list_type";

	private
	int mListType;

	@Override
	public
	PageIterator<Repository> onCreatePageIterator()
	{
		switch (mListType) {
		case LIST_USER:
			try {
				final RepositoryService rs =
						new RepositoryService(getBaseActivity().getGHClient());
				if (!getTargetUser().getLogin().equals(
						getBaseActivity().getCurrentContextLogin())) {
					return rs.pageRepositories(getTargetUser().getLogin());
				} else {
					final Map<String, String> filter = new HashMap<String, String>();
					if (getTargetUser().getLogin().equals(
							getBaseActivity().getCurrentUserLogin())) {
						filter.put("type", "owner");
						filter.put("sort", "pushed");
						return rs.pageRepositories(filter, REQUEST_PAGE_SIZE);
					} else {
						return rs.pageOrgRepositories(getTargetUser().getLogin(),
													  filter,
													  REQUEST_PAGE_SIZE);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AccountsException e) {
				e.printStackTrace();
			}
			break;
		case LIST_WATCHED:
			try {
				final WatcherService ws =
						new WatcherService(getBaseActivity().getGHClient());
				return ws.pageWatched(getTargetUser().getLogin(), REQUEST_PAGE_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AccountsException e) {
				e.printStackTrace();
			}
			break;
		}

		return null;
	}

    @Override
    public int getLoaderId() {
        switch (mListType) {
            case LIST_USER:
                return LOADER_OWNED_REPOSITORIES_PAGER;
            case LIST_WATCHED:
                return LOADER_WATCHED_REPOSITORIES_PAGER;
        }

        return 0;
    }

    @Override
    public Collection<Repository> onProcessItems(Collection<Repository> items) {
        /*
         * In the case of a list of repositories a user is watching, we want to strip out
         * all repositories belonging to the current context.
         */
        switch (mListType) {
            case LIST_WATCHED:
                final ArrayList<Repository> list = new ArrayList<Repository>();
                list.addAll(items);
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    try {
                        final Repository repo = list.get(i);
                        if (repo == null || repo.getOwner() == null) {
                            list.remove(i);
                            len--;
                            i--;
                        }
                        if (repo.getOwner().getLogin().equalsIgnoreCase(
                                getBaseActivity().getCurrentContextLogin())) {
                            list.remove(i);
                            len--;
                            i--;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                list.trimToSize();
                return list;
        }

        return items;
    }

    @Override
	public
	BaseListAdapter<Repository> onCreateListAdapter()
	{
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
	public
	void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		/*
		 * Send the user off to the Repository activity
		 */
		final Repository target = getListAdapter().getWrappedAdapter().getItem(position);
		final Bundle args = new Bundle();
		args.putString(ARG_TARGET_REPO, GsonUtils.toJson(target));
		/* TODO: Send the user off to the Repository activity */
	}
}