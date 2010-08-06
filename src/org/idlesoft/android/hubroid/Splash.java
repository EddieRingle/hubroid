package org.idlesoft.android.hubroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;

public class Splash extends Activity {
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.splash);

		SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
		if (prefs.contains("username") && prefs.contains("password")) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					long time = SystemClock.uptimeMillis();
					while ((SystemClock.uptimeMillis() - time) < 3000);
					runOnUiThread(new Runnable() {
						public void run() {
							finish();
						}
					});
				}
			});
			thread.start();
		} else {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					long time = SystemClock.uptimeMillis();
					while ((SystemClock.uptimeMillis() - time) < 3000);
					runOnUiThread(new Runnable() {
						public void run() {
							startActivity(new Intent(Splash.this, Login.class));
							finish();
						}
					});
				}
			});
			thread.start();
		}
	}
}