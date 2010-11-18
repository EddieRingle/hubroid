/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import java.io.File;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class UserInfo extends Activity {
    public JSONObject m_jsonData;
    private SharedPreferences m_prefs;
    private SharedPreferences.Editor m_editor;
    public Intent m_intent;
    private String m_username;
    private String m_password;
    private String m_targetUser;
    private boolean m_isLoggedIn;
    private boolean m_isFollowing;
    private Thread m_thread;
    private ProgressDialog m_progressDialog;
    private Dialog m_loginDialog;
    private GitHubAPI _gapi;

    private OnClickListener onButtonClick = new OnClickListener() {
        public void onClick(View v) {
            Intent intent;
            // Figure out what button was clicked
            int id = v.getId();
            switch (id) {
            case R.id.btn_user_info_repositories:
                // Go to the user's list of repositories
                intent = new Intent(UserInfo.this, RepositoriesList.class);
                intent.putExtra("username", m_targetUser);
                startActivity(intent);
                break;
            case R.id.btn_user_info_followers_following:
                // Go to the Followers/Following screen
                intent = new Intent(UserInfo.this, FollowersFollowing.class);
                intent.putExtra("username", m_targetUser);
                startActivity(intent);
                break;
            default:
                // oh well...
                break;
            }
        }
    };

    public Dialog onCreateDialog(int id) {
        m_loginDialog = new Dialog(UserInfo.this);
        m_loginDialog.setCancelable(true);
        m_loginDialog.setTitle("Login");
        m_loginDialog.setContentView(R.layout.login_dialog);
        Button loginBtn = (Button) m_loginDialog.findViewById(R.id.btn_loginDialog_login);
        loginBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                m_progressDialog = ProgressDialog.show(UserInfo.this, null, "Logging in...");
                m_thread = new Thread(new Runnable() {
                    public void run() {
                        String username = ((EditText) m_loginDialog
                                .findViewById(R.id.et_loginDialog_userField)).getText().toString();
                        String token = ((EditText) m_loginDialog
                                .findViewById(R.id.et_loginDialog_tokenField)).getText().toString();

                        if (username.equals("") || token.equals("")) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    m_progressDialog.dismiss();
                                    Toast.makeText(UserInfo.this, "Login details cannot be blank",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Response authResp = _gapi.user.info(username);

                            if (authResp.statusCode == 401) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        m_progressDialog.dismiss();
                                        Toast.makeText(UserInfo.this,
                                                "Error authenticating with server",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (authResp.statusCode == 200) {
                                m_editor.putString("login", username);
                                m_editor.putString("token", token);
                                m_editor.putBoolean("isLoggedIn", true);
                                m_editor.commit();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        m_progressDialog.dismiss();
                                        dismissDialog(0);
                                        Intent intent = new Intent(UserInfo.this, Hubroid.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                    }
                });
                m_thread.start();
            }
        });
        return m_loginDialog;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.hasVisibleItems())
            menu.clear();
        if (m_targetUser != m_username) {
            if (m_isFollowing) {
                menu.add(0, 3, 0, "Unfollow");
            } else {
                menu.add(0, 3, 0, "Follow");
            }
        }
        if (!m_isLoggedIn)
            menu.add(0, 0, 0, "Login");
        else if (m_isLoggedIn)
            menu.add(0, 1, 0, "Logout");
        menu.add(0, 2, 0, "Clear Cache");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 3:
            Response postResp;

            if (m_isFollowing) {
                postResp = _gapi.user.unfollow(m_targetUser);
                if (postResp.statusCode == 200) {
                    Toast.makeText(this, "You are no longer following " + m_targetUser + ".",
                            Toast.LENGTH_SHORT).show();
                }
                m_isFollowing = !m_isFollowing;
            } else {
                postResp = _gapi.user.follow(m_targetUser);
                if (postResp.statusCode == 200) {
                    Toast.makeText(this, "You are now following " + m_targetUser + ".",
                            Toast.LENGTH_SHORT).show();
                }
                m_isFollowing = !m_isFollowing;
            }
            break;
        case 0:
            showDialog(0);
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

    public void navBarOnClickSetup() {
        ((LinearLayout) findViewById(R.id.ll_user_info_navbar)).setVisibility(View.VISIBLE);
        if (m_targetUser.equals(m_username)) {
            ScrollView sv = (ScrollView) findViewById(R.id.sv_userInfo);
            RelativeLayout.LayoutParams svParams = new RelativeLayout.LayoutParams(
                    sv.getLayoutParams());
            svParams.setMargins(0, 0, 0, (int) (72.0f * getApplicationContext().getResources()
                    .getDisplayMetrics().density + 0.5f));
            sv.setLayoutParams(svParams);
        }
        ((Button) findViewById(R.id.btn_navbar_repositories))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(UserInfo.this, RepositoriesList.class));
                        finish();
                    }
                });
        ((Button) findViewById(R.id.btn_navbar_profile)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(UserInfo.this, UserInfo.class));
                finish();
            }
        });
        ((Button) findViewById(R.id.btn_navbar_search)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(UserInfo.this, Search.class));
                finish();
            }
        });

        ((Button) findViewById(R.id.btn_navbar_profile)).setEnabled(false);
        if (!m_isLoggedIn) {
            ((Button) findViewById(R.id.btn_navbar_profile)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.btn_navbar_repositories)).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.user_info);

        m_prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        m_editor = m_prefs.edit();

        m_username = m_prefs.getString("login", "");
        m_password = m_prefs.getString("token", "");
        m_isFollowing = false;
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

        _gapi = new GitHubAPI();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            m_targetUser = extras.getString("username");
        try {
            if (m_targetUser == null || m_targetUser.equals("")) {
                m_targetUser = m_username;
                navBarOnClickSetup();
            }
            Response userInfoResp;
            userInfoResp = (m_targetUser.equals(m_username)) ? _gapi.user.info(m_targetUser)
                    : _gapi.user.info(m_targetUser);
            JSONObject json = null;
            if (userInfoResp.statusCode == 200)
                json = new JSONObject(userInfoResp.resp);
            if (json == null) {
                // User doesn't really exist, return to the previous activity
                this.setResult(5005);
                this.finish();
            } else {
                m_jsonData = json.getJSONObject("user");

                JSONArray following_list = new JSONObject(_gapi.user.following(m_username).resp)
                        .getJSONArray("users");
                int length = following_list.length() - 1;
                for (int i = 0; i <= length; i++) {
                    if (following_list.getString(i).equalsIgnoreCase(m_targetUser)) {
                        m_isFollowing = true;
                    }
                }

                String company, location, full_name, email, blog;

                // Replace empty values with "N/A"
                if (m_jsonData.has("company") && !m_jsonData.getString("company").equals("null")
                        && !m_jsonData.getString("company").equals("")) {
                    company = m_jsonData.getString("company");
                } else {
                    company = "N/A";
                }
                if (m_jsonData.has("location") && !m_jsonData.getString("location").equals("null")
                        && !m_jsonData.getString("location").equals("")) {
                    location = m_jsonData.getString("location");
                } else {
                    location = "N/A";
                }
                if (m_jsonData.has("name") && !m_jsonData.getString("name").equals("null")) {
                    full_name = m_jsonData.getString("name");
                } else {
                    full_name = "N/A";
                }
                if (m_jsonData.has("email") && !m_jsonData.getString("email").equals("null")
                        && !m_jsonData.getString("email").equals("")) {
                    email = m_jsonData.getString("email");
                } else {
                    email = "N/A";
                }
                if (m_jsonData.has("blog") && !m_jsonData.getString("blog").equals("null")
                        && !m_jsonData.getString("blog").equals("")) {
                    blog = m_jsonData.getString("blog");
                } else {
                    blog = "N/A";
                }

                // Set all the values in the layout
                // ((TextView)findViewById(R.id.tv_top_bar_title)).setText(m_targetUser);
                ((ImageView) findViewById(R.id.iv_user_info_gravatar)).setImageBitmap(Hubroid
                        .getGravatar(Hubroid.getGravatarID(m_targetUser), 50));
                ((TextView) findViewById(R.id.tv_user_info_full_name)).setText(full_name);
                ((TextView) findViewById(R.id.tv_user_info_company)).setText(company);
                ((TextView) findViewById(R.id.tv_user_info_email)).setText(email);
                ((TextView) findViewById(R.id.tv_user_info_location)).setText(location);
                ((TextView) findViewById(R.id.tv_user_info_blog)).setText(blog);

                // Make the buttons work
                Button activityBtn = (Button) findViewById(R.id.btn_user_info_public_activity);
                Button repositoriesBtn = (Button) findViewById(R.id.btn_user_info_repositories);
                Button followersFollowingBtn = (Button) findViewById(R.id.btn_user_info_followers_following);

                activityBtn.setOnClickListener(onButtonClick);
                repositoriesBtn.setOnClickListener(onButtonClick);
                followersFollowingBtn.setOnClickListener(onButtonClick);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        if (m_thread != null && m_thread.isAlive())
            m_thread.stop();
        if (m_progressDialog != null && m_progressDialog.isShowing())
            m_progressDialog.dismiss();
        super.onPause();
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
