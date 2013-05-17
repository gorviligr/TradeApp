/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnt.mtgoxapi;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mnt.utils.HttpUtils;


import android.util.Base64;

/**
 *
 * https://github.com/adv0r/mtgox-apiv2-java
 * @author adv0r <leg@lize.it>
 * MIT License (see LICENSE.md)
 * Implementation of MtGox Api V2
 * Consider donations @ 1N7XxSvek1xVnWEBFGa5sHn1NhtDdMhkA7
 * unofficial documentation by nitrous https://bitbucket.org/nitrous/mtgox-api/overview
 */
public class MtGox implements TradeInterface{
    
private ApiKeys keys;


private final int USD_DIVIDE_FACTOR = 100000;  //In order to use the intvalue provided by the api
private final int EUR_DIVIDE_FACTOR = 100000;  //you should divide the intvalue by this number. Or vice versa
private final int BTC_DIVIDE_FACTOR = 100000000;
private final double USD_MULTIPLY_FACTOR = 0.00001; 
private final double EUR_MULTIPLY_FACTOR = 0.00001;  
private final double BTC_MULTIPLY_FACTOR = 0.00000001;

private final double MIN_ORDER = 0.01; //BTC

private final String API_BASE_URL = "https://data.mtgox.com/api/2/";

//Paths
private final String API_GET_INFO = "MONEY/INFO";
private final String API_TICKER_USD = "BTCUSD/MONEY/TICKER";
private final String API_TICKER_EUR = "BTCEUR/MONEY/TICKER";
private final String API_TICKER_FAST_USD = "BTCUSD/MONEY/TICKER_FAST"; 
private final String API_TICKER_FAST_EUR = "BTCEUR/MONEY/TICKER_FAST"; 

private final String API_WITHDRAW = "MONEY/BITCOIN/SEND_SIMPLE";
private final String API_LAG = "MONEY/ORDER/LAG";
private final String API_ADD_ORDER = "BTCUSD/MONEY/ORDER/ADD";

private final String SIGN_HASH_FUNCTION = "HmacSHA512";
private final String ENCODING = "UTF-8";

private boolean printHttpResponse ;

  public MtGox(ApiKeys keys) {
        this.keys = keys;
        printHttpResponse = false;
    }
  
  public void setPrintHTTPResponse(boolean resp)
    {
        this.printHttpResponse = resp;
    }
  
