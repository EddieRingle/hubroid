/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import com.flurry.android.FlurryAgent;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.adapters.IssuesListAdapter;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;

public class IssuesList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    private IssuesListAdapter mClosedIssuesAdapter;

    private JSONArray mClosedIssuesData;

    private SharedPreferences.Editor mEditor;

    private Intent mIntent;

    private final OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            JSONObject json = null;
            try {
                if (parent.getId() == R.id.lv_issues_list_open_list) {
                    json = mOpenIssuesData.getJSONObject(position);
                } else {
                    json = mClosedIssuesData.getJSONObject(position);
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            final Intent intent = new Intent(getApplicationContext(), SingleIssue.class);
            intent.putExtra("repoOwner", mTargetUser);
            intent.putExtra("repoName", mTargetRepo);
            intent.putExtra("item_json", json.toString());
            startActivity(intent);
        }
    };

    private IssuesListAdapter mOpenIssuesAdapter;

    private JSONArray mOpenIssuesData;

    private int mPosition;

    private SharedPreferences mPrefs;

    private ProgressDialog mProgressDialog;

    private String mTargetRepo;

    private String mTargetUser;

    private Thread mThread;

    private String mPassword;

    private String mType;

    private String mUsername;

    private final OnClickListener onButtonToggleClickListener = new OnClickListener() {
        public void onClick(final View v) {
            if (v.getId() == R.id.btn_issues_list_open) {
                toggleList("open");
                mType = "open";
            } else if (v.getId() == R.id.btn_issues_list_closed) {
                toggleList("closed");
                mType = "closed";
            }
        }
    };

    private final Runnable threadProc_initializeList = new Runnable() {
        public void run() {
            initializeList();

            runOnUiThread(new Runnable() {
                public void run() {
                    if ((mOpenIssuesAdapter != null) && (mClosedIssuesAdapter != null)) {
                        final ListView openIssues = (ListView) findViewById(R.id.lv_issues_list_open_list);
                        final ListView closedIssues = (ListView) findViewById(R.id.lv_issues_list_closed_list);
                        openIssues.setAdapter(mOpenIssuesAdapter);
                        closedIssues.setAdapter(mClosedIssuesAdapter);
                        if (mType.equals("open")) {
                            toggleList("open");
                        }
                        if (mType.equals("closed")) {
                            toggleList("closed");
                        }
                    }
                    mProgressDialog.dismiss();
                }
            });
        }
    };

    public void initializeList() {
        JSONObject json = null;
        mOpenIssuesData = new JSONArray();
        mClosedIssuesData = new JSONArray();
        try {
            json = new JSONObject(mGapi.issues.list(mTargetUser, mTargetRepo, "open").resp);
            mOpenIssuesData = new JSONArray();
            for (int i = 0; !json.getJSONArray("issues").isNull(i); i++) {
                mOpenIssuesData.put(json.getJSONArray("issues").getJSONObject(i));
            }
            mOpenIssuesAdapter = new IssuesListAdapter(IssuesList.this, mOpenIssuesData);

            json = new JSONObject(mGapi.issues.list(mTargetUser, mTargetRepo, "closed").resp);
            mClosedIssuesData = new JSONArray();
            for (int i = 0; !json.getJSONArray("issues").isNull(i); i++) {
                mClosedIssuesData.put(json.getJSONArray("issues").getJSONObject(i));
            }
            mClosedIssuesAdapter = new IssuesListAdapter(IssuesList.this, mClosedIssuesData);
        } catch (final JSONException e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(IssuesList.this,
                            "Error gathering issue data, please try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            e.printStackTrace();
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        JSONObject json = null;
        try {
            if (mType.equals("open")) {
                json = mOpenIssuesData.getJSONObject(info.position);
            } else {
                json = mClosedIssuesData.getJSONObject(info.position);
            }
            switch (item.getItemId()) {
                case 0:
                    final Intent intent = new Intent(getApplicationContext(), SingleIssue.class);
                    intent.putExtra("repoOwner", mTargetUser);
                    intent.putExtra("repoName", mTargetRepo);
                    intent.putExtra("item_json", json.toString());
                    startActivity(intent);
                    return true;
                case 1:
                    if (mGapi.issues.close(mTargetUser, mTargetRepo, json.getInt("number")).statusCode == 200) {
                        mProgressDialog = ProgressDialog.show(IssuesList.this, "Please wait...",
                                "Refreshing Issue List...", true);
                        mThread = new Thread(null, threadProc_initializeList);
                        mThread.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error closing issue.",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case 2:
                    if (mGapi.issues.reopen(mTargetUser, mTargetRepo, json.getInt("number")).statusCode == 200) {
                        mProgressDialog = ProgressDialog.show(IssuesList.this, "Please wait...",
                                "Refreshing Issue List...", true);
                        mThread = new Thread(null, threadProc_initializeList);
                        mThread.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error reopening issue.",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.issues_list);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();
        mType = "open";

        mUsername = mPrefs.getString("login", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("owner")) {
                mTargetUser = extras.getString("owner");
            } else {
                mTargetUser = mUsername;
            }
            if (extras.containsKey("repository")) {
                mTargetRepo = extras.getString("repository");
            }
        } else {
            mTargetUser = mUsername;
        }

        mProgressDialog = ProgressDialog.show(IssuesList.this, "Please wait...",
                "Loading Issues...", true);
        mThread = new Thread(null, threadProc_initializeList);
        mThread.start();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final int id = v.getId();
        if (id == R.id.lv_issues_list_open_list) {
            menu.add(0, 0, 0, "View");
            if (mUsername.equals(mTargetUser)) {
                menu.add(0, 1, 0, "Close");
            }
        } else if (id == R.id.lv_issues_list_closed_list) {
            menu.add(0, 0, 0, "View");
            if (mUsername.equals(mTargetUser)) {
                menu.add(0, 2, 0, "Reopen");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case -1:
                intent = new Intent(this, CreateIssue.class);
                intent.putExtra("owner", mTargetUser);
                intent.putExtra("repository", mTargetRepo);
                startActivity(intent);
                return true;
            case 0:
                intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 1:
                mEditor.clear().commit();
                intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 2:
                final File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()) {
                    final File hubroid = new File(root, "hubroid");
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

    @Override
    public void onPause() {
        if ((mThread != null) && mThread.isAlive()) {
            mThread.stop();
        }
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            menu.add(0, -1, 0, "Create Issue").setIcon(android.R.drawable.ic_menu_add);
            menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
            menu.add(0, 1, 0, "Clear Preferences");
            menu.add(0, 2, 0, "Clear Cache");
        }
        return true;
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean keepGoing = true;
        mType = savedInstanceState.getString("type");
        try {
            if (savedInstanceState.containsKey("openIssues_json")) {
                mOpenIssuesData = new JSONArray(savedInstanceState.getString("openIssues_json"));
            } else {
                keepGoing = false;
            }
            if (savedInstanceState.containsKey("closedIssues_json")) {
                mClosedIssuesData = new JSONArray(savedInstanceState
                        .getString("closedIssues_json"));
            } else {
                keepGoing = false;
            }
        } catch (final JSONException e) {
            keepGoing = false;
        }
        if (keepGoing == true) {
            mOpenIssuesAdapter = new IssuesListAdapter(getApplicationContext(), mOpenIssuesData);
            mClosedIssuesAdapter = new IssuesListAdapter(getApplicationContext(),
                    mClosedIssuesData);
        } else {
            mOpenIssuesAdapter = null;
            mClosedIssuesAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListView openList = (ListView) findViewById(R.id.lv_issues_list_open_list);
        final ListView closedList = (ListView) findViewById(R.id.lv_issues_list_closed_list);

        openList.setAdapter(mOpenIssuesAdapter);
        closedList.setAdapter(mClosedIssuesAdapter);
        toggleList(mType);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("type", mType);
        if (mOpenIssuesData != null) {
            savedInstanceState.putString("openIssues_json", mOpenIssuesData.toString());
        }
        if (mClosedIssuesData != null) {
            savedInstanceState.putString("closedIssues_json", mClosedIssuesData.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");

        ((Button) findViewById(R.id.btn_issues_list_open))
                .setOnClickListener(onButtonToggleClickListener);
        ((Button) findViewById(R.id.btn_issues_list_closed))
                .setOnClickListener(onButtonToggleClickListener);

        ((ListView) findViewById(R.id.lv_issues_list_open_list))
                .setOnItemClickListener(mMessageClickedHandler);
        ((ListView) findViewById(R.id.lv_issues_list_closed_list))
                .setOnItemClickListener(mMessageClickedHandler);
        registerForContextMenu(findViewById(R.id.lv_issues_list_open_list));
        registerForContextMenu(findViewById(R.id.lv_issues_list_closed_list));
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public void toggleList(String type) {
        final ListView openList = (ListView) findViewById(R.id.lv_issues_list_open_list);
        final ListView closedList = (ListView) findViewById(R.id.lv_issues_list_closed_list);
        final TextView title = (TextView) findViewById(R.id.tv_page_title);

        if (type.equals("") || (type == null)) {
            type = (mType.equals("open")) ? "closed" : "public";
        }
        mType = type;

        if (mType.equals("open")) {
            openList.setVisibility(View.VISIBLE);
            closedList.setVisibility(View.GONE);
            title.setText("Open Issues");
        } else if (mType.equals("closed")) {
            closedList.setVisibility(View.VISIBLE);
            openList.setVisibility(View.GONE);
            title.setText("Closed Issues");
        }
    }
}
