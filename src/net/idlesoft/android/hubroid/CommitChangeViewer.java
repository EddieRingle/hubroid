/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.hubroid;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class CommitChangeViewer extends Activity {
	public CommitListAdapter m_commitListAdapter;
	public ArrayAdapter<String> m_branchesAdapter;
	public ArrayList<String> m_branches;
	public ProgressDialog m_progressDialog;
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	public JSONArray m_commitData;
	public String m_repo_owner;
	public String m_repo_name;
	private String m_username;
	private String m_token;
	private String m_id;
	public Intent m_intent;
	public int m_position;
	private Thread m_thread;
	private GitHubAPI _gapi;

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
			menu.add(0, 1, 0, "Clear Preferences");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	/**
	 * Get the Gravatars of all users in the commit log 
	 */
	public Bitmap loadGravatarByLoginName(String login)
	{
		if (!login.equals("")) {
			String id = Hubroid.getGravatarID(login);
			return Hubroid.getGravatar(id, 30);
		} else {
			return null;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent i1 = new Intent(this, Hubroid.class);
			startActivity(i1);
			return true;
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(this, Hubroid.class);
			startActivity(intent);
        	return true;
		case 2:
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()) {
				File hubroid = new File(root, "hubroid");
				if (!hubroid.exists() && !hubroid.isDirectory()) {
					return true;
				} else {
					hubroid.delete();
					return true;
				}
			}
		}
		return false;
	}
 
	public String getHumanDate(Date current_time, Date commit_time){
		String end;
		long ms = current_time.getTime() - commit_time.getTime();
		long sec = ms / 1000;
		long min = sec / 60;
		long hour = min / 60;
		long day = hour / 24;
		if (day > 0) {
			if (day == 1) {
				end = " day ago";
			} else {
				end = " days ago";
			}
			return day + end;
		} else if (hour > 0) {
			if (hour == 1) {
				end = " hour ago";
			} else {
				end = " hours ago";
			}
			return hour + end;
		} else if (min > 0) {
			if (min == 1) {
				end = " minute ago";
			} else {
				end = " minutes ago";
			}
			return min + end;
		} else {
			if (sec == 1) {
				end = " second ago";
			} else {
				end = " seconds ago";
			}
			return sec + end;
		}
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.commit_view);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        View header = getLayoutInflater().inflate(R.layout.commit_view_header, null);

        _gapi = new GitHubAPI();

        TextView title = (TextView) findViewById(R.id.tv_top_bar_title);
        title.setText("Commit Viewer");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	m_repo_name = extras.getString("repo_name");
        	m_repo_owner = extras.getString("username");
        	m_id = extras.getString("id");

        	// Get the commit data for that commit ID so that we can get the tree ID and filename.
        	try {
        		Response commitInfo = _gapi.commits.commit(m_repo_owner, m_repo_name, m_id);
        		JSONObject commitJSON = new JSONObject(commitInfo.resp).getJSONObject("commit");
        		
        		// Display the committer and author
        		JSONObject authorInfo = commitJSON.getJSONObject("author");
        		String authorName = authorInfo.getString("login");

        		Bitmap authorGravatar = loadGravatarByLoginName(authorName);

        		JSONObject committerInfo = commitJSON.getJSONObject("committer");
        		String committerName = committerInfo.getString("login");

        		// If the committer is the author then just show them as the author, otherwise show both people
        		((TextView) header.findViewById(R.id.commit_view_author_name)).setText(authorName);
        		
        		if(authorGravatar != null)
        			((ImageView) header.findViewById(R.id.commit_view_author_gravatar)).setImageBitmap(authorGravatar);

        		// Set the commit message
        		((TextView) header.findViewById(R.id.commit_view_message)).setText(commitJSON.getString("message"));

    			SimpleDateFormat dateFormat = new SimpleDateFormat(Hubroid.GITHUB_TIME_FORMAT);
    			Date commit_time;
    			Date current_time;
    			String authorDate = "";

    			try {
    				commit_time = dateFormat.parse(commitJSON.getString("authored_date"));
    				current_time = dateFormat.parse(dateFormat.format(new Date()));
    				((TextView) header.findViewById(R.id.commit_view_author_time)).setText(getHumanDate(current_time, commit_time));
    				
    				commit_time = dateFormat.parse(commitJSON.getString("committed_date"));
    				authorDate = getHumanDate(current_time, commit_time);
    				
    				
    			} catch (ParseException e) {
    				e.printStackTrace();
    			}

        		if(!authorName.equals(committerName)){
        			// They are not the same person, make the author visible and fill in the details
        			((LinearLayout) header.findViewById(R.id.commit_view_author_layout)).setVisibility(View.VISIBLE);
        			((TextView) header.findViewById(R.id.commit_view_committer_name)).setText(committerName);
        			((TextView) header.findViewById(R.id.commit_view_committer_time)).setText(authorDate);
        			Bitmap committerGravatar = loadGravatarByLoginName(committerName);
        			if(committerGravatar != null)
        				((ImageView) header.findViewById(R.id.commit_view_committer_gravatar)).setImageBitmap(committerGravatar);
        		}

        		// Populate the ListView with the files that have changed 
        		JSONArray changesJSON = commitJSON.getJSONArray("modified");
        		CommitChangeViewerDiffAdapter diffs = new CommitChangeViewerDiffAdapter(getApplicationContext(), changesJSON);
        		ListView diffList = (ListView)findViewById(R.id.commit_view_diffs_list);
        		diffList.addHeaderView(header);
        		diffList.setAdapter(diffs);
        	} catch(JSONException e) {
        		e.printStackTrace();
        	}
        }
    }

	@Override
    public void onPause()
    {
    	if (m_thread != null && m_thread.isAlive())
    		m_thread.stop();
    	if (m_progressDialog != null && m_progressDialog.isShowing())
    		m_progressDialog.dismiss();
    	super.onPause();
    }

	@Override
    public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
}