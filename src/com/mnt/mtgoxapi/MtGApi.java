package com.mnt.mtgoxapi;

import java.util.Map;


public class MtGApi {
	public String getMtGoxResponse(Map<String, String> arguments)
	{
		ApiKeys keys = new ApiKeys(MtGoxConstants.mtgox_secretKey,MtGoxConstants.mtgox_apikey); 
		MtGox trade = new MtGox(keys);
		if(arguments.get("type").equals("buy"))
		{
		String buyResponse=trade.buyBTC(Double.parseDouble(arguments.get("amount")));
		return buyResponse;
		}
		else
		{
		String saleResponse=trade.sellBTC(Double.parseDouble(arguments.get("amount")));
		return saleResponse;
		}
	}
}
