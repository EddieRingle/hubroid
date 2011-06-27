/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.SingleGistInfoListAdapter;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class SingleGist extends BaseActivity {
    private String mGistId;

    private String mGistOwner;

    private String mGistDescription;

    private String mGistUpdatedDate;

    private String mGistCreatedDate;

    private String mGistURL;

    private int mGistFileCount;

    private LoadGistTask mTask;

    protected JSONObject buildListItem(final String title, final String content)
            throws JSONException {
        return new JSONObject().put("title", title).put("content", content);
    }

    protected void buildUI()
    {
        final TextView gistId = (TextView) findViewById(R.id.tv_gist_id);
        gistId.setText("Gist " + mGistId);

        final JSONArray listItems = new JSONArray();
        try {
            listItems.put(buildListItem("Owner", mGistOwner));
            listItems.put(buildListItem("Description", mGistDescription));
            listItems.put(buildListItem("Gist URL", mGistURL));
            listItems.put(buildListItem("Last Updated", mGistUpdatedDate));
            listItems.put(buildListItem("Created On", mGistCreatedDate));
            listItems.put(buildListItem("Files", String.valueOf(mGistFileCount)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ListView infoList = (ListView) findViewById(R.id.lv_gist_info);
        final SingleGistInfoListAdapter adapter = new SingleGistInfoListAdapter(this, infoList);
        adapter.loadData(listItems);
        adapter.pushData();
        infoList.setAdapter(adapter);

        infoList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View v,
                    final int position, final long id) {
                try {
                    final String title = ((JSONObject) listItems.get(position))
                            .getString("title");
                    final String content = ((JSONObject) listItems.get(position))
                            .getString("content");
                    final Intent intent;

                    if (title.equals("Owner")) {
                        intent = new Intent(SingleGist.this, Profile.class);
                        intent.putExtra("username", content);
                    } else if (title.equals("Gist URL")) {
                        intent = new Intent("android.intent.action.VIEW", Uri.parse(mGistURL));
                    } else if (title.equals("Files")) {
                        intent = new Intent(SingleGist.this, GistFilesList.class);
                        intent.putExtra("gistId", mGistId);
                    } else {
                        intent = null;
                    }
                    if (intent != null) {
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static class LoadGistTask extends AsyncTask<Void, Void, Boolean>
    {
        public SingleGist activity;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.getActionBar().setProgressBarVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(Void... params) {
            GitHubClient client = new GitHubClient();
            GistService gs = new GistService(client);

            try {
                Gist g = gs.getGist(activity.mGistId);
                activity.mGistOwner = g.getUser().getLogin();
                activity.mGistDescription = g.getDescription();
                activity.mGistUpdatedDate = g.getUpdatedAt().toString();
                activity.mGistCreatedDate = g.getCreatedAt().toString();
                activity.mGistURL = g.getUrl();
                activity.mGistFileCount = g.getFiles().size();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                activity.buildUI();
                activity.getActionBar().setProgressBarVisibility(View.GONE);
            } else {
                Toast.makeText(activity, "Failed to load gist.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            super.onPostExecute(result);
        }
    }
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gist);

        setupActionBar();

        mTask = (LoadGistTask) getLastNonConfigurationInstance();
        if (mTask == null) {
            mTask = new LoadGistTask();
        }
        mTask.activity = this;

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGistId = extras.getString("gistId");
            if (extras.containsKey("gistOwner")) {
                mGistOwner = extras.getString("gistOwner");
                mGistDescription = extras.getString("gistDescription");
                mGistUpdatedDate = extras.getString("gistUpdatedDate");
                mGistCreatedDate = extras.getString("gistCreatedDate");
                mGistURL = extras.getString("gistURL");
                mGistFileCount = extras.getInt("gistFileCount");
                buildUI();
            } else {
                if (mTask.getStatus() == AsyncTask.Status.PENDING) {
                    mTask.execute();
                }
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }
}
