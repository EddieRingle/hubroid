/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.Followers;
import net.idlesoft.android.apps.github.activities.tabs.Following;
import net.idlesoft.android.apps.github.activities.tabs.Members;
import net.idlesoft.android.apps.github.activities.tabs.Teams;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Users extends BaseTabActivity {

    private static final String TAG_FOLLOWERS = "followers";

    private static final String TAG_FOLLOWING = "following";

    private static final String TAG_MEMBERS = "members";

    private static final String TAG_TEAMS = "teams";

    private String mTarget;

    private boolean mIsOrganization = false;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.users);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
            mIsOrganization = extras.getBoolean("isOrganization", false);
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        setupActionBar();

        final ImageView gravatar = (ImageView) findViewById(R.id.iv_users_gravatar);
        gravatar.setImageBitmap(GravatarCache.getDipGravatar(GravatarCache.getGravatarID(mTarget),
                38.0f, getResources().getDisplayMetrics().density));
        ((TextView) findViewById(R.id.tv_page_title)).setText(mTarget);

        gravatar.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                final Intent i = new Intent(Users.this, Profile.class);
                i.putExtra("username", mTarget);
                startActivity(i);
            }
        });

        if (mIsOrganization) {
            final Intent intent = new Intent(getApplicationContext(), Members.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_MEMBERS)
                    .setIndicator(buildIndicator(R.string.members)).setContent(intent));
            intent.setClass(getApplicationContext(), Teams.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_TEAMS)
                    .setIndicator(buildIndicator(R.string.teams)).setContent(intent));
        } else {
            final Intent intent = new Intent(getApplicationContext(), Followers.class);
            intent.putExtra("target", mTarget);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_FOLLOWERS)
                    .setIndicator(buildIndicator(R.string.followers)).setContent(intent));

            intent.setClass(getApplicationContext(), Following.class);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_FOLLOWING)
                    .setIndicator(buildIndicator(R.string.following)).setContent(intent));
        }
    }
}
