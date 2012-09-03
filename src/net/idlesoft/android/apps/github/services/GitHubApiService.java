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

package net.idlesoft.android.apps.github.services;

import net.idlesoft.android.apps.github.authenticator.OAuthUserProvider;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.WatcherService;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.idlesoft.android.apps.github.HubroidConstants.REQUEST_PAGE_SIZE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_API;
import static org.eclipse.egit.github.core.client.IGitHubConstants.PROTOCOL_HTTPS;

public class GitHubApiService extends IntentService {

    public static final String ACTION_GET_URI = "action_get_uri";

    public static final String ACTION_EVENTS_LIST_USER_PUBLIC = "action_events_list_user_public";

    public static final String ACTION_EVENTS_LIST_USER_RECEIVED = "action_events_list_user_received";

    public static final String ACTION_EVENTS_LIST_TIMELINE = "action_events_list_timeline";

    public static final String ACTION_ISSUES_LIST_SELF = "action_issues_list_self";

    public static final String ACTION_ORGS_LIST_MEMBERS = "action_orgs_list_members";

    public static final String ACTION_ORGS_SELF_MEMBERSHIPS = "action_orgs_self_memberships";

    public static final String ACTION_ORGS_USER_MEMBERSHIPS = "action_orgs_user_memberships";

    public static final String ACTION_REPOS_GET_REPO = "action_repos_get_repo";

    public static final String ACTION_REPOS_LIST_ORG_OWNED = "action_repos_list_org_owned";

    public static final String ACTION_REPOS_LIST_SELF_OWNED = "action_repos_list_self_owned";

    public static final String ACTION_REPOS_LIST_USER_OWNED = "action_repos_list_user_owned";

    public static final String ACTION_REPOS_LIST_USER_WATCHED = "action_repos_list_user_watched";

    public static final String ACTION_USERS_GET_USER = "action_users_get_user";

    public static final String ACTION_USERS_LIST_FOLLOWERS = "action_users_list_followers";

    public static final String ACTION_USERS_LIST_FOLLOWING = "action_users_list_following";

    public static final String ARG_ACCOUNT = "arg_account";

    public static final String ARG_ANONYMOUS = "arg_anonymous";

    public static final String ARG_BASIC_USERNAME = "arg_basic_username";

    public static final String ARG_BASIC_PASSWORD = "arg_basic_password";

    public static final String ARG_FORCE_REFRESH = "arg_force_refresh";

    public static final String ARG_OAUTH_TOKEN = "arg_oauth_token";

    public static final String ARG_START_PAGE = "arg_start_page";

    public static final String EXTRA_ERROR = "extra_error";

    public static final String EXTRA_HAS_NEXT = "extra_has_next";

    public static final String EXTRA_NEXT_PAGE = "extra_next_page";

    public static final String EXTRA_RESULT_JSON = "extra_result_json";

    public static final String PARAM_DIRECTION = "param_direction";

    public static final String PARAM_FILTER = "param_filter";

    public static final String PARAM_LABELS = "param_labels";

    public static final String PARAM_LOGIN = "param_login";

    public static final String PARAM_REPO_OWNER = "param_repo_owner";

    public static final String PARAM_REPO_NAME = "param_repo_name";

    public static final String PARAM_SINCE = "param_since";

    public static final String PARAM_SORT = "param_sort";

    public static final String PARAM_STATE = "param_state";

    public static final String USER_AGENT = "Hubroid/GitHubJava";

    private static String sApiHostname = HOST_API;

    private static int sApiPort = -1;

    private static String sApiScheme = PROTOCOL_HTTPS;

    private GitHubClient mGitHubClient;

    /**
     * Sets the Api hostname to connect to.
     *
     * Default is {@link IGitHubConstants#HOST_API}.
     */
    public static void setApiHostname(final String hostname) {
        sApiHostname = hostname;
    }

    /**
     * Sets the port to connect to the host with. Default is -1.
     */
    public static void setApiPort(final int port) {
        sApiPort = port;
    }

    /**
     * Sets the scheme to connect to the host with.
     *
     * Default is {@link IGitHubConstants#PROTOCOL_HTTPS}.
     */
    public static void setApiScheme(final String scheme) {
        sApiScheme = scheme;
    }

