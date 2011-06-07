/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SingleGist extends BaseActivity {
    private String mGistId;

    public Intent mIntent;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gist);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGistId = extras.getString("gistId");

            final TextView gistId = (TextView)findViewById(R.id.tv_gist_id);
            gistId.setText(mGistId);

            final ListView infoList = (ListView)findViewById(R.id.lv_gist_info);
            infoList.setAdapter(new ArrayAdapter<String>(SingleGist.this, R.layout.branch_info_item,
                    R.id.tv_branchInfoItem_text1, new String[] {
                            "Files"
                    }));
            infoList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(final AdapterView<?> parent, final View v,
                        final int position, final long id) {
                    if (position == 0) {
                        mIntent = new Intent(SingleGist.this, GistFilesList.class);
                        mIntent.putExtra("gistId", mGistId);
                        startActivity(mIntent);
                    }
                }
            });
        }
    }
}
