/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.MyOrgs;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Organizations extends BaseTabActivity {
    private static final String TAG_MY_ORGS = "my_orgs";

    private String mTarget;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.organizations);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        final ImageView gravatar = (ImageView) findViewById(R.id.iv_orgs_gravatar);
        if (mTarget != null && !mTarget.equals("")) {
            gravatar.setImageBitmap(GravatarCache.getDipGravatar(mTarget, 38.0f, getResources()
                    .getDisplayMetrics().density));
            ((TextView) findViewById(R.id.tv_page_title)).setText(mTarget + "'s Organizations");

            gravatar.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    final Intent i = new Intent(Organizations.this, Profile.class);
                    i.putExtra("username", mTarget);
                    startActivity(i);
                }
            });

            Intent intent = new Intent(getApplicationContext(), MyOrgs.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_MY_ORGS)
                    .setIndicator(buildIndicator(R.string.my_orgs)).setContent(intent));
        }
        mTabHost.getTabWidget().setVisibility(View.GONE);
    }
}
