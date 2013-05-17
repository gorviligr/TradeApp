package com.mnt.btcapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import android.content.Context;
import com.mnt.libraryfiles.HelperSharedPreferences;
import com.mnt.utils.Hex;
import com.mnt.utils.HttpUtils;

public class BtcEApi {
	String  requestResult=null;
	Context mContext;
	int noOfTimesError;
	int nounce;
	String mthod;
	Map<String, String> aguments= new HashMap<String, String>();
	   public BtcEApi(Context mContext){
		   this.mContext=mContext;
	   }
	   // Create a unixtime nonce for the new API.
		//static long	_nonce = ();

	    /**
	     * Execute a authenticated query on btc-e.
	     *
	     * @param method The method to execute.
	     * @param arguments The arguments to pass to the server.
	     *
	     * @return The returned data as JSON or null, if the request failed.
	     *
	     * @see http://pastebin.com/K25Nk2Sv
	     */
	    public final String authenticatedHTTPRequest( String method, Map<String, String> arguments) throws JSONException 
	    {
	    this.mthod=method;
	    this.aguments=arguments;
		HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.
		Mac mac;
		SecretKeySpec key = null;

		if( arguments == null) {  // If the user provided no arguments, just create an empty argument array.
		    arguments = new HashMap<String, String>();
		}
		nounce=HelperSharedPreferences.getSharedPreferencesInt(mContext, "nonce",0);
		nounce++;
		arguments.put( "method", method);  // Add the method to the post data.
		arguments.put( "nonce",  ""+nounce);  // Add the dummy nonce.

		String postData = "";

		for( Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext(); ) {
		    Map.Entry argument = (Map.Entry)argumentIterator.next();
		    
		    if( postData.length() > 0) {
			postData += "&";
		    }
		    postData += argument.getKey() + "=" + argument.getValue();
		}

		// Create a new secret key
		
		try {
			String  _secret= BtcEConstants.btc_e_secretKey;
		    key = new SecretKeySpec( _secret.getBytes( "UTF-8"), "HmacSHA512" ); 
		} catch( UnsupportedEncodingException uee) {
		    System.err.println( "Unsupported encoding exception: " + uee.toString());
		    return null;
		} 

		// Create a new mac
		try {
		    mac = Mac.getInstance( "HmacSHA512" );
		} catch( NoSuchAlgorithmException nsae) {
		    System.err.println( "No such algorithm exception: " + nsae.toString());
		    return null;
		}

		try {
		    mac.init( key);
		} catch( InvalidKeyException ike) {
		    System.err.println( "Invalid key exception: " + ike.toString());
		    return null;
		}
		 String _key= BtcEConstants.btc_e_key;
		
		headerLines.put( "Key", _key);

		// Encode the post data by the secret and encode the result as base64.
		try {
		    headerLines.put( "Sign", Hex.encodeHexString( mac.doFinal( postData.getBytes( "UTF-8"))));
		} catch( UnsupportedEncodingException uee) {
		    System.err.println( "Unsupported encoding exception: " + uee.toString());
		    return null;
		} 
		
		// Now do the actual request
		String DOMAIN  = "btc-e.com";

		try {
        	requestResult=HttpUtils.getUrlByPost("https://" + DOMAIN + "/tapi", arguments, headerLines, 2);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String result=null;
		if( requestResult != null) {   // The request worked
			result =execute();
		} 
		
		return result;  
	    }
	    
	    String execute()
	    {
	    	
	    	 org.json.JSONObject userData = null;
			try {
				userData = new org.json.JSONObject(requestResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			 if(userData.opt("error")!=null)
			 {
				 String errorData=userData.opt("error").toString();
				 if(errorData.contains("invalid nonce parameter"))
				 {
					 if(noOfTimesError>3)
					 {
						 return String.valueOf(-1);
					 }
					 else
					 {
						 String[] array = errorData.split("\\s+"); 
						 if(array.length>3)
						 {
							 nounce=Integer.parseInt(array[3]);
							 HelperSharedPreferences.putSharedPreferencesInt(mContext, "nonce",nounce);
							 try {noOfTimesError++;
								authenticatedHTTPRequest(mthod,aguments);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						 }
					 }
				 }
				 else
				 {
					 return requestResult;
				 }
			 }
			 HelperSharedPreferences.putSharedPreferencesInt(mContext,"nonce", nounce);
	    	return requestResult;
	    }

}
