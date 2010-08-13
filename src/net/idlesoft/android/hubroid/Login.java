package net.idlesoft.android.hubroid;

import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.login);
		((Button)findViewById(R.id.btn_login_login)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				String user = ((EditText)findViewById(R.id.et_login_username)).getText().toString();
				String pass = ((EditText)findViewById(R.id.et_login_password)).getText().toString();
				if (user.equals("") || pass.equals("")) {
					Toast.makeText(Login.this, "Login details cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				GitHubAPI ghapi = new GitHubAPI();
				ghapi.authenticate(user, pass);
				int returnCode = ghapi.user.private_activity().statusCode;
				if (returnCode == 401) {
					Toast.makeText(Login.this, "Login details incorrect, try again", Toast.LENGTH_SHORT).show();
				} else if (returnCode == 200) {
					SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
					Editor edit = prefs.edit();
					edit.putString("username", user);
					edit.putString("password", pass);
					edit.commit();
					Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
					Login.this.startActivity(new Intent(Login.this, Dashboard.class));
					Login.this.finish();
				} else {
					Toast.makeText(Login.this, "Unknown login error: " + returnCode, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}