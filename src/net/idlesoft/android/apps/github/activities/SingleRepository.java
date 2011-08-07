/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;

import java.io.IOException;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.InfoListAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SingleRepository extends BaseActivity {
	private Repository mRepository = null;

    private static final String GIT_CLONE_INTENT = "org.openintents.git.clone.PREPARE";

    private static class LoadRepositoryTask extends AsyncTask<Void, Void, Void> {
        public SingleRepository activity;

        @Override
        protected Void doInBackground(final Void... params) {
        	final RepositoryService rs = new RepositoryService(activity.getGitHubClient());
            try {
            	activity.mRepository = rs.getRepository(
            			activity.mRepositoryOwner, activity.mRepositoryName);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            if (activity.mRepository != null) {
                activity.buildUI();
                activity.getActionBar().setProgressBarVisibility(View.GONE);
            } else {
                Toast.makeText(activity, "Repository no longer exists.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }

        @Override
        protected void onPreExecute() {
        	activity.getActionBar().setProgressBarVisibility(View.VISIBLE);
        }

    }

    public Intent mIntent;

    private LoadRepositoryTask mLoadRepositoryTask;

    private String mRepositoryName;

    private String mRepositoryOwner;

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
    	if (mRepository != null) {
    		if (!mRepository.isPrivate()) {
    			((ImageView) findViewById(R.id.iv_repository_visibility)).setImageResource(R.drawable.opensource);
    		} else {
    			((ImageView) findViewById(R.id.iv_repository_visibility)).setImageResource(R.drawable.lock);
    		}
    		((TextView) findViewById(R.id.tv_repository_name)).setText(mRepository.getName());

	        final JSONArray listItems = new JSONArray();
	        try {
	        	if (IsNotNullNorEmpty(mRepository.getOwner().getLogin())) {
	        		listItems.put(buildListItem("Owner", mRepository.getOwner().getLogin()));
	        	}
	        	if (IsNotNullNorEmpty(mRepository.getDescription())) {
	        		listItems.put(buildListItem("Description", mRepository.getDescription()));
	        	}
	        	if (mRepository.isFork() && mRepository.getParent() != null) {
	        		listItems.put(buildListItem("Parent Repository", "This repository is a fork of " + mRepository.getParent().getOwner().getLogin() + "'s repository"));
	        	}
	        	listItems.put(buildListItem("Branches", "Master branch is " + mRepository.getMasterBranch()));
	        	if (mRepository.isHasIssues()) {
	        		listItems.put(buildListItem("Issues", mRepository.getOpenIssues() + " open issues"));
	        	}
	        	listItems.put(buildListItem("Forks", Integer.toString(mRepository.getForks())));
	        	listItems.put(buildListItem("Watchers", Integer.toString(mRepository.getWatchers())));
	        	if (isIntentAvailable(this, GIT_CLONE_INTENT)) {
	        		listItems.put(buildListItem("Clone", "Clone this repository with Agit"));
	        	}
	        	if (IsNotNullNorEmpty(mRepository.getCreatedAt().toString())) {
	        		listItems.put(buildListItem("Creation Date",
	        				mRepository.getCreatedAt().toString()));
	        	}
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	
	        final ListView infoList = (ListView) findViewById(R.id.lv_repository_info);
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
	
	                    if (title.equals("Owner")) {
	                    	intent = new Intent(SingleRepository.this, Profile.class);
	                    	intent.putExtra("username", content);
	                    } else if (title.equals("Parent Repository")) {
	                    	intent = new Intent(SingleRepository.this, SingleRepository.class);
	                    	intent.putExtra("repo_owner", mRepository.getParent().getOwner().getLogin());
	                    	intent.putExtra("repo_name", mRepository.getParent().getName());
	                    } else if (title.equals("Branches")) {
	                    	intent = new Intent(SingleRepository.this, BranchesList.class);
	                    	intent.putExtra("repo_owner", mRepository.getOwner().getLogin());
	                    	intent.putExtra("repo_name", mRepository.getName());
	                    } else if (title.equals("Issues")) {
	                    	intent = new Intent(SingleRepository.this, Issues.class);
	                    	intent.putExtra("repo_owner", mRepository.getOwner().getLogin());
	                    	intent.putExtra("repo_name", mRepository.getName());
	                    } else if (title.equals("Clone")) {
	                        intent = new Intent(GIT_CLONE_INTENT);
	                        intent.putExtra("source-uri", mRepository.getCloneUrl());
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
    
    /**
     * Indicates whether the specified action can be used as an intent.
     * 
     * Adapted from http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     */
    public static boolean isIntentAvailable(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(action);
        return !packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY).isEmpty();
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.repository);

        setupActionBar("Repository");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("repo_owner");

            mLoadRepositoryTask = (LoadRepositoryTask) getLastNonConfigurationInstance();
            if (mLoadRepositoryTask == null) {
                mLoadRepositoryTask = new LoadRepositoryTask();
            }
            mLoadRepositoryTask.activity = SingleRepository.this;
            if (mLoadRepositoryTask.getStatus() == AsyncTask.Status.PENDING) {
                mLoadRepositoryTask.execute();
            }
        }
    }
}
