package net.idlesoft.android.apps.github.activities;

import org.apache.http.util.EncodingUtils;

import shade.org.apache.http.HttpEntity;
import shade.org.apache.http.HttpResponse;
import shade.org.apache.http.NameValuePair;
import shade.org.apache.http.client.ClientProtocolException;
import shade.org.apache.http.client.HttpClient;
import shade.org.apache.http.client.entity.UrlEncodedFormEntity;
import shade.org.apache.http.client.methods.HttpPost;
import shade.org.apache.http.client.utils.URLEncodedUtils;
import shade.org.apache.http.impl.client.DefaultHttpClient;
import shade.org.apache.http.message.BasicNameValuePair;
import shade.org.apache.http.util.EntityUtils;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://github.com/login/oauth/access_token");
            try {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(3);
                pairs.add(new BasicNameValuePair("client_id", "1b068a84673859c1eff5"));
                pairs.add(new BasicNameValuePair("client_secret", "00b95b144476f32c33483cb79f1a9ed90b1ecbce"));
                pairs.add(new BasicNameValuePair("code", params[0]));
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = client.execute(post);
                HttpEntity resEntities = response.getEntity();
                if (resEntities != null) {
                    List<NameValuePair> results = URLEncodedUtils.parse(resEntities);
                    for (int i = 0; i < results.size(); i++) {
                        if (results.get(i) != null && results.get(i).getName().equalsIgnoreCase("access_token")) {
                            if (activity.mPrefsEditor == null) {
                                Log.d("hubroid", "GASP!");
                            }
                            activity.mPrefsEditor.putString("access_token", results.get(i).getValue());
                            activity.mPrefsEditor.commit();
                            return null;
                        } else {
                            continue;
                        }
                    }
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