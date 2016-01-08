package com.example.mitmotionapp.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.jsiegel.mitmotionapp.R;

import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Message;

import java.text.DateFormat;

import android.util.Base64;
import android.widget.ImageView;

public class RestQuery extends AsyncTask<String, Void, String> {

	public AsyncResponse delegate=null;
	
	String user = "rootApp";
	String password = "cloudcar12";
			
	@Override
	protected String doInBackground(String... urls) {
		
		HttpGet getRequest = new HttpGet(urls[0]);
		
		String base64AuthString = "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), Base64.NO_WRAP);
		getRequest.addHeader("Authorization", base64AuthString);
		
		getRequest.addHeader("Accept", "application/json");
		
		String responseString = null;
		
		try {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(getRequest);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        response.getEntity().writeTo(out);
	        out.close();
	        responseString = out.toString();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}

		return responseString;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			delegate.processFinish(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
