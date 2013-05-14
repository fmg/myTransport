package com.mytransport.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
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
import org.apache.http.params.HttpConnectionParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TestActivity extends Activity {
	
	private final static String TAG = "Rodonorte";
	ProgressBar progress;
	
	List<String> origins;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		origins = new ArrayList<String>();
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		progress.setVisibility(View.INVISIBLE);
		
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
	

	
	
	private class GetOriginLocalitiesTask extends AsyncTask<URI, Integer, Long>{
		
		//TODO: ver cancel
		
		
		final private String cookie = "Cookie:__utma=205429139.853656801.1367187359.1367187359.1367187359.1; __utmb=205429139.1.10.1367187359; __utmc=205429139; __utmz=205429139.1367187359.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)";
		private double step;
		
		@Override
		protected void onPreExecute() {
			 progress.setVisibility(View.VISIBLE);
			 progress.setIndeterminate(true);
		}
		
		
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			int status = values[0];
			if(status == -1){
				Toast.makeText(TestActivity.this, "No Wifi connection", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "No wifi connection");
				 progress.setVisibility(View.INVISIBLE);

			}else if(status >= 0){
				progress.setIndeterminate(false);
				progress.setProgress(values[0]);
			}
		}



		@Override
		protected Long doInBackground(URI... params) {
			try{
				
				ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);   
				android.net.NetworkInfo wifi =  connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
				
				if(!wifi.isAvailable() || !wifi.isConnected()){
					publishProgress(-1);
				}
				
				
				getOrigins(params[0]);
				origins.remove(0);

				step = 100.0/origins.size();
				Log.d(TAG, "STEP-> " + step +  " - " + origins.size());
				
				double percentageCompleted = 0.0;
				publishProgress((int)Math.ceil(percentageCompleted));
				
				
				for(String origin: origins){
					Log.d(TAG, "\n\n\nA obter destinos de " + origin);
					getDestiniesAndSchedules(origin);
					percentageCompleted+= step;
					publishProgress((int)Math.ceil(percentageCompleted));
				}
					
					
				
			}catch (Exception e) {
				e.printStackTrace();
				publishProgress(-2);
			}
			
			
			return null;
		}
		
	
		
		@Override
		protected void onPostExecute(Long result) {
			Log.d(TAG, "ACABOU!!!!");
			progress.setVisibility(View.GONE);
		}

		
		
		private void getOrigins(URI link) throws Exception {
			
			//HTTP request
			HttpRequestBase request = new HttpGet();
			request.setURI(link);
			HttpGet getRequest = (HttpGet)request;
			HttpConnectionParams.setConnectionTimeout(getRequest.getParams(), 3000);
			HttpConnectionParams.setSoTimeout(getRequest.getParams(), 5000);
			getRequest.setHeader("Accept","text/html");
			
			
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);

			
			HttpEntity responseEntity = response.getEntity();
			StatusLine responseStatus = response.getStatusLine();
			int statusCode = responseStatus.getStatusCode();
			
			
			if(responseEntity != null){
				
				String HTMLresult= getContent(responseEntity.getContent());
									
				Document doc = Jsoup.parse(HTMLresult);
				Element ele = doc.getElementById("origem");
				Elements cities = ele.children();
				for (Element city : cities) {
				  String cidade = new String(city.text().getBytes("ISO-8859-1"),"ISO-8859-1");
				  
				  Log.d(TAG, "ELEMENTOS -> " + cidade);
				  
				  origins.add(cidade);
				}
			}
			
		}
		
		
		
		private void getDestiniesAndSchedules(String origin) throws Exception{
			
			//HTTP request
			HttpRequestBase request = new HttpPost();
			request.setURI(new URI((Uri.parse("http://www.rodonorte.pt/destino_handler.php").toString())));
			HttpPost postRequest = (HttpPost)request;
			HttpConnectionParams.setConnectionTimeout(postRequest.getParams(), 3000);
			HttpConnectionParams.setSoTimeout(postRequest.getParams(), 5000);
			postRequest.setHeader("Accept","text/html");
			postRequest.setHeader("Content-Type","application/x-www-form-urlencoded");
			postRequest.setHeader("Cookie",cookie);
			
			//form data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("origem", origin));
            postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			
			
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);

			
			HttpEntity responseEntity = response.getEntity();
			StatusLine responseStatus = response.getStatusLine();
			int statusCode = responseStatus.getStatusCode();
			
			if(responseEntity != null){
				
				String HTMLresult = getContent(responseEntity.getContent());

				
				//Log.d(TAG, HTMLresult);
				
				Document doc = Jsoup.parse(HTMLresult);
				Element ele = doc.getElementById("destino");
				Elements cities = ele.children();
				for (Element city : cities) {
				  String destination = new String(city.text().getBytes("ISO-8859-1"),"ISO-8859-1");
				  Log.d(TAG, "A Obter horarios de "+origin + " para "  + destination);
				  getSchedules(origin, destination);
				}
			}
		}
		
		
		
		private void getSchedules(String origin, String destination) throws Exception{
			//HTTP request
			HttpRequestBase request = new HttpPost();
			request.setURI(new URI((Uri.parse("http://rodonorte.pt/timetable_handler.php").toString())));
			HttpPost postRequest = (HttpPost)request;
			HttpConnectionParams.setConnectionTimeout(postRequest.getParams(), 3000);
			HttpConnectionParams.setSoTimeout(postRequest.getParams(), 5000);
			postRequest.setHeader("Accept","text/html");
			postRequest.setHeader("Content-Type","application/x-www-form-urlencoded");
			postRequest.setHeader("Cookie",cookie);
			
			//form data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("origem", origin));
            nameValuePairs.add(new BasicNameValuePair("destino", destination));
            postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			
			
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);

			
			HttpEntity responseEntity = response.getEntity();
			StatusLine responseStatus = response.getStatusLine();
			int statusCode = responseStatus.getStatusCode();
			
			if(responseEntity != null){
				
				String HTMLresult = getContent(responseEntity.getContent());

				
				//Log.d(TAG, HTMLresult);
				
				Document doc = Jsoup.parse(HTMLresult);
				Elements tableRows = doc.getElementsByTag("tbody").get(0).children();
				
				//Log.d(TAG, eles.toString());
				
				for (int i = 0; i < tableRows.size(); i+=4){
				  
					Element scheduleTime = tableRows.get(i);
					String departureTime = new String(scheduleTime.child(1).text().getBytes("ISO-8859-1"),"ISO-8859-1");
					String arrivalTime = new String(scheduleTime.child(3).text().getBytes("ISO-8859-1"),"ISO-8859-1");
					String price = new String(scheduleTime.child(5).text().getBytes("ISO-8859-1"),"ISO-8859-1");
					
					
					String scheduleDays = new String(tableRows.get(i+1).text().getBytes("ISO-8859-1"),"ISO-8859-1");

					Log.d(TAG, "Partida: " + departureTime + "\tChegada: "+ arrivalTime + "\tPreco: "+ price + "\n" + scheduleDays);
					
					
				}
				
			}
		}
		
		
		private String getContent(InputStream is) throws IOException{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line + "\n");
			}
			is.close();
			
			return sb.toString();
		}

		
		
	}

}