    public GitHubApiService() {
        super("GitHubApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        mGitHubClient = new GitHubClient(sApiHostname, sApiPort, sApiScheme);
        mGitHubClient.setUserAgent(USER_AGENT);

        if (!intent.getBooleanExtra(ARG_ANONYMOUS, false)) {
            if (intent.hasExtra(ARG_OAUTH_TOKEN)) {
                mGitHubClient.setOAuth2Token(intent.getStringExtra(ARG_OAUTH_TOKEN));
            } else if (intent.hasExtra(ARG_BASIC_USERNAME) && intent.hasExtra(ARG_BASIC_PASSWORD)) {
                mGitHubClient.setCredentials(intent.getStringExtra(ARG_BASIC_USERNAME),
                        intent.getStringExtra(ARG_BASIC_PASSWORD));
            } else if (intent.hasExtra(ARG_ACCOUNT) &&
                    intent.getParcelableExtra(ARG_ACCOUNT) != null) {
                final Account account = intent.getParcelableExtra(ARG_ACCOUNT);
                try {
                    final String oauthToken =
                            new OAuthUserProvider().getOAuthResponse(this, account).access_token;
                    if (oauthToken != null) {
                        mGitHubClient.setOAuth2Token(oauthToken);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AccountsException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        if (intent.getAction().equals(ACTION_GET_URI)) {
            final GitHubRequest request = new GitHubRequest();
            request.setUri(intent.getData().toString());

            GitHubResponse response;
            try {
                response = mGitHubClient.get(request);
            } catch (IOException e) {
                response = null;
                e.printStackTrace();
            }

            final Intent resultIntent = new Intent(ACTION_GET_URI, intent.getData());
            if (response != null) {
                resultIntent.putExtra(EXTRA_HAS_NEXT, response.getNext() != null);
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(response.getBody()));
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_EVENTS_LIST_USER_PUBLIC)) {
            final EventService es = new EventService(mGitHubClient);
            ArrayList<Event> result = null;
            PageIterator<Event> iterator = null;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            if (intent.hasExtra(PARAM_LOGIN)) {
                iterator = es.pageUserEvents(intent.getStringExtra(PARAM_LOGIN), true, startPage,
                        REQUEST_PAGE_SIZE);
            }

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Event>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_EVENTS_LIST_USER_PUBLIC);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_EVENTS_LIST_USER_RECEIVED)) {
            final EventService es = new EventService(mGitHubClient);
            ArrayList<Event> result = null;
            PageIterator<Event> iterator = null;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            if (intent.hasExtra(PARAM_LOGIN)) {
                iterator = es.pageUserReceivedEvents(intent.getStringExtra(PARAM_LOGIN), false,
                        startPage, REQUEST_PAGE_SIZE);
            }

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Event>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_EVENTS_LIST_USER_RECEIVED);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_EVENTS_LIST_TIMELINE)) {
            final EventService es = new EventService(mGitHubClient);
            ArrayList<Event> result = null;
            PageIterator<Event> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            iterator = es.pagePublicEvents(startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Event>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_EVENTS_LIST_TIMELINE);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_ISSUES_LIST_SELF)) {
            final IssueService is = new IssueService(mGitHubClient);
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);
            ArrayList<RepositoryIssue> result = null;
            PageIterator<RepositoryIssue> iterator;
            HashMap<String, String> params = new HashMap<String, String>();

            if (intent.hasExtra(PARAM_FILTER)) {
                params.put("filter", intent.getStringExtra(PARAM_FILTER));
            }
            if (intent.hasExtra(PARAM_STATE)) {
                params.put("state", intent.getStringExtra(PARAM_STATE));
            }
            if (intent.hasExtra(PARAM_LABELS)) {
                params.put("labels", intent.getStringExtra(PARAM_LABELS));
            }
            if (intent.hasExtra(PARAM_SORT)) {
                params.put("sort", intent.getStringExtra(PARAM_SORT));
            }
            if (intent.hasExtra(PARAM_DIRECTION)) {
                params.put("direction", intent.getStringExtra(PARAM_DIRECTION));
            }
            if (intent.hasExtra(PARAM_SINCE)) {
                params.put("since", intent.getStringExtra(PARAM_SINCE));
            }

