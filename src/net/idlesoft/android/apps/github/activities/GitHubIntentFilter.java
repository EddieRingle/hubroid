package net.idlesoft.android.apps.github.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GitHubIntentFilter extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String uri = getIntent().getDataString();
		final String[] parts = uri.split("/");
		final Intent intent = new Intent();

		if (parts[2].equalsIgnoreCase("github.com")) {
			if (parts.length == 3) {
				/* User only meant to go to github.com */
			} else if (parts.length == 4) {
				intent.setClass(getApplicationContext(), Profile.class);
				intent.putExtra("username", parts[3]);
				startActivity(intent);
			} else if (parts.length == 5) {
				intent.setClass(getApplicationContext(), Repository.class);
				intent.putExtra("repo_owner", parts[3]);
				intent.putExtra("repo_name", parts[4]);
				startActivity(intent);
			} else if (parts.length >= 6) {
				if (parts[5].equalsIgnoreCase("issues")) {
					intent.setClass(getApplicationContext(), Issues.class);
					intent.putExtra("repo_owner", parts[3]);
					intent.putExtra("repo_name", parts[4]);
					if (parts.length > 6 && parts[6] != null) {
						intent.setClass(getApplicationContext(), SingleIssue.class);
						intent.putExtra("number", Integer.parseInt(parts[6]));
					}
					startActivity(intent);
				} else if (parts[5].equalsIgnoreCase("network")) {
					intent.setClass(getApplicationContext(), NetworkList.class);
					intent.putExtra("repo_owner", parts[3]);
					intent.putExtra("repo_name", parts[4]);
					startActivity(intent);
				} else if (parts[5].equalsIgnoreCase("commits")) {
					if (parts.length > 6 && parts[6] != null) {
						intent.setClass(getApplicationContext(), CommitsList.class);
						intent.putExtra("repo_owner", parts[3]);
						intent.putExtra("repo_name", parts[4]);
						intent.putExtra("branch_name", parts[6]);
						startActivity(intent);
					}
				} else if (parts[5].equalsIgnoreCase("commit")) {
					if (parts.length > 6 && parts[6] != null) {
						intent.setClass(getApplicationContext(), Commit.class);
						intent.putExtra("repo_owner", parts[3]);
						intent.putExtra("repo_name", parts[4]);
						intent.putExtra("commit_sha", parts[6]);
						startActivity(intent);
					}
				} else if (parts[5].equalsIgnoreCase("branches")) {
					intent.setClass(getApplicationContext(), BranchesList.class);
					intent.putExtra("repo_owner", parts[3]);
					intent.putExtra("repo_name", parts[4]);
					startActivity(intent);
				}
			}
		} else if (parts[2].equalsIgnoreCase("gist.github.com")) {
			if (parts.length == 4) {
				intent.setClass(getApplicationContext(), SingleGist.class);
				intent.putExtra("gistId", parts[3]);
				startActivity(intent);
			}
		}
		finish();
	}
}
