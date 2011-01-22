
package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
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

import com.flurry.android.FlurryAgent;

public class CreateIssue extends Activity {
    private GitHubAPI _gapi;

    private Intent m_intent;

    private SharedPreferences m_prefs;

    private SharedPreferences.Editor m_prefsEditor;

    private ProgressDialog m_progressDialog;

    private String m_targetRepo;

    private String m_targetUser;

    private Thread m_thread;

    private String m_token;

    private String m_username;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.create_issue);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_prefsEditor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");

        _gapi = new GitHubAPI();

        final Bundle extras = getIntent().getExtras();
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

        ((TextView) findViewById(R.id.tv_page_title)).setText("New Issue");

        ((Button) findViewById(R.id.btn_create_issue_submit))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(final View v) {
                        m_thread = new Thread(new Runnable() {
                            public void run() {
                                final String title = ((TextView) findViewById(R.id.et_create_issue_title))
                                        .getText().toString();
                                final String body = ((TextView) findViewById(R.id.et_create_issue_body))
                                        .getText().toString();
                                if (!title.equals("") && !body.equals("")) {
                                    final Response createResp = _gapi.issues.open(m_targetUser,
                                            m_targetRepo, title, body);
                                    if (createResp.statusCode == 200) {
                                        try {
                                            final JSONObject response = new JSONObject(
                                                    createResp.resp).getJSONObject("issue");
                                            final int number = response.getInt("number");
                                            final JSONObject issueJSON = new JSONObject(
                                                    _gapi.issues.issue(m_targetUser, m_targetRepo,
                                                            number).resp).getJSONObject("issue");
                                            m_intent = new Intent(CreateIssue.this,
                                                    SingleIssue.class);
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
                                        } catch (final JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(CreateIssue.this, "Error creating issue.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        m_progressDialog = ProgressDialog.show(CreateIssue.this, "Please Wait...",
                                "Creating issue...");
                        m_thread.start();
                    }
                });
    }

    @Override
    public void onPause() {
        if ((m_thread != null) && m_thread.isAlive()) {
            m_thread.stop();
        }
        if ((m_progressDialog != null) && m_progressDialog.isShowing()) {
            m_progressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("titleText")) {
            ((EditText) findViewById(R.id.et_create_issue_title)).setText(savedInstanceState
                    .getString("titleText"));
        }
        if (savedInstanceState.containsKey("bodyText")) {
            ((EditText) findViewById(R.id.et_create_issue_body)).setText(savedInstanceState
                    .getString("bodyText"));
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString("titleText",
                ((EditText) findViewById(R.id.et_create_issue_title)).getText().toString());
        savedInstanceState.putString("bodyText",
                ((EditText) findViewById(R.id.et_create_issue_body)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
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
