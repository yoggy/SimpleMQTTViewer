//
//	Config.java - a simple wrapper class for SharedPreference.  
//
//  license:
//      Copyright (c) 2015 yoggy <yoggy0@gmail.com>
//      Released under the MIT license
//      http://opensource.org/licenses/mit-license.php
//
package net.sabamiso.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config {

	private static Config singleton;

	public static void init(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		singleton = new Config(prefs);
	}

	public static synchronized Config getInstance() {
		return singleton;
	}

	private SharedPreferences prefs;
	
	private Config(SharedPreferences prefs) {
		this.prefs = prefs;
	}
	
	public boolean contains(String key) {
		return prefs.contains(key);
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int default_value) {
		if (!contains(key)) setInt(key, default_value);
		int val = Integer.parseInt(prefs.getString(key, ""));
		return val;
	}

	public void setInt(String key, int value) {
		String val_str = Integer.toString(value);
		setString(key, val_str);
	}
		
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	public boolean getBoolean(String key, boolean default_value) {
		if (!contains(key)) setBoolean(key, default_value);
		return prefs.getBoolean(key, default_value);
	}
	
	public void setBoolean(String key, boolean value) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(key, value);
		e.commit();
	}	

	public String getString(String key) {
		return getString(key, "");
	}
	
	public String getString(String key, String default_value) {
		if (!contains(key)) setString(key, default_value);
		return prefs.getString(key, default_value);
	}

	public void setString(String key, String value) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(key, value);
		e.commit();
	}
}
