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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class Hubroid extends Activity {
	public static final String PREFS_NAME = "HubroidPrefs";
	// Time format used by GitHub in their responses
	public static final String GITHUB_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";
	// Time format used by GitHub in their issue API. Inconsistent, tsk, tsk.
	public static final String GITHUB_ISSUES_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZZ";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(Hubroid.this, Splash.class));
        finish();
    }
}