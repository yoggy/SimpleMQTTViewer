package net.sabamiso.android.simplemqttviewer;

import java.util.ArrayList;

import net.sabamiso.android.util.Config;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

public class SimpleMQTTViewerActivity extends Activity {

	ArrayList<Item> list;
	ItemAdapter adapter;
	ListView  list_view;
	
	Item item_connection_status;

	Handler handler = new Handler();
	
	MQTT mqtt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		super.onCreate(savedInstanceState);

		mqtt = new MQTT(this);
		
		Config.init(this);

		setContentView(R.layout.activity_simple_mqttviewer);
		
		item_connection_status = new Item("connection status", "disconnect");

		list = new ArrayList<Item>();
		list_view = (ListView) findViewById(R.id.listView);
		adapter = new ItemAdapter(this, list);
		list_view.setAdapter(adapter);		
		clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mqtt.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mqtt.stop();
	}

	public void clear() {
		list.clear();
		list.add(item_connection_status);
		adapter.notifyDataSetChanged();
	}
	
	public void setConnectionStatus(boolean flag) {
		handler.post(new Runnable() {
			boolean flag_val;
			public Runnable setFlag(boolean flag) {
				this.flag_val = flag;
				return this;
			}

			@Override
			public void run() {
				if (flag_val == true) {
					item_connection_status.setValue("connect");
				}
				else {
					item_connection_status.setValue("disconnect");
				}
				adapter.notifyDataSetChanged();
			}

		}.setFlag(flag));
	}

	public void onReceiveMessage(String topic, String message) {
		handler.post(new Runnable() {
			String topic, message;
			public Runnable setMessage(String topic, String message) {
				this.topic = topic;
				this.message = message;
				return this;
			}

			@Override
			public void run() {
				adapter.add(new Item(topic, message));
			}

		}.setMessage(topic, message));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.simple_mqttviewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		}
		else if (id == R.id.action_clear) {
			clear();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
