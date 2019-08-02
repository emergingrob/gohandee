package com.gohandee.services;

import android.content.Context;

import com.gohandee.services.*;
import com.gohandee.gohandeeapp.R;

public class ServiceFactory {

	private static DrupalService service = null;
	
	public static DrupalService getService(Context context, String type){
		if(service != null){
			return service;
		}
		if(type.equalsIgnoreCase("JSON")){
			service = new JSONServerClient(context,
					context.getString(R.string.sharedpreferences_name),
					context.getString(R.string.SERVER), 
					context.getString(R.string.API_KEY),
					context.getString(R.string.DOMAIN), 
					context.getString(R.string.ALGORITHM), 
					Long.parseLong(context.getString(R.string.SESSION_LIFETIME)));			
		}
		return service;
	}
}
