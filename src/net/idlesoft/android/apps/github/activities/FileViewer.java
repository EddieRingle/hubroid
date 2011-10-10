/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.StringUtils;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.DataService;

import shade.org.apache.commons.codec.binary.Base64;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;

public class FileViewer extends BaseActivity {
    private Blob mBlob = null;

    private String mBlobName;

    private String mMimeType;

    private static class LoadBlobTask extends AsyncTask<Void, Void, Void> {
        FileViewer activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                if (activity.mBlob == null) {
                    final DataService ds = new DataService(activity.getGitHubClient());
                    activity.mBlob = ds.getBlob(RepositoryId.create(activity.mRepositoryOwner,
                            activity.mRepositoryName), activity.mBlobSha);
                }
                final String content = (activity.mBlob.getEncoding().equals("utf-8")) ? activity.mBlob
                        .getContent() : new String(Base64.decodeBase64(activity.mBlob.getContent()
                        .getBytes()));

                activity.mMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        StringUtils.getExtension(activity.mBlobName));
                if (activity.mMimeType == null || !activity.mMimeType.startsWith("image")) {
                    activity.mMimeType = "text/html";
                }

                if (activity.mMimeType.startsWith("image")) {
                    activity.mHtml = "<img src='data:" + activity.mMimeType + ";base64,"
                            + activity.mBlob.getContent() + "' />";
                    activity.mMimeType = "text/html";
                } else {
                    activity.mHtml = StringUtils.blobToHtml(content, activity.mBlobName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            final WebSettings ws = activity.mWebView.getSettings();
            ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
            ws.setBuiltInZoomControls(true);
            ws.setSupportZoom(true);
            ws.setJavaScriptEnabled(true);
            ws.setSupportMultipleWindows(true);
            ws.setUseWideViewPort(true);
            ws.setLoadsImagesAutomatically(true);

            activity.mWebView.loadDataWithBaseURL("file:///android_asset/", activity.mHtml,
                    activity.mMimeType, "UTF-8", "");
            activity.mProgressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please Wait...",
                    "Loading blob...", true);
        }
    }

    private String mHtml;

    private LoadBlobTask mLoadBlobTask;

    private ProgressDialog mProgressDialog;

    public String mRepositoryName;

    public String mRepositoryOwner;

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
            mBlobSha = extras.getString("blob_sha");
            mBlobName = extras.getString("blob_name");
            if (extras.containsKey("blob")) {
                mBlob = GsonUtils.fromJson(extras.getString("blob"), Blob.class);
            }

            final TextView filename = (TextView) findViewById(R.id.tv_file_name);
            filename.setText(mBlobName);
            filename.requestFocus();

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
    public Object onRetainNonConfigurationInstance() {
        return mLoadBlobTask;
    }
}
