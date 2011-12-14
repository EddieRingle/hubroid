package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.utils.StringUtils;

import org.apache.http.client.ClientProtocolException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class OAuthActivity extends BaseActivity {
    private ProgressDialog mProgressDialog;
    private static class AccessTokenTask extends AsyncTask<String, Void, Void> {
        public OAuthActivity activity;

        @Override
        protected void onPreExecute() {
            activity.mProgressDialog = ProgressDialog.show(activity, null, "Requesting OAuth token...");
            super.onPreExecute();
        }

        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("https://github.com/login/oauth/access_token");
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes("client_id=1b068a84673859c1eff5"
                        + "&client_secret=00b95b144476f32c33483cb79f1a9ed90b1ecbce"
                        + "&code=" + params[0]);
                wr.flush();
                wr.close();

                InputStream is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer resp = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    resp.append(line);
                }

                HashMap<String, String> query = new HashMap<String, String>(StringUtils.mapQueryString(resp.toString()));
                String access_token = query.get("access_token");
                if (access_token != null) {
                    activity.mPrefsEditor.putString("access_token", access_token);
                    activity.mPrefsEditor.commit();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            activity.mProgressDialog.dismiss();
            final Intent i = new Intent(activity, Login.class);
            i.putExtra("tryingOAuth", true);
            activity.startActivity(i);
            activity.finish();
        }
    }

    private AccessTokenTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, NO_LAYOUT);
        final WebView wv = new WebView(OAuthActivity.this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("hubroid", "url: " + url);
                if (url.contains("callback")) {
                    final Intent i = new Intent(OAuthActivity.this, GitHubIntentFilter.class);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    finish();
                    return false;
                } else {
                    return false;
                }
            }
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.d("hubroid", "sslerror");
                handler.proceed();
            }
        });
        setContentView(wv);
        mTask = (AccessTokenTask) getLastNonConfigurationInstance();
        if (mTask == null) {
            mTask = new AccessTokenTask();
        }
        mTask.activity = OAuthActivity.this;
        if (mTask.getStatus() == AsyncTask.Status.RUNNING && mProgressDialog != null) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog = ProgressDialog.show(OAuthActivity.this, null, "Requesting OAuth token...");
            }
        }
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getInt("stage") == 1) {
                Log.d("hubroid", "stage 1");
                wv.loadUrl("https://github.com/login/oauth/authorize?client_id=1b068a84673859c1eff5&scope=user,repo,gist");
            } else if (b.getInt("stage") == 2 && b.getString("code") != null) {
                Log.d("hubroid", "stage 2");
                mTask.execute(b.getString("code"));
            }
        }
    }
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }
    @Override
    protected void onPause() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onPause();
    }
}