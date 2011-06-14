/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.google.gson.Gson;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.SingleGistInfoListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class SingleGist extends BaseActivity {
    private String mGistId;

    private String mGistOwner;

    private String mGistDescription;

    private String mGistUpdatedDate;

    private String mGistCreatedDate;

    private String mGistURL;

    private int mGistFileCount;

    protected JSONObject buildListItem(final String title, final String content)
            throws JSONException {
        return new JSONObject().put("title", title).put("content", content);
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gist);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGistId = extras.getString("gistId");
            mGistOwner = extras.getString("gistOwner");
            mGistDescription = extras.getString("gistDescription");
            mGistUpdatedDate = extras.getString("gistUpdatedDate");
            mGistCreatedDate = extras.getString("gistCreatedDate");
            mGistURL = extras.getString("gistURL");
            mGistFileCount = extras.getInt("gistFileCount");

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
    }
}
