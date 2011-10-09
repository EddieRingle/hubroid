
package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.GravatarCache;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class HubroidPreferences extends PreferenceActivity {
    private static class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        public HubroidPreferences activity;

        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, "Please wait...",
                    "Clearing the gravatar cache...");
            super.onPreExecute();
        }

        protected Void doInBackground(Void... arg0) {
            GravatarCache.clearCache();
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mProgressDialog.dismiss();
            super.onPostExecute(result);
        }
    };

    private ClearCacheTask mClearCacheTask;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mClearCacheTask = (ClearCacheTask) getLastNonConfigurationInstance();
        if (mClearCacheTask == null) {
            mClearCacheTask = new ClearCacheTask();
        }
        mClearCacheTask.activity = HubroidPreferences.this;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("clear_gravatar_cache")) {
            if (mClearCacheTask.getStatus() == AsyncTask.Status.FINISHED) {
                mClearCacheTask = new ClearCacheTask();
                mClearCacheTask.activity = HubroidPreferences.this;
            }
            mClearCacheTask.execute();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mClearCacheTask;
    }

    @Override
    protected void onPause() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mClearCacheTask.getStatus() == AsyncTask.Status.RUNNING && mProgressDialog != null
                && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        super.onResume();
    }
}
