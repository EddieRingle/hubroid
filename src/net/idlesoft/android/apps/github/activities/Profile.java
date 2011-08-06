/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import java.io.IOException;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends BaseActivity {
    public JSONObject mJson;

    public JSONArray mJsonFollowing;

    private String mTarget;

    private Bitmap mGravatar;

    private User mUser = null;

    protected boolean IsNotNullNorEmpty(final String subject)
    {
    	if (subject != null && !subject.equals("")) {
    		return true;
    	} else {
    		return false;
    	}
    }

    protected JSONObject buildListItem(final String title, final String content)
            throws JSONException {
        return new JSONObject().put("title", title).put("content", content);
    }

    protected void buildUI()
    {
    	if (mUser != null) {
    		((ImageView) findViewById(R.id.iv_profile_gravatar)).setImageBitmap(mGravatar);
    		((TextView) findViewById(R.id.tv_profile_username)).setText(mTarget);

	        final JSONArray listItems = new JSONArray();
	        try {
	        	if (IsNotNullNorEmpty(mUser.getName())) {
	        		listItems.put(buildListItem("Name", mUser.getName()));
	        	}
	        	if (IsNotNullNorEmpty(mUser.getCompany())) {
	        		listItems.put(buildListItem("Company", mUser.getCompany()));
	        	}
	        	if (IsNotNullNorEmpty(mUser.getEmail())) {
	        		listItems.put(buildListItem("Email", mUser.getEmail()));
	        	}
	        	if (IsNotNullNorEmpty(mUser.getBlog())) {
	        		listItems.put(buildListItem("Blog", mUser.getBlog()));
	        	}
	        	if (IsNotNullNorEmpty(mUser.getLocation())) {
	        		listItems.put(buildListItem("Location", mUser.getLocation()));
	        	}
	        	listItems.put(buildListItem("Public Activity", "View " + mTarget + "'s public activity"));
	        	listItems.put(buildListItem("Repositories",
	        			Integer.toString(mUser.getPublicRepos()
	        					+ mUser.getTotalPrivateRepos())));
	        	if (IsNotNullNorEmpty(mUser.getType())) {
	        		if (mUser.getType().equalsIgnoreCase("Organization")) {
	        			listItems.put(buildListItem("Members & Teams", "Find out who makes this organization tick"));
	        		}
	        	}
	        	listItems.put(buildListItem("Followers / Following",
	        			Integer.toString(mUser.getFollowers()) + " / "
	        			+ Integer.toString(mUser.getFollowing())));
	        	listItems.put(buildListItem("Gists",
	        			Integer.toString(mUser.getPublicGists()
	        					+ mUser.getPrivateGists())));
	        	if (IsNotNullNorEmpty(mUser.getCreatedAt().toString())) {
	        		listItems.put(buildListItem("Join Date",
	        				mUser.getCreatedAt().toString()));
	        	}
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	
	        final ListView infoList = (ListView) findViewById(R.id.lv_profile_info);
	        final InfoListAdapter adapter = new InfoListAdapter(this, infoList);
	        adapter.loadData(listItems);
	        adapter.pushData();
	        infoList.setAdapter(adapter);
	
	        infoList.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(final AdapterView<?> parent, final View v,
	                    final int position, final long id) {
	                try {
	                    final String title = ((JSONObject) listItems.get(position))
	                            .getString("title");
	                    String content = ((JSONObject) listItems.get(position))
	                            .getString("content");
	                    final Intent intent;
	
	                    if (title.equals("Email")) {
	                    	intent = new Intent(android.content.Intent.ACTION_SEND);
	                    	intent.setType("text/plain");
	                    	intent.putExtra(android.content.Intent.EXTRA_EMAIL, content);
	                    } else if (title.equals("Blog")) {
	                    	if (content.indexOf("://") == -1) {
	                    		content = "http://" + content;
	                    	}
	                        intent = new Intent("android.intent.action.VIEW", Uri.parse(content));
	                    } else if (title.equals("Public Activity")) {
	                    	intent = new Intent(Profile.this, NewsFeed.class);
	                    	intent.putExtra("username", mTarget);
	                	} else if (title.equals("Repositories")) {
	                    	intent = new Intent(Profile.this, Repositories.class);
	                    	intent.putExtra("target", mTarget);
	                	} else if (title.equals("Members & Teams")) {
	                		intent = new Intent(Profile.this, Users.class);
	                		intent.putExtra("target", mTarget);
	                		intent.putExtra("isOrganization", true);
	                    } else if (title.equals("Followers / Following")) {
	                    	intent = new Intent(Profile.this, Users.class);
	                    	intent.putExtra("target", mTarget);
	                    } else if (title.equals("Gists")) {
	                    	intent = new Intent(Profile.this, Gists.class);
	                    	intent.putExtra("target", mTarget);
	                    } else {
	                    	return;
	                    }
	                    startActivity(intent);
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	            }
	        });
    	}
    }

    private static class LoadProfileTask extends AsyncTask<Void, Void, Boolean> {
        public Profile activity;

        protected void onPreExecute() {
        	super.onPreExecute();
        	activity.getActionBar().setProgressBarVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(Void... params) {
        	UserService service = new UserService(activity.getGitHubClient());
        	try {
        		if (activity.mTarget.equalsIgnoreCase(activity.mUsername)) {
        			activity.mUser = service.getUser();
        		} else {
        			activity.mUser = service.getUser(activity.mTarget);
        		}
        		activity.mGravatar = GravatarCache.getDipGravatar(
        				GravatarCache.getGravatarID(activity.mTarget), 50.0f,
        				activity.getResources().getDisplayMetrics().density);
        		return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                activity.buildUI();
                activity.getActionBar().setProgressBarVisibility(View.GONE);
            } else {
                Toast.makeText(activity, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            super.onPostExecute(result);
        }
    }

    private LoadProfileTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.profile);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("username");
        }

        if (mTarget == null) {
            mTarget = mUsername;
        }

        ((TextView) findViewById(R.id.tv_profile_username)).setText(mTarget);
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
}
