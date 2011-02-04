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
import org.w3c.dom.Text;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

public class FileViewer extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public JSONObject mJson;

    private SharedPreferences.Editor mEditor;

    public Intent mIntent;

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mPassword;

    private String mUsername;

    private ProgressDialog mProgressDialog;

    private LoadBlobTask mLoadBlobTask;

    public String mTreeSha;

    public String mBlobPath;

    private String mHtml;

    private static class LoadBlobTask extends AsyncTask<Void, Void, Void> {
        FileViewer activity;

        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...", "Loading blob...", true);
        }

        protected Void doInBackground(Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGapi.object.blob(activity.mRepositoryOwner, activity.mRepositoryName, activity.mTreeSha, activity.mBlobPath).resp).getJSONObject("blob");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            try {
                final WebView webView = (WebView) activity.findViewById(R.id.wv_fileView_contents);

                /*
                 * Prepare CSS for file
                 */
                activity.mHtml =
                        "<style type=\"text/css\">"
                        + "div {"
                        + "margin-right: 100%25;"
                        + "font-family: monospace;"
                        + "white-space: nowrap;"
                        + "display: inline-block;"
                        + "}"
                        + ".filename {"
                        + "background-color: #EAF2F5;"
                        + "}"
                        + "</style>";

                activity.mHtml += "<div class=\"filename\">" + activity.mJson.getString("name") + "</div><br/>";

                String[] splitFile = activity.mJson.getString("data").split("\n");
                for (int i = 0; i < splitFile.length; i++) {

                    /* HTML Encode line to make it safe for viewing */
                    splitFile[i] = TextUtils.htmlEncode(splitFile[i]);

                    // Replace all tabs with four non-breaking spaces (most browsers truncate "\t+" to " ").
                    splitFile[i] = splitFile[i].replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

                    // Replace any sequence of two or more spaces with &nbsps (most browsers truncate " +" to " ").
                    splitFile[i] = splitFile[i].replaceAll("(?<= ) ","&nbsp;");

                    activity.mHtml += "<div>" + splitFile[i] + "</div>";
                }
                webView.loadData(activity.mHtml, "text/html", "UTF-8");

            } catch (final JSONException e) {
                e.printStackTrace();
            }
            activity.mProgressDialog.dismiss();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.file_view);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(FileViewer.this, Search.class));
            }
        });

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("File Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mTreeSha = extras.getString("tree_sha");
            mBlobPath = extras.getString("blob_path");

            mLoadBlobTask = (LoadBlobTask) getLastNonConfigurationInstance();
            if (mLoadBlobTask == null) {
                mLoadBlobTask = new LoadBlobTask();
            }
            mLoadBlobTask.activity = FileViewer.this;
            if (mLoadBlobTask.getStatus() == AsyncTask.Status.RUNNING) {
                mProgressDialog.show();
            } else if (mLoadBlobTask.getStatus() == AsyncTask.Status.PENDING) {
                mLoadBlobTask.execute();
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
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
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

    public Object onRetainNonConfigurationInstance() {
        return mLoadBlobTask;
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("html", mHtml);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mHtml = savedInstanceState.getString("html");

        if (mHtml != null) {
            ((WebView) findViewById(R.id.wv_fileView_contents)).loadData(mHtml, "text/html", "UTF-8");
        }

        super.onRestoreInstanceState(savedInstanceState);
    }
}