            iterator = is.pageIssues(params, startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<RepositoryIssue>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_ISSUES_LIST_SELF);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_ORGS_SELF_MEMBERSHIPS)) {
            final OrganizationService os = new OrganizationService(mGitHubClient);
            List<User> result;
            try {
                result = os.getOrganizations();
            } catch (IOException e) {
                result = null;
                e.printStackTrace();
            }

            final Intent resultIntent = new Intent(ACTION_ORGS_SELF_MEMBERSHIPS);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_REPOS_GET_REPO)) {
            final RepositoryService rs = new RepositoryService(mGitHubClient);
            Repository result;
            try {
                result = rs.getRepository(intent.getStringExtra(PARAM_REPO_OWNER),
                        intent.getStringExtra(PARAM_REPO_NAME));
            } catch (IOException e) {
                result = null;
                e.printStackTrace();
            }

            final Intent resultIntent = new Intent(ACTION_REPOS_GET_REPO);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_REPOS_LIST_ORG_OWNED)) {
            final RepositoryService rs = new RepositoryService(mGitHubClient);
            ArrayList<Repository> result = null;
            PageIterator<Repository> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            iterator = rs.pageOrgRepositories(intent.getStringExtra(PARAM_LOGIN),
                    startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Repository>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_REPOS_LIST_ORG_OWNED);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_REPOS_LIST_SELF_OWNED)) {
            final RepositoryService rs = new RepositoryService(mGitHubClient);
            ArrayList<Repository> result = null;
            PageIterator<Repository> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            Map<String, String> filter = new HashMap<String, String>();
            filter.put("type", "owner");
            filter.put("sort", "pushed");

            iterator = rs.pageRepositories(filter, startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Repository>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_REPOS_LIST_SELF_OWNED);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_REPOS_LIST_USER_OWNED)) {
            final RepositoryService rs = new RepositoryService(mGitHubClient);
            ArrayList<Repository> result = null;
            PageIterator<Repository> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            iterator = rs.pageRepositories(intent.getStringExtra(PARAM_LOGIN),
                    startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Repository>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_REPOS_LIST_USER_OWNED);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_REPOS_LIST_USER_WATCHED)) {
            final WatcherService ws = new WatcherService(mGitHubClient);
            ArrayList<Repository> result = null;
            PageIterator<Repository> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            try {
                iterator = ws.pageWatched(intent.getStringExtra(PARAM_LOGIN),
                        startPage, REQUEST_PAGE_SIZE);
            } catch (IOException e) {
                iterator = null;
            }

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<Repository>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_REPOS_LIST_USER_WATCHED);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_USERS_GET_USER)) {
            final UserService us = new UserService(mGitHubClient);
            User result;
            try {
                result = us.getUser(intent.getStringExtra(PARAM_LOGIN));
            } catch (IOException e) {
                result = null;
                e.printStackTrace();
            }

            final Intent resultIntent = new Intent(ACTION_USERS_GET_USER);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_USERS_LIST_FOLLOWERS)) {
            final UserService us = new UserService(mGitHubClient);
            ArrayList<User> result = null;
            PageIterator<User> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            iterator = us.pageFollowers(intent.getStringExtra(PARAM_LOGIN),
                    startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<User>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_USERS_LIST_FOLLOWERS);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        } else if (intent.getAction().equals(ACTION_USERS_LIST_FOLLOWING)) {
            final UserService us = new UserService(mGitHubClient);
            ArrayList<User> result = null;
            PageIterator<User> iterator;
            final int startPage = intent.getIntExtra(ARG_START_PAGE, 1);

            iterator = us.pageFollowing(intent.getStringExtra(PARAM_LOGIN),
                    startPage, REQUEST_PAGE_SIZE);

            if (iterator != null && iterator.hasNext()) {
                result = new ArrayList<User>();
                result.addAll(iterator.next());
            }

            final Intent resultIntent = new Intent(ACTION_USERS_LIST_FOLLOWING);
            if (result != null) {
                resultIntent.putExtra(EXTRA_RESULT_JSON, GsonUtils.toJson(result));
                resultIntent.putExtra(EXTRA_HAS_NEXT, iterator.hasNext());
                resultIntent.putExtra(EXTRA_NEXT_PAGE, iterator.getNextPage());
            } else {
                resultIntent.putExtra(EXTRA_ERROR, true);
            }
            sendBroadcast(resultIntent);
        }
    }
}
