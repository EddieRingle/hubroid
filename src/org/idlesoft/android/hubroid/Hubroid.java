/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.idlesoft.libraries.ghapi.User;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import com.flurry.android.FlurryAgent;

public class Hubroid extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	// Time format used by GitHub in their responses
	public static final String GITHUB_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";
	// Time format used by GitHub in their issue API. Inconsistent, tsk, tsk.
	public static final String GITHUB_ISSUES_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZZ";
	private SharedPreferences m_prefs;
	private SharedPreferences.Editor m_editor;
	private String m_username;
	private String m_token;
	public ListView m_menuList;
	public JSONObject m_userData;
	public ProgressDialog m_progressDialog;
	public boolean m_isLoggedIn;
	private Thread m_thread;

	/**
	 * Returns a Gravatar ID associated with the provided name
	 * 
	 * @param name
	 * @return	the gravatar ID associated with the name
	 */
	public static String getGravatarID(String name) {
		String id = "";
		try {
			// Get SD card directory and check to see if it is writable
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()) {
				// Create the "hubroid" sub-directory if it doesn't already exist
				File hubroid = new File(root, "hubroid");
				if (!hubroid.exists() && !hubroid.isDirectory()) {
					hubroid.mkdir();
				}
				// Create the "gravatars" sub-directory if it doesn't already exist
				File gravatars = new File(hubroid, "gravatars");
				if (!gravatars.exists() && !gravatars.isDirectory()) {
					gravatars.mkdir();
				}
				// Create the image file on the disk
				File image = new File(gravatars, name + ".id");
				if (image.exists() && image.isFile()) {
					FileReader fr = new FileReader(image);
					BufferedReader in = new BufferedReader(fr);
					id = in.readLine();
					in.close();
				} else {
					try {
						id = new JSONObject(User.info(name).resp).getJSONObject("user").getString("gravatar_id");
						FileWriter fw = new FileWriter(image);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(id);
						bw.flush();
						bw.close();
					} catch (NullPointerException e) {
						// do nothing, we don't like null pointers
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.e("debug", "Error saving bitmap", e);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return id;
	}

	/**
	 * Returns a Bitmap of the Gravatar associated with the provided ID.
	 * This image will be scaled according to the provided size.
	 * 
	 * @param id
	 * @param size
	 * @return	a scaled Bitmap
	 */
	public static Bitmap getGravatar(String id, int size) {
		// Check to see if a gravatar of the correct size already exists
		Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
				+ "/hubroid/gravatars/"
				+ id + ".png");
		// If not, fetch one
		if (bm == null) {
			try {
				URL aURL = new URL(
				"http://www.gravatar.com/avatar.php?gravatar_id="
						+ URLEncoder.encode(id) + "&size=50&d="
						// Get the default 50x50 gravatar from GitHub if ID doesn't exist
						+ URLEncoder.encode("http://github.com/eddieringle/hubroid/raw/master/res/drawable/default_gravatar.png"));
				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				bm = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			} catch (IOException e) {
				Log.e("debug", "Error getting bitmap", e);
			}
			// Save the gravatar onto the SD card for later retrieval
			try {
				File root = Environment.getExternalStorageDirectory();
				if (root.canWrite()) {
					File hubroid = new File(root, "hubroid");
					if (!hubroid.exists() && !hubroid.isDirectory()) {
						hubroid.mkdir();
					}
					File gravatars = new File(hubroid, "gravatars");
					if (!gravatars.exists() && !gravatars.isDirectory()) {
						gravatars.mkdir();
					}
					// Add .nomedia so the Gallery doesn't pick up our gravatars
					File nomedia = new File(gravatars, ".nomedia");
					if (!nomedia.exists()) {
						nomedia.createNewFile();
					}
					File image = new File(gravatars, id + ".png");
					bm.compress(CompressFormat.PNG, 100, new FileOutputStream(image));
				}
			} catch (FileNotFoundException e) {
				Log.e("debug", "Error saving bitmap", e);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Scale the image to the desired size
		bm = Bitmap.createScaledBitmap(bm, size, size, true);
		return bm;
	}

	public static final String[] MAIN_MENU = new String[] {
		"Watched Repos",
		"Followers/Following",
		"Activity Feeds",
		"My Repositories",
		"Search",
		"My Profile"
	};

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!menu.hasVisibleItems()) {
			menu.add(0, 1, 0, "Clear Preferences");
			menu.add(0, 2, 0, "Clear Cache");
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			m_editor.clear().commit();
			Intent intent = new Intent(Hubroid.this, Hubroid.class);
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

	private OnItemClickListener onMenuItemSelected = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> pV, View v, int pos, long id) {
			Intent intent;
			switch(pos) {
			case 0:
				intent = new Intent(Hubroid.this, WatchedRepositories.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(Hubroid.this, FollowersFollowing.class);
				startActivity(intent);
				break;
			case 2:
				intent = new Intent(Hubroid.this, ActivityFeeds.class);
				intent.putExtra("username", m_username);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(Hubroid.this, RepositoriesList.class);
				startActivity(intent);
				break;
			case 4:
				intent = new Intent(Hubroid.this, Search.class);
				startActivity(intent);
				break;
			case 5:
				intent = new Intent(Hubroid.this, UserInfo.class);
				intent.putExtra("username", m_username);
				startActivity(intent);
				break;
			default:
				Toast.makeText(Hubroid.this, "Umm...", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_prefs = getSharedPreferences(PREFS_NAME, 0);
    	m_editor = m_prefs.edit();
    	m_username = m_prefs.getString("login", "");
        m_token = m_prefs.getString("token", "");
        m_isLoggedIn = m_prefs.getBoolean("isLoggedIn", false);

        // Check to see if the user is already logged in
        if (!m_isLoggedIn) {
        	// Launch the splash screen if not logged in so the user can do so
			Intent intent = new Intent(Hubroid.this, SplashScreen.class);
			startActivity(intent);
			Hubroid.this.finish();
		} else {
			// Start the show.
	        setContentView(R.layout.main_menu);

	        m_menuList = (ListView)findViewById(R.id.lv_main_menu_list);
	        m_menuList.setAdapter(new ArrayAdapter<String>(Hubroid.this, R.layout.main_menu_item, MAIN_MENU));
	        m_menuList.setOnItemClickListener(onMenuItemSelected);

	        m_thread = new Thread(new Runnable() {
				public void run() {
					try {						
						JSONObject result = new JSONObject(User.info(m_username, m_token).resp);
						m_userData = result.getJSONObject("user");

						runOnUiThread(new Runnable() {
							public void run() {
								ImageView gravatar = (ImageView)findViewById(R.id.iv_main_gravatar);
								try {
									gravatar.setImageBitmap(Hubroid.getGravatar(m_userData.getString("gravatar_id"), 36));
									TextView username = (TextView)findViewById(R.id.tv_main_username);
									if (m_userData.getString("name").length() > 0) {
										username.setText(m_userData.getString("name"));
									} else {
										username.setText(m_username);
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}

								// Unhide the screen
								((RelativeLayout)findViewById(R.id.rl_main_menu_root)).setVisibility(View.VISIBLE);
							}
						});
					} catch (JSONException e) {
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(Hubroid.this, "Error gathering user data.", Toast.LENGTH_SHORT).show();

								// Unhide the screen
								((RelativeLayout)findViewById(R.id.rl_main_menu_root)).setVisibility(View.VISIBLE);
							}
						});
						e.printStackTrace();
					}
				}
			});
	        m_thread.start();
		}
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
}