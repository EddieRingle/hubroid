package org.idlesoft.android.hubroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Hubroid extends TabActivity {
	public static JSONObject make_api_request(URL url) {
		JSONObject json = null;
		HttpClient c = new DefaultHttpClient();
		HttpGet getReq;
		try {
			getReq = new HttpGet(url.toURI());
			HttpResponse resp = c.execute(getReq);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				resp.getEntity().writeTo(os);
				json = new JSONObject(os.toString());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost m_TabHost = getTabHost();

        m_TabHost.addTab(m_TabHost.newTabSpec("tab1")
        					.setIndicator(getString(R.string.repositories_tab_label), getResources().getDrawable(R.drawable.repository))
        					.setContent(new Intent(this, RepositoriesList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        m_TabHost.addTab(m_TabHost.newTabSpec("tab2")
        					.setIndicator(getString(R.string.users_tab_label), getResources().getDrawable(R.drawable.users))
        					.setContent(new Intent(this, UsersList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
}