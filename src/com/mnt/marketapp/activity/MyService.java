package com.mnt.marketapp.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mnt.btcapi.BtcEApi;
import com.mnt.marketapp.R;
import com.mnt.mtgoxapi.MtGApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
public class MyService extends Service {
	private static final String TAG = "MyService";
	MediaPlayer player;
	final Handler handler = new Handler();
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
	//	Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		handler.removeCallbacks(timedTask);
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		JSONArray data = null;
		List<String> amount = new ArrayList<String>();
		List<String> price = new ArrayList<String>();
		List<String> type = new ArrayList<String>();
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		MyAsyncTask async=new MyAsyncTask();
	    try {
	  		String result=async.execute().get();
	  		
	  		try {
				JSONObject json = new JSONObject(result);
				JSONParser jParser = new JSONParser();
				data = json.getJSONArray("return");
				for(int i = 0; i < data.length(); i++){
			        JSONObject c = data.getJSONObject(i);
			         
			        // Storing each json item in variable
			        amount.add(c.getString("amount"));
			        price.add(c.getString("price"));
			        type.add(c.getString("type"));
			        System.out.println("amount"+amount);
			        System.out.println("price"+price);
			        System.out.println("type"+type);
				         
				  }
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  	} catch (InterruptedException e) {
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	} catch (ExecutionException e) {
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
	//	handler.post(timedTask);
		Log.d(TAG, "onStart");

	}
	 
    private Runnable timedTask = new Runnable(){

  @Override
  public void run() {
   // TODO Auto-generated method stub
   MyAsyncTask async=new MyAsyncTask();
    async.execute();
  
   Toast.makeText(getBaseContext(), "Congrats! MyService Created", Toast.LENGTH_SHORT).show();
   handler.postDelayed(timedTask, 5000);
  }};
  
  
  private class MyAsyncTask extends AsyncTask<Void,Void,String>{

		protected void onPreExecute(Activity actpass){
		  

		}
		@Override
		protected String doInBackground(Void... params) {
			
			String ur = "https://bter.com/api/1/trade/ltc_btc";
			StringBuilder response  = new StringBuilder();
			 try {
		    URL url = new URL(ur);
		    HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
		    if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
		    {
		        BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
		        String strLine = null;
		        while ((strLine = input.readLine()) != null)
		        {
		            response.append(strLine);
		        }
		        input.close();
		    }
		    
			 } catch (MalformedURLException e) {
			        // TODO Auto-generated catch block e.printStackTrace(); } catch
			    } catch (IOException e) { // TODO Auto-generated catch block
			                                // e.printStackTrace();
			    }
		    return response.toString();	
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			
		}
		
	}
  
  
}


//