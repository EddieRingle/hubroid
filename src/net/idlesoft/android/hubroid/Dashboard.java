package net.idlesoft.android.hubroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Dashboard extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);

		((Button)findViewById(R.id.btn_dashboard_newsfeed)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(Dashboard.this, NewsFeed.class));
			}
		});
	}
}
