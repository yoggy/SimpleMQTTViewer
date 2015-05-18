package net.sabamiso.android.simplemqttviewer;

import java.util.Vector;

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

	public String getHost() {
		return getString("host", "iot.eclipse.org");
	}

	public int getPort() {
		return getInt("port", 1883);
	}
	
	public String getUrl() {
		String url = "tcp://" + getHost() + ":" + getPort();
		return url;
	}

	public String [] getTopic() {
		String default_value = "topic";
		
		String str = getString("topic", default_value);
		String [] strs = str.split(",", 0);
		
		if (strs == null || strs.length == 0 || "".equals(strs[0]) ) {
			setDefaultTopic(default_value);
			String [] res = new String[1];
			res[0] = default_value;
			return res;
		}
		
		Vector<String> v = new Vector<String>();
		for (int i = 0; i < strs.length; ++i) {
			if (strs[i] == null) continue;
			
			strs[i] = strs[i].trim();
			if (strs[i].length() == 0) continue;
			
			v.add(strs[i]);
		}

		if (v.size() == 0) {
			setDefaultTopic(default_value);
			String [] res = new String[1];
			res[0] = default_value;
			return res;
		}
		
		String [] res = v.toArray(new String[0]);
		
		return res;
	}

	private void setDefaultTopic(String val) {
		setString("topic", val);
	}

	public String getClientId() {		
		return getString("client_id", "SimpleMQTTViewer");
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
		return getInt("connection_timeout", 5);
	}
	
	public String getLastPublishTopic() {
		return getString("last_publish_topic", "topic");
	}

	public void setLastPublishTopic(String val) {
		if (val == "") {
			setString("last_publish_topic", "topic");
		}
		else {
			setString("last_publish_topic", val);
		}
	}

	public String getLastPublishMessage() {
		return getString("last_publish_message", "message");
	}
	
	public void setLastPublishMessage(String val) {
		if (val == "") {
			setString("last_publish_message", "");
		}
		else {
			setString("last_publish_message", val);
		}
	}

	public boolean getLastPublishRetained() {
		return getBoolean("last_publish_retained", false);
	}

	public void setLastPublishRetained(boolean flag) {
		setBoolean("last_publish_retained", flag);
	}
}
