/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.flurry.android.FlurryAgent;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.File;

public class CommitChangeViewer extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public JSONObject mJson;

    private SharedPreferences.Editor mEditor;

    public Intent mIntent;

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mPassword;

    private String mUsername;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit_view);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Commit Diff");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            try {
                mJson = new JSONObject(extras.getString("json"));

                /*
                 * This new method of displaying file diffs was inspired by iOctocat's approach.
                 * Thanks to Dennis Bloete (dbloete on GitHub) for creating iOctocat and
                 * making me realize Android needed some GitHub love too. ;-)
                 */
                final WebView webView = (WebView) findViewById(R.id.wv_commitView_diff);

                /*
                 * Prepare CSS for diff:
                 * Added lines are green, removed lines are red, and the special lines that specify
                 * how many lines were affected in the chunk are a light blue.
                 */
                String content =
                        "<style type=\"text/css\">"
                        + "div {"
                        + "margin-right: 100%25;"
                        + "font-family: monospace;"
                        + "white-space: nowrap;"
                        + "display: inline-block;"
                        + "}"
                        + ".lines {"
                        + "background-color: #EAF2F5;"
                        + "}"
                        + ".added {"
                        + "background-color: #DDFFDD;"
                        + "}"
                        + ".removed {"
                        + "background-color: #FFDDDD;"
                        + "}"
                        + "</style>";

                String[] splitDiff = mJson.getString("diff").split("\n");
                for (int i = 0; i < splitDiff.length; i++) {
                    // Replace all tabs with four non-breaking spaces (most browsers truncate "\t+" to " ").
                    splitDiff[i] = splitDiff[i].replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

                    // Replace any sequence of two or more spaces with &nbsps (most browsers truncate " +" to " ").
                    splitDiff[i] = splitDiff[i].replaceAll("(?<= ) ","&nbsp;");

                    if (splitDiff[i].startsWith("@@")) {
                        splitDiff[i] = "<div class=\"lines\">".concat(splitDiff[i].concat("</div>"));
                    } else if (splitDiff[i].startsWith("+")) {
                        splitDiff[i] = "<div class=\"added\">".concat(splitDiff[i].concat("</div>"));
                    } else if (splitDiff[i].startsWith("-")) {
                        splitDiff[i] = "<div class=\"removed\">".concat(splitDiff[i].concat("</div>"));
                    } else {
                        // Add an extra space before lines not beginning with "+" or "-" to make them line up properly
                        splitDiff[i] = "<div>&nbsp;".concat(splitDiff[i].substring(1).concat("</div>"));
                    }
                    content += splitDiff[i];
                }
                webView.loadData(content, "text/html", "UTF-8");

            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
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
            case 2:
                final File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()) {
                    final File hubroid = new File(root, "hubroid");
                    if (!hubroid.exists() && !hubroid.isDirectory()) {
                        return true;
                    } else {
                        hubroid.delete();
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
            menu.add(0, 1, 0, "Clear Preferences");
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
