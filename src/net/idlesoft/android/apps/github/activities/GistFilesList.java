/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.GistFilesListAdapter;

import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Map;

public class GistFilesList extends BaseActivity {
    private Map<String, GistFile> mFileMap;

    private static class GetGistFilesTask extends AsyncTask<Void, Void, Void> {
        GistFilesList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                final GistService gs = new GistService(activity.getGitHubClient());
                activity.mFileMap = gs.getGist(activity.mGistId).getFiles();
                activity.mGistFilesListAdapter = new GistFilesListAdapter(activity, activity.mFileMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mGistFilesList.removeHeaderView(activity.mLoadView);
            activity.mGistFilesList.setAdapter(activity.mGistFilesListAdapter);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            activity.mGistFilesList.addHeaderView(activity.mLoadView);
            activity.mGistFilesList.setAdapter(null);
            super.onPreExecute();
        }
    }

    public ListView mGistFilesList;

    public GistFilesListAdapter mGistFilesListAdapter;

    public GetGistFilesTask mGetGistFilesTask;

    public Intent mIntent;

    public JSONObject mJson;

    public View mLoadView;

    private String mGistId;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gist_file_list);

        mLoadView = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        mGistFilesList = (ListView) findViewById(R.id.lv_gistFiles);
        mGistFilesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position,
                    long id) {
                final String fileName = (String)mFileMap.keySet().toArray()[position];
                final GistFile gistFile = mFileMap.get(fileName);
                final Intent intent = new Intent(GistFilesList.this, GistFileViewer.class);
                intent.putExtra("gistId", mGistId);
                intent.putExtra("fileName", gistFile.getFilename());
                intent.putExtra("fileContents", gistFile.getContent());
                intent.putExtra("fileRawURL", gistFile.getRawUrl());
                intent.putExtra("fileSize", gistFile.getSize());
                startActivity(intent);
            };
        });

        setupActionBar();

        final TextView title = (TextView) findViewById(R.id.tv_page_title);
        title.setText("Gist Files");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGistId = extras.getString("gistId");

            mGetGistFilesTask = (GetGistFilesTask) getLastNonConfigurationInstance();

            if (mGetGistFilesTask == null) {
                mGetGistFilesTask = new GetGistFilesTask();
            }

            mGetGistFilesTask.activity = this;

            if ((mGetGistFilesTask.getStatus() == AsyncTask.Status.PENDING)
                    && (mGistFilesListAdapter == null)) {
                mGetGistFilesTask.execute();
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetGistFilesTask;
    }
}
