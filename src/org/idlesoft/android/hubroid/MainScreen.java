package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainScreen extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_username;
	private String m_token;
	public ListView m_menuList;
	public JSONObject m_userData;
	public ProgressDialog m_progressDialog;
	public boolean m_isLoggedIn;

	public static final String[] MAIN_MENU = new String[] {
		"Watched Repos",
		"Followers/Following",
		"Activity Feeds",
		"Repositories",
		"Search",
		"Profile"
	};

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (m_isLoggedIn) {
			if (!menu.hasVisibleItems()) {
				menu.add(0, 1, 0, "Logout");
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			m_isLoggedIn = false;
			m_editor.putBoolean("isLoggedIn", false).commit();
			Intent intent = new Intent(MainScreen.this, Hubroid.class);
			startActivity(intent);
        	return true;
		}
		return false;
	}

	private OnItemClickListener onMenuItemSelected = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> pV, View v, int pos, long id) {
			Intent intent;
			switch(pos) {
			case 0:
				intent = new Intent(MainScreen.this, WatchedRepositories.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(MainScreen.this, FollowersFollowing.class);
				startActivity(intent);
				break;
			case 2:
				Toast.makeText(MainScreen.this, "Activity Feeds", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(MainScreen.this, "Repositories", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(MainScreen.this, "Search", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(MainScreen.this, "Profile", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(MainScreen.this, "Umm...", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        /*
        TabHost m_TabHost = getTabHost();

        m_TabHost.addTab(m_TabHost.newTabSpec("tab1")
        					.setIndicator(getString(R.string.repositories_tab_label), getResources().getDrawable(R.drawable.repository))
        					.setContent(new Intent(this, RepositoriesList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        m_TabHost.addTab(m_TabHost.newTabSpec("tab2")
        					.setIndicator(getString(R.string.users_tab_label), getResources().getDrawable(R.drawable.users))
        					.setContent(new Intent(this, UsersList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        */
        m_prefs = getSharedPreferences(PREFS_NAME, 0);
    	m_editor = m_prefs.edit();
    	m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

        m_progressDialog = ProgressDialog.show(MainScreen.this, "Please wait...", "Loading user data...");

        m_menuList = (ListView)findViewById(R.id.lv_main_menu_list);
        m_menuList.setAdapter(new ArrayAdapter<String>(MainScreen.this, R.layout.main_menu_item, MAIN_MENU));
        m_menuList.setOnItemClickListener(onMenuItemSelected);
        
        Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					URL query = new URL("http://github.com/api/v2/json/user/show/"
										+ URLEncoder.encode(m_username)
										+ "?login="
										+ URLEncoder.encode(m_username)
										+ "&token="
										+ URLEncoder.encode(m_token));
					m_userData = Hubroid.make_api_request(query).getJSONObject("user");

					runOnUiThread(new Runnable() {
						public void run() {
							ImageView gravatar = (ImageView)findViewById(R.id.iv_main_gravatar);
							try {
								gravatar.setImageBitmap(Hubroid.getGravatar(m_userData.getString("gravatar_id"), 48));
								TextView username = (TextView)findViewById(R.id.tv_main_username);
								if (m_userData.getString("name").length() > 0) {
									username.setText(m_userData.getString("name"));
								} else {
									username.setText(m_username);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
					        RelativeLayout root_layout = (RelativeLayout)findViewById(R.id.rl_main_menu_root);
					        root_layout.setVisibility(0);
							m_progressDialog.dismiss();
						}
					});
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
        thread.start();
    }
}