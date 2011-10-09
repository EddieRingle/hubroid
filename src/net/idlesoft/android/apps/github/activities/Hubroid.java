/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import android.content.Intent;
import android.os.Bundle;

public class Hubroid extends BaseActivity {
    /*
     * TODO: Review these time format constants, as the new GitHub library
     * provides them
     */
    // Time format used by GitHub in their issue API. Inconsistent, tsk, tsk.
    public static final String GITHUB_ISSUES_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZZ";

    // Time format used by GitHub in their responses
    public static final String GITHUB_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";

    public static final String PREFS_NAME = "HubroidPrefs";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, NO_LAYOUT);
        if (mUsername.equals("")) {
            startActivity(new Intent(Hubroid.this, Login.class));
        } else {
            startActivity(new Intent(Hubroid.this, Dashboard.class).putExtra("fresh", true));
        }
        finish();
    }
}
