package net.sabamiso.android.simplemqttviewer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

class MQTTThread extends Thread {
	SimpleMQTTViewerActivity activity;
	boolean break_flag;

	MqttClient client;
	MemoryPersistence persistence = new MemoryPersistence();
    MqttConnectOptions opts = new MqttConnectOptions();
	
	public MQTTThread(SimpleMQTTViewerActivity activity) {
		this.activity = activity;
	}

	public void setBreakFlag(boolean val) {
		break_flag = val;
	}
	
	boolean isConnect() {
		if (client == null) return false;
		return true;
	}
	
	boolean connect() {
		if (client != null) return true;
		
		try {
			client = new MqttClient("tcp://iot.eclipse.org:1883", "SimpleMQTTViewer", persistence);
			opts.setCleanSession(true);
			client.connect(opts);
		} catch (MqttException e) {
			e.printStackTrace();
			client = null;
		}
		
		activity.setConnectionStatus(true);
		
		return false;
	}
	
	void disconnect() {
		if (client != null) {
			try {
				client.disconnect(100);
			} catch (MqttException e) {
				e.printStackTrace();
			}
			client = null;
		}

		activity.setConnectionStatus(false);
	}
	
	@Override
	public void run() {
		while(!break_flag) {
			if (isConnect() == false) {
				boolean rv = connect();
				if (rv == false) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		disconnect();
	}
}

public class MQTT {
	SimpleMQTTViewerActivity activity;
	MQTTThread thread;

	public MQTT(SimpleMQTTViewerActivity activity) {
		this.activity = activity;
	}

	void start() {
		if (thread != null)
			return;

		thread = new MQTTThread(activity);
		thread.start();
	}

	void stop() {
		if (thread == null) return;
		
		thread.setBreakFlag(true);
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		thread = null;
	}

}
