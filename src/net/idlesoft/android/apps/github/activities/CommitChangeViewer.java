/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

public class CommitChangeViewer extends Activity {
    private SharedPreferences.Editor mEditor;

    private final GitHubAPI mGapi = new GitHubAPI();

    public Intent mIntent;

    public JSONObject mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mUsername;

    private WebView mWebView;

    private String mHtml;

    private LoadDiffTask mLoadDiffTask;

    private static class LoadDiffTask extends AsyncTask<Void, Void, Void> {
        public CommitChangeViewer activity;

        protected Void doInBackground(Void... params) {
            /*
             * This new method of displaying file diffs was inspired by
             * iOctocat's approach. Thanks to Dennis Bloete (dbloete on
             * GitHub) for creating iOctocat and making me realize Android
             * needed some GitHub love too. ;-)
             */

            /*
             * Prepare CSS for diff: Added lines are green, removed lines
             * are red, and the special lines that specify how many lines
             * were affected in the chunk are a light blue.
             */
            activity.mHtml = "<style type=\"text/css\">" + "div {" + "margin-right: 100%25;"
                    + "font-family: monospace;" + "white-space: nowrap;"
                    + "display: inline-block; float: left; clear: both;" + "}" + ".lines {"
                    + "background-color: #EAF2F5;" + "}" + ".added {"
                    + "background-color: #DDFFDD;" + "}" + ".removed {"
                    + "background-color: #FFDDDD;" + "}" + "</style>";

            try {
                final String[] splitDiff = activity.mJson.getString("diff").split("\n");
                
                for (int i = 0; i < splitDiff.length; i++) {
                    // HTML encode any elements, else any diff containing
                    // "<div>" or any HTML element will be interpreted as one by
                    // the browser
                    splitDiff[i] = TextUtils.htmlEncode(splitDiff[i]);
    
                    // Replace all tabs with four non-breaking spaces (most
                    // browsers truncate "\t+" to " ").
                    splitDiff[i] = splitDiff[i].replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    
                    // Replace any sequence of two or more spaces with &nbsps
                    // (most browsers truncate " +" to " ").
                    splitDiff[i] = splitDiff[i].replaceAll("(?<= ) ", "&nbsp;");
    
                    if (splitDiff[i].startsWith("@@")) {
                        splitDiff[i] = "<div class=\"lines\">"
                                .concat(splitDiff[i].concat("</div>"));
                    } else if (splitDiff[i].startsWith("+")) {
                        splitDiff[i] = "<div class=\"added\">"
                                .concat(splitDiff[i].concat("</div>"));
                    } else if (splitDiff[i].startsWith("-")) {
                        splitDiff[i] = "<div class=\"removed\">".concat(splitDiff[i]
                                .concat("</div>"));
                    } else {
                        // Add an extra space before lines not beginning with
                        // "+" or "-" to make them line up properly
                        if (splitDiff[i].length() > 0) {
                            splitDiff[i] = "<div>&nbsp;".concat(splitDiff[i].substring(1).concat(
                                    "</div>"));
                        }
                    }

                    activity.mHtml += splitDiff[i];
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mWebView.loadDataWithBaseURL("hubroid", activity.mHtml, "text/html", "UTF-8", "hubroid");
        }

    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit_view);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mWebView = (WebView) findViewById(R.id.wv_commitView_diff);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(CommitChangeViewer.this, Search.class));
            }
        });

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Commit Diff");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            try {
                mJson = new JSONObject(extras.getString("json"));

                mLoadDiffTask = (LoadDiffTask) getLastNonConfigurationInstance();
                if (mLoadDiffTask == null) {
                    mLoadDiffTask = new LoadDiffTask();
                }
                mLoadDiffTask.activity = CommitChangeViewer.this;
                if (mLoadDiffTask.getStatus() == AsyncTask.Status.PENDING) {
                    mLoadDiffTask.execute();
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Object onRetainNonConfigurationInstance() {
        return mLoadDiffTask;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                mEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (mHtml != null && !mHtml.equals("")) {
            outState.putString("html", mHtml);
        }
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("html")) {
            mHtml = savedInstanceState.getString("html");
        }
        if (mHtml != null) {
            mWebView.loadData(mHtml, "text/html", "UTF-8");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

}