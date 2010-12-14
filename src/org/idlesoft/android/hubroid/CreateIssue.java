package org.idlesoft.android.hubroid;

import com.flurry.android.FlurryAgent;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.Issues;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateIssue extends Activity {
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_prefsEditor;
	private ProgressDialog m_progressDialog;
	private Thread m_thread;
	private Intent m_intent;
	private String m_username;
	private String m_password;
	private String m_targetUser;
	private String m_targetRepo;
	private GitHubAPI mGapi = new GitHubAPI();

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.create_issue);

		m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
		m_prefsEditor = m_prefs.edit();

		m_username = m_prefs.getString("login", "");
		m_password = m_prefs.getString("password", "");
		mGapi.authenticate(m_username, m_password);

		Bundle extras = getIntent().getExtras();
        if (extras != null) {
	        if (extras.containsKey("owner")) {
	        	m_targetUser = extras.getString("owner");
	        } else {
	        	m_targetUser = m_username;
	        }
	        if (extras.containsKey("repository")) {
	        	m_targetRepo = extras.getString("repository");
	        }
        } else {
        	m_targetUser = m_username;
        }

		((TextView)findViewById(R.id.tv_top_bar_title)).setText("New Issue");

		((Button)findViewById(R.id.btn_create_issue_submit)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_thread = new Thread(new Runnable() {
					public void run() {
						String title = ((TextView)findViewById(R.id.et_create_issue_title)).getText().toString();
						String body = ((TextView)findViewById(R.id.et_create_issue_body)).getText().toString();
						if (!title.equals("") && !body.equals("")) {
							Response createResp = mGapi.issues.open(m_targetUser, m_targetRepo, title, body);
							if (createResp.statusCode == 200) {
								try {
									JSONObject response = new JSONObject(createResp.resp).getJSONObject("issue");
									int number = response.getInt("number");
									JSONObject issueJSON = new JSONObject(mGapi.issues.issue(m_targetUser, m_targetRepo, number).resp).getJSONObject("issue");
									m_intent = new Intent(CreateIssue.this, SingleIssue.class);
									m_intent.putExtra("repoOwner", m_targetUser);
									m_intent.putExtra("repoName", m_targetRepo);
									m_intent.putExtra("item_json", issueJSON.toString());

									runOnUiThread(new Runnable() {
										public void run() {
											m_progressDialog.dismiss();
											startActivity(m_intent);
											finish();
										}
									});									
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								Toast.makeText(CreateIssue.this, "Error creating issue.", Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
				m_progressDialog = ProgressDialog.show(CreateIssue.this, "Please Wait...", "Creating issue...");
				m_thread.start();
			}
		});
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("titleText", ((EditText)findViewById(R.id.et_create_issue_title)).getText().toString());
		savedInstanceState.putString("bodyText", ((EditText)findViewById(R.id.et_create_issue_body)).getText().toString());
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    		if (savedInstanceState.containsKey("titleText")) {
    			((EditText)findViewById(R.id.et_create_issue_title)).setText(savedInstanceState.getString("titleText"));
    		}
    		if (savedInstanceState.containsKey("bodyText")) {
    			((EditText)findViewById(R.id.et_create_issue_body)).setText(savedInstanceState.getString("bodyText"));
    		}
    }
}
