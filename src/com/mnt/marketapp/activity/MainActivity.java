package com.mnt.marketapp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.mnt.btcapi.BtcEApi;

import com.mnt.marketapp.R;
import com.mnt.mtgoxapi.MtGApi;
import com.mnt.utils.Currency;

public class MainActivity extends Activity {
	Spinner marketSpinner,currencySpinner;
	Button submitButton;
	String pair;
	String marketType;
	EditText quantity,rate;
	CheckBox testingCheckbox;
	ProgressDialog dialog_;
	TextView market_value,currency_value,rate_value,quantity_value,action_value;
	String action;
	RadioButton buy;
	Map<String,String> requestMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//addListenerOnSpinnerItemSelection();
		marketSpinner = (Spinner) findViewById(R.id.spinner1);
		currencySpinner=(Spinner)findViewById(R.id.spinner2);
		addItemsOfMarket();
		currencySpinner.setOnItemSelectedListener(onItemListner);
		marketSpinner.setOnItemSelectedListener(listnerOfMArketValue);
		submitButton = (Button)findViewById(R.id.submitButton);
		submitButton.setOnClickListener(listner);
		testingCheckbox = (CheckBox)findViewById(R.id.testingCheckBox);
	    buy= (RadioButton)findViewById(R.id.buy);
	    rate = (EditText)findViewById(R.id.rateEditbox);
		quantity = (EditText)findViewById(R.id.quantityEditbox);
	//	startService(new Intent(this, MyService.class));
	
	}
	
	
	OnItemSelectedListener listnerOfMArketValue = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long arg3) {
			Currency currency = new Currency();
			ArrayList<String> currencyValues = new ArrayList<String>();
			marketType=parent.getItemAtPosition(pos).toString();
			currencyValues = currency.currencyMap.get(parent.getItemAtPosition(pos).toString());
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_spinner_item, currencyValues);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				currencySpinner.setAdapter(dataAdapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	OnItemSelectedListener onItemListner = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			pair=arg0.getItemAtPosition(arg2).toString();
			pair=pair.replace("/","_").toLowerCase();
			if(testingCheckbox.isChecked()){
				Toast.makeText(MainActivity.this, pair, Toast.LENGTH_SHORT).show();
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	};
	
	
	
	OnClickListener listner = new OnClickListener() {
		
		public void onClick(View v) {	 
			// stopService(new Intent(MainActivity.this, MyService.class));
			 View view = getLayoutInflater().inflate(R.layout.popup, null);
			 market_value = (TextView) view.findViewById(R.id.choose_market);
			 currency_value = (TextView) view.findViewById(R.id.choose_currence);
			 rate_value = (TextView) view.findViewById(R.id.rate);
			 quantity_value = (TextView) view.findViewById(R.id.quantity_value);
			 action_value = (TextView) view.findViewById(R.id.action);
			 market_value.setText(marketSpinner.getSelectedItem().toString());
			 currency_value.setText(currencySpinner.getSelectedItem().toString());
			 rate_value.setText(rate.getText().toString());
			 quantity_value.setText(quantity.getText().toString());
			 if(buy.isChecked())
				{
				 action_value.setText("Buy");
				}
				else
				{
					action_value.setText("Sell");
				}
			 
			  AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
			  builder.setView(view);
			  builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
			    	dialog_ = ProgressDialog.show(MainActivity.this, "", "Please wait...", true);
					requestMap = new HashMap<String,String>();
					
					if(buy.isChecked())
					{
						requestMap.put("type","buy");
					}
					else
					{
						requestMap.put("type","sell");
					}
					requestMap.put("pair",pair);
					requestMap.put("rate",rate.getText().toString());
					requestMap.put("amount",quantity.getText().toString());
					executeAsyncTask();
					rate.setText("");
			    	quantity.setText("");
			        return;
			    } }); 
			   builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
			    	rate.setText("");
			    	quantity.setText("");
			        return;
			    } }); 
			final AlertDialog dialog1 = builder.create ();	
			dialog1.getWindow().
			    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			dialog1.show();      
			
		}
	};
	private void executeAsyncTask(){
        MyAsyncTask async=new MyAsyncTask();
        async.execute();
    }
	private class MyAsyncTask extends AsyncTask<Void,Void,String>{

		protected void onPreExecute(Activity actpass){
		  

		}
		@Override
		protected String doInBackground(Void... params) {
			
			BtcEApi btceApi = new BtcEApi(getApplicationContext());
			MtGApi mtGoxapi = new MtGApi();
			String response = null;
			if(marketType.equalsIgnoreCase("btc-e"))
			{
				try {
					response=btceApi.authenticatedHTTPRequest("Trade",requestMap);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
			}
			else{
				response=mtGoxapi.getMtGoxResponse(requestMap);
			}
			return response;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog_.dismiss();
		if(testingCheckbox.isChecked())
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
			alertDialogBuilder.setTitle("Response");
	 			alertDialogBuilder
					.setMessage(result)
					.setCancelable(false)
					.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
						}
					  });
					AlertDialog alertDialog = alertDialogBuilder.create();
	 				alertDialog.show();
		}
			
		}
		
	}
	
	public void addItemsOfMarket() {
		 
		List<String> list = new ArrayList<String>();
		list.add("BTC-E");
		list.add("MT GOX");
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		marketSpinner.setAdapter(dataAdapter);
	  }
}
