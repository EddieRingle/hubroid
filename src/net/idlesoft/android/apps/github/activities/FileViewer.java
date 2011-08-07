/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.petebevin.markdown.MarkdownProcessor;

import net.idlesoft.android.apps.github.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.TextView;

public class FileViewer extends BaseActivity {
    private static class LoadBlobTask extends AsyncTask<Void, Void, Void> {
        FileViewer activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGApi.object.blob(
                        activity.mRepositoryOwner, activity.mRepositoryName, activity.mTreeSha,
                        activity.mBlobPath).resp).getJSONObject("blob");
                /*
                 * Prepare CSS for file
                 */
                activity.mHtml = "<style type=\"text/css\">" + "div {" + "margin-right: 100%25;"
                        + "font-family: monospace;" + "white-space: nowrap;"
                        + "display: inline-block; float: left; clear: both;" + "}" + ".filename {"
                        + "background-color: #EAF2F5;" + "}" + "</style>";

                activity.mHtml += "<div class=\"filename\">" + activity.mJson.getString("name")
                        + "</div><br/>";

                final String mimeType = activity.mJson.getString("mime_type");

                if (mimeType.startsWith("text") || mimeType.startsWith("application")) {
                    if (activity.mJson.getString("name").endsWith(".md")
                            || activity.mJson.getString("name").endsWith(".markdown")
                            || activity.mJson.getString("name").endsWith(".mdown")) {
                        activity.mHtml += new MarkdownProcessor().markdown(activity.mJson
                                .getString("data"));
                    } else {
                        final String[] splitFile = activity.mJson.getString("data").split("\n");
                        for (int i = 0; i < splitFile.length; i++) {

                            /* HTML Encode line to make it safe for viewing */
                            splitFile[i] = TextUtils.htmlEncode(splitFile[i]);

                            // Replace all tabs with four non-breaking spaces
                            // (most
                            // browsers truncate "\t+" to " ").
                            splitFile[i] = splitFile[i]
                                    .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

                            // Replace any sequence of two or more spaces with
                            // &nbsps
                            // (most browsers truncate " +" to " ").
                            splitFile[i] = splitFile[i].replaceAll("(?<= ) ", "&nbsp;");

                            activity.mHtml += "<div>" + splitFile[i] + "</div>";
                        }
                    }
                } else if (mimeType.startsWith("image")) {
                    activity.mHtml += "<img src=\"https://" + activity.mUsername + ":"
                            + activity.mPassword + "@github.com/api/v2/json/blob/show/"
                            + activity.mRepositoryOwner + "/" + activity.mRepositoryName + "/"
                            + activity.mBlobSha + "\" alt=\"Image\" />";
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mWebView.loadDataWithBaseURL("hubroid", activity.mHtml, "text/html", "UTF-8",
                    "hubroid");
            activity.mProgressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                    "Loading blob...", true);
        }
    }

    public String mBlobPath;

    private String mHtml;

    public Intent mIntent;

    public JSONObject mJson;

    private LoadBlobTask mLoadBlobTask;

    private ProgressDialog mProgressDialog;

    public String mRepositoryName;

    public String mRepositoryOwner;

    public String mTreeSha;

    private WebView mWebView;

    private String mBlobSha;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.file_view);

        mWebView = (WebView) findViewById(R.id.wv_fileView_contents);

        setupActionBar();

        getActionBar().setTitle("File Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");
            mTreeSha = extras.getString("tree_sha");
            mBlobPath = extras.getString("blob_path");
            mBlobSha = extras.getString("blob_sha");

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
    public void onPause() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mHtml = savedInstanceState.getString("html");

        if (mHtml != null) {
            ((WebView) findViewById(R.id.wv_fileView_contents)).loadData(mHtml, "text/html",
                    "UTF-8");
        }

        if (mLoadBlobTask != null) {
            mLoadBlobTask.activity = this;
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadBlobTask;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putString("html", mHtml);
        super.onSaveInstanceState(outState);
    }
}
