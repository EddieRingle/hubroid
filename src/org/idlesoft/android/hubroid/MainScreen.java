package org.idlesoft.android.hubroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainScreen extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView menu_list = (ListView)findViewById(R.id.lv_main_menu_list);
        menu_list.setAdapter(new ArrayAdapter<String>(MainScreen.this, R.layout.main_menu_item, MAIN_MENU));
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
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);
        setContentView(R.layout.main_menu);
    }
}