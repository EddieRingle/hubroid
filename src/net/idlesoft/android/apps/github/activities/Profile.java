/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Profile extends BaseActivity {
    public JSONObject mJson;

    public JSONArray mJsonFollowing;

    private String mTarget;

    private Bitmap mGravatar;

    private static class LoadProfileTask extends AsyncTask<Void, Void, Void> {
    	public Profile activity;

    	protected void onPreExecute() {
    		((RelativeLayout) activity.findViewById(R.id.rl_profile_progress)).setVisibility(View.VISIBLE);
    		((ScrollView) activity.findViewById(R.id.sv_userInfo)).setVisibility(View.GONE);
    	}

    	protected Void doInBackground(Void... params) {
    		try {
	    		Response r = activity.mGApi.user.info(activity.mTarget);
	    		if (r.statusCode == 200) {
	    			activity.mJson = new JSONObject(r.resp);
	    			Response fResp = activity.mGApi.user.following(activity.mUsername);
	    			if (fResp.statusCode == 200) {
	    				activity.mJsonFollowing = new JSONObject(fResp.resp).getJSONArray("users");
	    			}
	    			activity.mGravatar = GravatarCache.getDipGravatar(GravatarCache.
	    					getGravatarID(activity.mTarget), 50.0f,
	    					activity.getResources().getDisplayMetrics().density);
	    		}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		return null;
    	}

    	protected void onPostExecute(Void result) {
    		activity.loadInfo();
    		((RelativeLayout) activity.findViewById(R.id.rl_profile_progress)).setVisibility(View.GONE);
    		((ScrollView) activity.findViewById(R.id.sv_userInfo)).setVisibility(View.VISIBLE);
    	}
    }

    private LoadProfileTask mTask;

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

    public void loadInfo() {
    	try {
            if (mJson == null) {
                // User doesn't really exist, return to the previous activity
                this.setResult(5005);
                finish();
            } else {
                mJson = mJson.getJSONObject("user");

                final int length = mJsonFollowing.length() - 1;
                for (int i = 0; i <= length; i++) {
                    if (mJsonFollowing.getString(i).equalsIgnoreCase(mTarget)) {
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
                ((ImageView) findViewById(R.id.iv_user_info_gravatar)).setImageBitmap(mGravatar);
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
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.profile);

        HubroidApplication.setupActionBar(Profile.this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("username");
        }

        if (mTarget == null) {
        	mTarget = mUsername;
        }

        mTask = (LoadProfileTask) getLastNonConfigurationInstance();
        if (mTask == null) {
        	mTask = new LoadProfileTask();
        }
        mTask.activity = Profile.this;

        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
        	mTask.execute();
        }
    }

    public Object onRetainNonConfigurationInstance() {
    	return mTask;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                mPrefsEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }
}