package org.idlesoft.android.hubroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;

public class Splash extends Activity {
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.splash);

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
	}
}