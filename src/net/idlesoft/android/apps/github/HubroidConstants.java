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

package net.idlesoft.android.apps.github;

public class HubroidConstants {

    public final static String USER_AGENT_STRING = "Hubroid/3.0";

    public final static String ARG_TARGET_REPO = "target_repo";

    public final static String ARG_TARGET_USER = "target_user";

    public final static String ARG_TARGET_ISSUE = "target_issue";

    public final static String ARG_TARGET_URI = "target_uri";

    public final static String ARG_HANDLED_LISTS = "handled_lists";

    public final static String DEFAULT_USER = "eddieringle";

    public final static String DEFAULT_REPO = "hubroid";

    /*
      * Preferences keys
      */
    public static final String PREF_CURRENT_USER = "currentUser";

    public static final String PREF_CURRENT_USER_LOGIN = "currentUserLogin";

    public static final String PREF_CURRENT_CONTEXT_LOGIN = "currentContextLogin";

    public static final String PREF_FIRST_RUN = "firstRun";

    public static final String PREF_LAST_DASHBOARD_LIST = "lastDashboardList";

    /*
      * Loader IDs
      *
      * Each ID has to be unique in order to prevent conflicts where two fragments
      * that might both use a loader with id 0 or something and cause a crash.
      * Experienced the bug during development, doing it this way now to prevent
      * further issues.
      */
    public static final int LOADER_CONTEXTS = 0;

    public static final int LOADER_PROFILE = 1;

    public static final int LOADER_OWNED_REPOSITORIES_PAGER = 2;

    public static final int LOADER_WATCHED_REPOSITORIES_PAGER = 3;

    public static final int REQUEST_PAGE_SIZE = 20;
}