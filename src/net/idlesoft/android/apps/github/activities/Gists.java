/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.MyGists;
import net.idlesoft.android.apps.github.activities.tabs.StarredGists;
import net.idlesoft.android.apps.github.utils.GravatarCache;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Gists extends BaseTabActivity {
    private static final String TAG_MY_GISTS = "my_gists";

    private static final String TAG_STARRED_GISTS = "starred_gists";

    private String mTarget;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.gists);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        final ImageView gravatar = (ImageView) findViewById(R.id.iv_gists_gravatar);
        if (mTarget != null && !mTarget.equals("")) {
            gravatar.setImageBitmap(GravatarCache.getDipGravatar(
                    GravatarCache.getGravatarID(mTarget), 38.0f,
                    getResources().getDisplayMetrics().density));
            ((TextView) findViewById(R.id.tv_page_title)).setText(mTarget + "'s Gists");

            gravatar.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    final Intent i = new Intent(Gists.this, Profile.class);
                    i.putExtra("username", mTarget);
                    startActivity(i);
                }
            });

            Intent intent = new Intent(getApplicationContext(), MyGists.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_MY_GISTS)
                    .setIndicator(buildIndicator(R.string.my_gists)).setContent(intent));

            intent = new Intent(getApplicationContext(), StarredGists.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_STARRED_GISTS)
                    .setIndicator(buildIndicator(R.string.starred_gists)).setContent(intent));
        }
        if (!mTarget.equals(mUsername)) {
            mTabHost.getTabWidget().setVisibility(View.GONE);
        }
    }
}
