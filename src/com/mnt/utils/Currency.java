package com.mnt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Currency {

	public static Map<String ,ArrayList<String>> currencyMap= new HashMap<String, ArrayList<String>>();
	
	static 
	{
		
		ArrayList<String> listMarket1=new ArrayList<String>();
		 ArrayList<String> listMarket2=new ArrayList<String>();
		 
		 listMarket1.add("BTC/USD");
		 listMarket1.add("LTC/USD ");
		 listMarket1.add("LTC/BTC");
		 listMarket1.add("NMC/BTC ");
		 listMarket1.add("NVC/BTC");
		 listMarket1.add("TRC/BTC");
		 listMarket1.add("PPC/BTC");
		 listMarket1.add("FTC/BTC");
		 listMarket1.add("CNC/BTC");
		 
		 listMarket2.add("BTC/USD ");
		 listMarket2.add("LTC/USD");
		 
		 
		 currencyMap.put("BTC-E",listMarket1);
		 currencyMap.put("MT GOX",listMarket2);
	}
	
}
