package com.mytransport.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class TestActivity extends Activity {
	
	private final static String TAG = "Rodonorte";
	
	HashMap<String, String> origens;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		origens = new HashMap<String,String>();
		
		try {
			new GetOriginLocalitiesTask().execute(new URI((Uri.parse("http://rodonorte.pt").toString())));
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	//Cookie:__utma=205429139.853656801.1367187359.1367187359.1367187359.1; __utmb=205429139.1.10.1367187359; __utmc=205429139; __utmz=205429139.1367187359.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)

	
	
	private class GetOriginLocalitiesTask extends AsyncTask<URI, Void, Long>{
		
		@Override
		protected Long doInBackground(URI... params) {
			try{
				for(URI url: params){
					
					//HTTP request
					HttpRequestBase request = new HttpGet();
					request.setURI(url);
					HttpGet getRequest = (HttpGet)request;
					getRequest.setHeader("Accept","text/html");
					
					HttpClient client = new DefaultHttpClient();
					HttpResponse response = client.execute(request);

					
					HttpEntity responseEntity = response.getEntity();
					StatusLine responseStatus = response.getStatusLine();
					int statusCode = responseStatus.getStatusCode();
					
					if(responseEntity != null){
						InputStream is = responseEntity.getContent();
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						StringBuilder sb = new StringBuilder();
						String line = null;
						while((line = reader.readLine()) != null){
							sb.append(line + "\n");
						}
						is.close();
						
						String HTMLresult = sb.toString();
						
						//Log.d(TAG, HTMLresult);
						
						Document doc = Jsoup.parse(HTMLresult);
						Element ele = doc.getElementById("origem");
						Elements cities = ele.children();
						for (Element city : cities) {
						  String cidade = city.text();
						  String cidadeKey = city.attr("value");
						  
						  Log.d(TAG, "ELEMENTOS -> " + cidade + " -> " + cidadeKey);
						  
						  origens.put(cidade, cidadeKey);
						}
						
						
					}
					
					
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			
			return null;
		}
		
	
		
		@Override
		protected void onPostExecute(Long result) {
			new GetDestinyLocalitiesTask().execute(new String("Vila Real"));
		}


		
		
		
	}
	
	
	private class GetDestinyLocalitiesTask extends AsyncTask<String, Void, Long>{
		
		@Override
		protected Long doInBackground(String... params) {
			try{
				for(String origin: params){
					
					//HTTP request
					HttpRequestBase request = new HttpPost();
					request.setURI(new URI((Uri.parse("http://www.rodonorte.pt/destino_handler.php").toString())));
					HttpPost postRequest = (HttpPost)request;
					postRequest.setHeader("Accept","text/html");
					postRequest.setHeader("Content-Type","application/x-www-form-urlencoded");
					postRequest.setHeader("Cookie","Cookie:__utma=205429139.853656801.1367187359.1367187359.1367187359.1; __utmb=205429139.1.10.1367187359; __utmc=205429139; __utmz=205429139.1367187359.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
					
					//form data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		            nameValuePairs.add(new BasicNameValuePair("origem", "ALFÂNDEGA DA FÉ"));
		            postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					
					
					HttpClient client = new DefaultHttpClient();
					HttpResponse response = client.execute(request);

					
					HttpEntity responseEntity = response.getEntity();
					StatusLine responseStatus = response.getStatusLine();
					int statusCode = responseStatus.getStatusCode();
					
					if(responseEntity != null){
						InputStream is = responseEntity.getContent();
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						StringBuilder sb = new StringBuilder();
						String line = null;
						while((line = reader.readLine()) != null){
							sb.append(line + "\n");
						}
						is.close();
						
						String HTMLresult = sb.toString();
						
						Log.d(TAG, HTMLresult);
						/*
						Document doc = Jsoup.parse(HTMLresult);
						Element ele = doc.getElementById("origem");
						Elements cities = ele.children();
						for (Element city : cities) {
						  String cidade = city.text();
						  Log.d(TAG, "ELEMENTOS -> " + cidade);
						}
						*/
						
					}
					
					
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Long result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
		
	}

}
