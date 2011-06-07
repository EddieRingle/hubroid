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

import org.eclipse.egit.github.core.GistFile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.TextView;

public class GistFileViewer extends BaseActivity {
    private static class LoadGistTask extends AsyncTask<Void, Void, Void> {
        GistFileViewer activity;

        @Override
        protected Void doInBackground(final Void... params) {
            /*
             * Prepare CSS for file
             */
            activity.mHtml = "<style type=\"text/css\">" + "div {" + "margin-right: 100%25;"
                    + "font-family: monospace;" + "white-space: nowrap;"
                    + "display: inline-block; float: left; clear: both;" + "}" + ".filename {"
                    + "background-color: #EAF2F5;" + "}" + "</style>";

            final String filename = activity.mGistFile.getFilename();

            activity.mHtml += "<div class=\"filename\">" + filename + "</div><br/>";

            if (filename.endsWith(".md") || filename.endsWith(".markdown")
                    || filename.endsWith(".mdown")) {
                activity.mHtml += new MarkdownProcessor().markdown(activity.mGistFile.getContent());
            } else {
                final String[] splitFile = activity.mGistFile.getContent().split("\n");
                for (int i = 0; i < splitFile.length; i++) {

                    /* HTML Encode line to make it safe for viewing */
                    splitFile[i] = TextUtils.htmlEncode(splitFile[i]);

                    // Replace all tabs with four non-breaking spaces (most
                    // browsers truncate "\t+" to " ").
                    splitFile[i] = splitFile[i].replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

                    // Replace any sequence of two or more spaces with
                    // &nbsps
                    // (most browsers truncate " +" to " ").
                    splitFile[i] = splitFile[i].replaceAll("(?<= ) ", "&nbsp;");

                    activity.mHtml += "<div>" + splitFile[i] + "</div>";
                }
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
                    "Loading gist file...", true);
        }
    }

    private String mHtml;

    public Intent mIntent;

    private LoadGistTask mLoadGistTask;

    private ProgressDialog mProgressDialog;

    private WebView mWebView;

    private String mGistId;

    private GistFile mGistFile;

    private String mFileRawURL;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gist_viewer);

        mWebView = (WebView) findViewById(R.id.wv_gistViewer_contents);

        setupActionBar();

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Gist Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGistId = extras.getString("gistId");
            mGistFile = new GistFile();
            mGistFile.setFilename(extras.getString("fileName"));
            mGistFile.setContent(extras.getString("fileContents"));
            mFileRawURL = extras.getString("fileRawURL");

            title.setText("Gist " + mGistId);

            mLoadGistTask = (LoadGistTask) getLastNonConfigurationInstance();
            if (mLoadGistTask == null) {
                mLoadGistTask = new LoadGistTask();
            }
            mLoadGistTask.activity = GistFileViewer.this;
            if (mLoadGistTask.getStatus() == AsyncTask.Status.RUNNING) {
                mProgressDialog.show();
            } else if (mLoadGistTask.getStatus() == AsyncTask.Status.PENDING) {
                mLoadGistTask.execute();
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
            ((WebView) findViewById(R.id.wv_gistViewer_contents)).loadData(mHtml, "text/html",
                    "UTF-8");
        }

        if (mLoadGistTask != null) {
            mLoadGistTask.activity = this;
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadGistTask;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putString("html", mHtml);
        super.onSaveInstanceState(outState);
    }
}
