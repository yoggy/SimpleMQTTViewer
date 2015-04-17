package net.sabamiso.android.simplemqttviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class SimpleMQTTViewerActivity extends Activity {

	HashMap<String, Item> map;
	ArrayList<Item> list;
	ItemAdapter adapter;
	ListView list_view;
	ActionBar action_bar;

	Handler handler = new Handler();

	MQTT mqtt;
	MyMQTTConfig config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		super.onCreate(savedInstanceState);

		mqtt = new MQTT(this);

		MyMQTTConfig.init(this);
		config = MyMQTTConfig.getInstance();

		setContentView(R.layout.activity_simple_mqttviewer);

		action_bar = getActionBar();

		map = new HashMap<String, Item>();
		list = new ArrayList<Item>();

		list_view = (ListView) findViewById(R.id.listView);
		adapter = new ItemAdapter(this, list);
		list_view.setAdapter(adapter);

		clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setConnectionStatus(false);
		clear();
		mqtt.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mqtt.stop();
	}

	public void clear() {
		map.clear();
		list.clear();
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
					action_bar.setLogo(R.drawable.icon_connect);
					action_bar.setTitle("connected, host=" + config.getHost());
				} else {
					action_bar.setLogo(R.drawable.icon_disconnect);
					action_bar.setTitle("disconnect...");
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
				updateItem(topic, message);
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
		} else if (id == R.id.action_clear) {
			clear();
			return true;
		} else if (id == R.id.action_publish_message) {
			showPublishMessageDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateItem(String topic, String message) {
		if (map.keySet().contains(topic)) {
			Item item = map.get(topic);
			item.setValue(message);
			adapter.notifyDataSetChanged();
		} else {
			Item item = new Item(topic, message);
			map.put(topic, item);
			list.add(item);
			Collections.sort(list, new ItemComparator());
			adapter.notifyDataSetChanged();
		}
	}

	private void showPublishMessageDialog() {
		if (mqtt == null || mqtt.isConnect() == false) {
			showAlertDialog("MQTT is not connected...");
			return;
		}

		// show custom dialog
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(
				R.layout.dialog_mqtt_publish_message,
				(ViewGroup) findViewById(R.id.layout_root));

		EditText editTopic = (EditText) layout
				.findViewById(R.id.editMQTTPublishTopic);
		EditText editMessage = (EditText) layout
				.findViewById(R.id.editMQTTPublishMessage);
		CheckBox checkRetained = (CheckBox) layout
				.findViewById(R.id.checkMQTTPublishRetained);

		editTopic.setText(config.getLastPublishTopic());
		editMessage.setText(config.getLastPublishMessage());
		checkRetained.setChecked(config.getLastPublishRetained());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Publish message");
		builder.setView(layout);
		builder.setPositiveButton("Publish", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EditText editTopic = (EditText) layout
						.findViewById(R.id.editMQTTPublishTopic);
				EditText editMessage = (EditText) layout
						.findViewById(R.id.editMQTTPublishMessage);
				CheckBox checkRetained = (CheckBox) layout
						.findViewById(R.id.checkMQTTPublishRetained);

				String topic = editTopic.getText().toString();
				String message = editMessage.getText().toString();
				boolean retained = checkRetained.isChecked();
				int qos = 0;
				mqtt.publish(topic, message, retained, qos);

				config.setLastPublishTopic(topic);
				config.setLastPublishMessage(message);
				config.setLastPublishRetained(retained);
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.create().show();
	}

	private void showAlertDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Alert");
		builder.setMessage(message);
		builder.create().show();
		return;
	}
}
