package net.sabamiso.android.simplemqttviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import net.sabamiso.android.util.Config;

public class MyMQTTConfig extends Config {
	private static MyMQTTConfig singleton;
	
	public static void init(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		singleton = new MyMQTTConfig(prefs);
	}

	public static synchronized MyMQTTConfig getInstance() {
		return singleton;
	}

	public MyMQTTConfig(SharedPreferences prefs) {
		super(prefs);
	}

	public String getClientId() {		
		return getString("client_id", "SimpleMQTTViewer");
	}

	public String getUrl() {
		String host = getString("host", "iot.eclipse.org");
		int port = getInt("port", 1883);
		String url = "tcp://" + host + ":" + port;
		return url;
	}

	public String getTopic() {
		return getString("topic", "topic");
	}

	public boolean getCleanSession() {
		return getBoolean("clean_session", true);
	}

	public boolean getUseAuthentication() {
		return getBoolean("use_auth", false);
	}

	public String getUsername() {
		return getString("username", "username");
	}

	public char[] getPassword() {
		String pass = getString("password", "password");
		char [] buf = pass.toCharArray();
		pass = null;
		System.gc();
		return buf;
	}

	public int getConnectionTimeout() {
		return getInt("connection_timeout", 1);
	}
}