    @Override
    public String getLag()  {        
        String urlPath = API_LAG;
        HashMap<String, String> query_args = new HashMap<String,String>();
         /*Params
         * 
         */
        String queryResult = query(urlPath, query_args);
         /*Sample result
         * the lag in milliseconds
         */
         JSONParser parser=new JSONParser();
         String lag="";
         try {
            JSONObject httpAnswerJson=(JSONObject)(parser.parse(queryResult));
            JSONObject dataJson = (JSONObject)httpAnswerJson.get("data");
            lag = (String)dataJson.get("lag_text");                      
         } catch (ParseException ex) {
            Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return lag;
    }

    @Override
    public String withdrawBTC(double amount, String dest_address) {  //TODO
        String urlPath = API_WITHDRAW;
        HashMap<String, String> query_args = new HashMap<String,String>();
        /*Params
         * address : Target bitcoin address
         * amount_int : Amount of bitcoins to withdraw
         * fee_int : Fee amount to be added to transaction (optional), maximum 0.01 BTC
         * no_instant : Setting this parameter to 1 will prevent transaction from being processed internally, and force usage of the bitcoin blockchain even if receipient is also on the system
         * green : Setting this parameter to 1 will cause the TX to use MtGox’s green address
         */
        query_args.put("amount_int", Long.toString(Math.round(amount*BTC_DIVIDE_FACTOR)));
        query_args.put("address",dest_address);
        String queryResult = query(urlPath, query_args);
        

         /*Sample result
         * On success, this method will return the transaction id (in offser trx ) which will contain either the bitcoin transaction id as hexadecimal or a UUID value in case of internal transfer.
         */
        
         JSONParser parser=new JSONParser();
         try {
            JSONObject obj2=(JSONObject)(parser.parse(queryResult));
            //JSONObject data = (JSONObject)obj2.get("data"); //TODO
  
            
         } catch (ParseException ex) {
            Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ""; //TODO Edit
    }

    @Override
    public String sellBTC(double amount) {
       return placeOrder("sell", Math.round(amount*BTC_DIVIDE_FACTOR));
    }

    @Override
    public String buyBTC(double amount) {
       return placeOrder("buy", Math.round(amount*BTC_DIVIDE_FACTOR));
    }
    
     public String placeOrder(String type, long amount_int) {
         
        String toReturn = "";
        String result = "" ;
        String data= "";
        String errorData=null;
        String urlPath = API_ADD_ORDER;
        HashMap<String, String> query_args = new HashMap<String,String>();
        /*Params
         * type : {ask (sell) | bid(buy) }
         * amount_int : amount of BTC to buy or sell, as an integer
         * price_int : The price per bitcoin in the auxiliary currency, as an integer, optional if you wish to trade at the market price
         */
        query_args.put("amount_int",Long.toString(amount_int));
        if (type.equals("sell"))
           query_args.put("type", "ask");
        else 
           query_args.put("type", "bid");
        
        String queryResult = query(urlPath, query_args); 
         /*Sample result
         * {"result":"success","data":"abc123-def45-.."} 
         */
         JSONParser parser=new JSONParser();
         try {
            JSONObject obj2=(JSONObject)(parser.parse(queryResult));
            result = (String)obj2.get("result");
            data = (String)obj2.get("data");
            errorData=(String)obj2.get("error");
            //lastPriceArray[0] = (Double)obj2.get("last"); //USD
  
            
         } catch (ParseException ex) {
            Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(result.equals("success"))
        {
            toReturn="executed : " +data; 
        }
        else 
            toReturn=errorData;//"not executed : " +data; //TODO test this branch
        
        return toReturn; //TODO change
    }

    @Override
    public double[] getBalance() {
        String urlPath = API_GET_INFO;
        HashMap<String, String> query_args = new HashMap<String,String>();
        
        /*Params
         * 
         */
        double[] balanceArray = new double[3];

        
        
        String queryResult = query(urlPath, query_args); 
         /*Sample result
         * {
         *   "data": {
         *       "Created": "yyyy-mm-dd hh:mm:ss",
         *       "Id": "abc123",
         *       "Index": "123",
         *       "Language": "en_US",
         *       "Last_Login": "yyyy-mm-dd hh:mm:ss",
         *       "Login": "username",
         *       "Monthly_Volume":                   **Currency Object**,
         *       "Trade_Fee": 0.6,
         *       "Rights": ['deposit', 'get_info', 'merchant', 'trade', 'withdraw'],
         *       "Wallets": {
         *           "BTC": {
         *               "Balance":                  **Currency Object**,
         *               "Daily_Withdraw_Limit":     **Currency Object**,
         *               "Max_Withdraw":             **Currency Object**,
         *               "Monthly_Withdraw_Limit": null,
         *               "Open_Orders":              **Currency Object**,
         *               "Operations": 1,
         *           },
         *           "USD": {
         *               "Balance":                  **Currency Object**,
         *               "Daily_Withdraw_Limit":     **Currency Object**,
         *               "Max_Withdraw":             **Currency Object**,
         *               "Monthly_Withdraw_Limit":   **Currency Object**,
         *               "Open_Orders":              **Currency Object**,
         *               "Operations": 0,
         *           },
         *           "JPY":{...}, "EUR":{...},
         *           // etc, depends what wallets you have
         *       },
         *   },
         *   "result": "success"
         * }
         */
         JSONParser parser=new JSONParser();
         try {
            JSONObject httpAnswerJson=(JSONObject)(parser.parse(queryResult));
            JSONObject dataJson = (JSONObject)httpAnswerJson.get("data");  
            JSONObject walletsJson = (JSONObject)dataJson.get("Wallets"); 
            
            JSONObject BTCwalletJson = (JSONObject)((JSONObject)walletsJson.get("BTC")).get("Balance");  
        
            String BTCBalance = (String)BTCwalletJson.get("value");
                      
            boolean hasDollars = true;
            boolean hasEuros = true;
            JSONObject USDwalletJson,EURwalletJson;
            String USDBalance ="" , EURBalance ="";

            try{
                 USDwalletJson = (JSONObject)((JSONObject)walletsJson.get("USD")).get("Balance"); 
                 USDBalance = (String)USDwalletJson.get("value");
            }
            catch (Exception e)
            {
                hasDollars = false;
            }
            
            try{
                 EURwalletJson = (JSONObject)((JSONObject)walletsJson.get("EUR")).get("Balance");
                 EURBalance = (String)EURwalletJson.get("value");
            }
            catch (Exception e)
            {
                hasEuros = false;  
            }
            
            balanceArray[0] = Double.parseDouble(BTCBalance); //BTC
            
            if(hasDollars)
                balanceArray[1] = Double.parseDouble(USDBalance); //USD
            else 
                balanceArray[1] = -1; //Account does not have USD wallet

            if(hasEuros)
                balanceArray[2] = Double.parseDouble(EURBalance); //EUR
            else 
                balanceArray[2] = -1; //Account does not have EUR wallet

            
         } catch (ParseException ex) {
            Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return balanceArray;
    }
    
    @Override
    public double getLastPriceUSD() {
        return getLastPrice("USD");
    }

    @Override
    public double getLastPriceEUR() {
        return getLastPrice("EUR");
    }

    
    public String query(String path, HashMap<String, String> args) {
        GoxService query = new GoxService(path, args, keys);
        String queryResult = query.executeQuery();
        return queryResult;
        //TODO should be done by a different thread ...
    }
    

    public double getLastPrice(String currency) {    
  
        String urlPath="";
        long divideFactor;
        /*switch (Integer.parseInt(currency)) {
        case "USD":
            urlPath = API_TICKER_FAST_USD ;
            divideFactor = USD_DIVIDE_FACTOR;
            break;
        case "EUR":
            urlPath = API_TICKER_FAST_EUR ; //TODO When they will fix it change to ticker fast!! It is not working properly today 17Apr2013
            divideFactor = EUR_DIVIDE_FACTOR;
            break;
        default:
            throw new UnsupportedOperationException("MTGOX API ERROR: Currency - "+currency+ " - Not supported yet.");
    }*/
        HashMap<String, String> query_args = new HashMap<String,String>();
        
        /*Params :
        * No params required
        */
        String queryResult = query(urlPath, query_args);

         /* Result sample :
         *{
         *   "result":"success",
         *   "data": {
         *       "high":       **Currency Object - USD**,
         *       "low":        **Currency Object - USD**,
         *       "avg":        **Currency Object - USD**,
         *       "vwap":       **Currency Object - USD**,
         *       "vol":        **Currency Object - BTC**,
         *       "last_local": **Currency Object - USD**,
         *       "last_orig":  **Currency Object - ???**,
         *       "last_all":   **Currency Object - USD**,
         *       "last":       **Currency Object - USD**,
         *       "buy":        **Currency Object - USD**,
         *       "sell":       **Currency Object - USD**,
         *       "now":        "1364689759572564"
         *   }
         *}
         */
        JSONParser parser=new JSONParser();
        double last=0;
        try {
            JSONObject httpAnswerJson=(JSONObject)(parser.parse(queryResult));
            JSONObject dataJson = (JSONObject)httpAnswerJson.get("data");
            JSONObject lastJson = (JSONObject)dataJson.get("last");
            String last_String = (String)lastJson.get("value");
            last = Double.parseDouble(last_String);
            
         } catch (ParseException ex) {
            Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
        }
        return last;
    }
   
    private class GoxService  {
        protected String path;
        protected HashMap args;
        protected ApiKeys keys;
        

        public GoxService(String path,HashMap<String, String> args, ApiKeys keys)
        {
            this.path = path;
            this.args = args;
            this.keys = keys;
        }
        
    //Build the query string given a set of query parameters
    private String buildQueryString(HashMap<String, String> args) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) result += '&';
            try {
                result += URLEncoder.encode(hashkey, ENCODING) + "="
                        + URLEncoder.encode(args.get(hashkey), ENCODING);
            } catch (Exception ex) {
                Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    
    private String signRequest(String secret, String hash_data) {
       String signature = "";
        try{
          Mac mac = Mac.getInstance(SIGN_HASH_FUNCTION);
          SecretKeySpec secret_spec = new SecretKeySpec(Base64.decode(secret,Base64.DEFAULT), SIGN_HASH_FUNCTION);
          mac.init(secret_spec);
          signature = Base64.encodeToString(mac.doFinal(hash_data.getBytes()),Base64.DEFAULT);
        }
        catch (Exception e){
          Logger.getLogger(MtGox.class.getName()).log(Level.SEVERE, null, e);            
        }
        return signature;
    }
    
   

    private String executeQuery() {
                        String answer = "";
                        String nonce = String.valueOf(System.currentTimeMillis())+"000";
                        try {
                          args.put("nonce", nonce);     
                            
                            String post_data= buildQueryString(args);
                            String hash_data = path + "\0" + post_data; //Should be correct
                            // args signature with apache cryptografic tools
                            String signature = signRequest(keys.getPrivateKey(), hash_data);
                            // build URL
                            URL queryUrl = new URL(API_BASE_URL + path); 
                            HashMap<String,String> headerLines = new HashMap<String, String>();
                            
                            headerLines.put("User-Agent", "Advanced-java-client API v2");
                            headerLines.put("Rest-Key", keys.getApiKey());
                            headerLines.put("Rest-Sign", signature.replaceAll("\n", ""));

                             try {
                            	answer=HttpUtils.getUrlByPost(queryUrl.toString(), args, headerLines, 2);
                    		} catch (IOException e1) {
                    			// TODO Auto-generated catch block
                    			e1.printStackTrace();
                    		}
                            //Read the response
                                                   } 
                        //Capture Exceptions
                        catch (IllegalStateException ex) {
                             System.err.println(ex);
                        }
                        catch (IOException ex) {
                             System.err.println(ex);
                        }
                       //{"result":"error","error":"Access denied to this API, missing rights","token":"access_denied"}    
                        return answer;        
        }
    }
}