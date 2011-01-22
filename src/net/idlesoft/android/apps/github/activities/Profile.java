/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class Profile extends Activity {
    private GitHubAPI mGapi;

    private boolean mIsFollowing;

    private boolean mIsLoggedIn;

    public JSONObject mJson;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mTarget;

    private String mUsername;

    private final OnClickListener onButtonClick = new OnClickListener() {
        public void onClick(final View v) {
            final Intent intent;
            /* Figure out what button was clicked */
            final int id = v.getId();
            switch (id) {
                case R.id.btn_user_info_public_activity:
                    /* Go to the user's public activity feed */
                    intent = new Intent(Profile.this, NewsFeed.class);
                    intent.putExtra("username", mTarget);
                    startActivity(intent);
                    break;
                case R.id.btn_user_info_repositories:
                    /* Go to the user's list of repositories */
                    intent = new Intent(Profile.this, Repositories.class);
                    intent.putExtra("target", mTarget);
                    startActivity(intent);
                    break;
                case R.id.btn_user_info_followers_following:
                    /* Go to the Followers/Following screen */
                    intent = new Intent(Profile.this, Users.class);
                    intent.putExtra("target", mTarget);
                    startActivity(intent);
                    break;
                default:
                    /* oh well... */
                    break;
            }
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.profile);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");
        mIsFollowing = false;

        mGapi = new GitHubAPI();
        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("username");
        }
        try {
            if ((mTarget == null) || mTarget.equals("")) {
                mTarget = mUsername;
            }
            Response userInfoResp;
            userInfoResp = mGapi.user.info(mTarget);
            if (userInfoResp.statusCode == 200) {
                mJson = new JSONObject(userInfoResp.resp);
            }
            if (mJson == null) {
                // User doesn't really exist, return to the previous activity
                this.setResult(5005);
                finish();
            } else {
                mJson = mJson.getJSONObject("user");

                final JSONArray following_list = new JSONObject(
                        mGapi.user.following(mUsername).resp).getJSONArray("users");
                final int length = following_list.length() - 1;
                for (int i = 0; i <= length; i++) {
                    if (following_list.getString(i).equalsIgnoreCase(mTarget)) {
                        mIsFollowing = true;
                    }
                }

                String company, location, full_name, email, blog;

                // Replace empty values with "N/A"
                if (mJson.has("company") && !mJson.getString("company").equals("null")
                        && !mJson.getString("company").equals("")) {
                    company = mJson.getString("company");
                } else {
                    company = "N/A";
                }
                if (mJson.has("location") && !mJson.getString("location").equals("null")
                        && !mJson.getString("location").equals("")) {
                    location = mJson.getString("location");
                } else {
                    location = "N/A";
                }
                if (mJson.has("name") && !mJson.getString("name").equals("null")) {
                    full_name = mJson.getString("name");
                } else {
                    full_name = mTarget;
                }
                if (mJson.has("email") && !mJson.getString("email").equals("null")
                        && !mJson.getString("email").equals("")) {
                    email = mJson.getString("email");
                } else {
                    email = "N/A";
                }
                if (mJson.has("blog") && !mJson.getString("blog").equals("null")
                        && !mJson.getString("blog").equals("")) {
                    blog = mJson.getString("blog");
                } else {
                    blog = "N/A";
                }

                // Set all the values in the layout
                // ((TextView)findViewById(R.id.tv_top_bar_title)).setText(m_targetUser);
                ((ImageView) findViewById(R.id.iv_user_info_gravatar)).setImageBitmap(GravatarCache
                        .getDipGravatar(GravatarCache.getGravatarID(mTarget), 50.0f, getResources()
                                .getDisplayMetrics().density));
                ((TextView) findViewById(R.id.tv_user_info_full_name)).setText(full_name);
                ((TextView) findViewById(R.id.tv_user_info_company)).setText(company);
                ((TextView) findViewById(R.id.tv_user_info_email)).setText(email);
                ((TextView) findViewById(R.id.tv_user_info_location)).setText(location);
                ((TextView) findViewById(R.id.tv_user_info_blog)).setText(blog);

                // Make the buttons work
                final Button activityBtn = (Button) findViewById(R.id.btn_user_info_public_activity);
                final Button repositoriesBtn = (Button) findViewById(R.id.btn_user_info_repositories);
                final Button followersFollowingBtn = (Button) findViewById(R.id.btn_user_info_followers_following);

                activityBtn.setOnClickListener(onButtonClick);
                repositoriesBtn.setOnClickListener(onButtonClick);
                followersFollowingBtn.setOnClickListener(onButtonClick);
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
