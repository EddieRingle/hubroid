/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.File;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
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

import com.flurry.android.FlurryAgent;

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
            String gitRef = extras.getString("commit_sha");
            try {
                Response commitInfo = mGapi.commits.commit(mRepositoryOwner, mRepositoryName, gitRef);
                JSONObject mJson = new JSONObject(commitInfo.resp).getJSONObject("commit");

                final WebView webView = (WebView) findViewById(R.id.wv_commitView_diff);

                String content =
                    "<style type=\"text/css\">"
                    + "div {"
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

                // Example response:
                // 01-26 17:52:16.637: DEBUG/martin(1606): {"message":"Passes jXHR object as third argument of prefilters and transport factories.","id":"bab8079593913dbc689404aa4e83c46b9b4c9355","author":{"email":"j@ubourg.net","login":"jaubourg","name":"jaubourg"},"parents":[{"id":"d7d64713a72c67243b279b9dcb16ae9fbb825c17"}],"tree":"0ec897dc75a1602eed81a9531982726c0baa3d7b","authored_date":"2011-01-26T08:37:08-08:00","committer":{"email":"j@ubourg.net","login":"jaubourg","name":"jaubourg"},"committed_date":"2011-01-26T08:37:08-08:00","url":"\/jquery\/jquery\/commit\/bab8079593913dbc689404aa4e83c46b9b4c9355","modified":[{"filename":"src\/ajax.js","diff":"--- a\/src\/ajax.js\n+++ b\/src\/ajax.js\n@@ -73,7 +73,7 @@ function addToPrefiltersOrTransports( structure ) {\n }\n \n \/\/Base inspection function for prefilters and transports\n-function inspectPrefiltersOrTransports( structure, options, originalOptions,\n+function inspectPrefiltersOrTransports( structure, options, originalOptions, jXHR,\n \t\tdataType \/* internal *\/, inspected \/* internal *\/ ) {\n \n \tdataType = dataType || options.dataTypes[ 0 ];\n@@ -97,7 +97,7 @@ function inspectPrefiltersOrTransports( structure, options, originalOptions,\n \t\t\t} else {\n \t\t\t\toptions.dataTypes.unshift( selection );\n \t\t\t\tselection = inspectPrefiltersOrTransports(\n-\t\t\t\t\t\tstructure, options, originalOptions, selection, inspected );\n+\t\t\t\t\t\tstructure, options, originalOptions, jXHR, selection, inspected );\n \t\t\t}\n \t\t}\n \t}\n@@ -105,7 +105,7 @@ function inspectPrefiltersOrTransports( structure, options, originalOptions,\n \t\/\/ we try the catchall dataType if not done already\n \tif ( ( executeOnly || !selection ) && !inspected[ \"*\" ] ) {\n \t\tselection = inspectPrefiltersOrTransports(\n-\t\t\t\tstructure, options, originalOptions, \"*\", inspected );\n+\t\t\t\tstructure, options, originalOptions, jXHR, \"*\", inspected );\n \t}\n \t\/\/ unnecessary when only executing (prefilters)\n \t\/\/ but it'll be ignored by the caller in that case\n@@ -565,7 +565,7 @@ jQuery.extend({\n \t\t}\n \n \t\t\/\/ Apply prefilters\n-\t\tinspectPrefiltersOrTransports( prefilters, s, options );\n+\t\tinspectPrefiltersOrTransports( prefilters, s, options, jXHR );\n \n \t\t\/\/ Uppercase the type\n \t\ts.type = s.type.toUpperCase();\n@@ -638,7 +638,7 @@ jQuery.extend({\n \t\t\t}\n \n \t\t\t\/\/ Get transport\n-\t\t\ttransport = inspectPrefiltersOrTransports( transports, s, options );\n+\t\t\ttransport = inspectPrefiltersOrTransports( transports, s, options, jXHR );\n \n \t\t\t\/\/ If no transport, we auto-abort\n \t\t\tif ( !transport ) {"}]}
                // Cater for multiple file changes
                JSONArray fileDiffList = mJson.getJSONArray("modified");
                for(int j = 0; j < fileDiffList.length(); j ++){
                    String[] splitDiff = fileDiffList.getJSONObject(j).getString("diff").split("\n");
                    for (int i = 0; i < splitDiff.length; i++) {
                        if (splitDiff[i].startsWith("@@")) {
                            splitDiff[i] = "<div class=\"lines\">".concat(splitDiff[i].concat("</div>"));
                        } else if (splitDiff[i].startsWith("+")) {
                            splitDiff[i] = "<div class=\"added\">".concat(splitDiff[i].concat("</div>"));
                        } else if (splitDiff[i].startsWith("-")) {
                            splitDiff[i] = "<div class=\"removed\">".concat(splitDiff[i].concat("</div>"));
                        } else {
                            splitDiff[i] = "<div>".concat(splitDiff[i].concat("</div>"));
                        }
                        content += splitDiff[i];
                    }
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
