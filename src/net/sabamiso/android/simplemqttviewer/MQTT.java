package net.sabamiso.android.simplemqttviewer;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

class MQTTThread extends Thread implements MqttCallback {
	SimpleMQTTViewerActivity activity;
	boolean break_flag;

	MqttClient client;
	MemoryPersistence persistence = new MemoryPersistence();
	MqttConnectOptions opts = new MqttConnectOptions();

	MyMQTTConfig config;

	public MQTTThread(SimpleMQTTViewerActivity activity) {
		this.activity = activity;
		this.config = MyMQTTConfig.getInstance();
		setName("MQTTThread");
	}

	public void setBreakFlag(boolean val) {
		break_flag = val;
	}

	boolean isConnect() {
		if (client == null)
			return false;
		if (client.isConnected() == false) {
			disconnect();
			return false;
		}
		return true;
	}

	boolean connect() {
		if (client != null)
			return true;

		try {
			client = new MqttClient(config.getUrl(), config.getClientId(),
					persistence);
			opts.setCleanSession(config.getCleanSession());
			opts.setConnectionTimeout(config.getConnectionTimeout());
			if (config.getUseAuthentication()) {
				opts.setUserName(config.getUsername());

				char[] pass = config.getPassword();
				opts.setPassword(pass);
			}
			client.connect(opts);
			client.setCallback(this);
			client.subscribe(config.getTopic());
		} catch (MqttException e) {
			e.printStackTrace();
			client = null;
			return false;
		}

		activity.setConnectionStatus(true);

		return true;
	}

	void disconnect() {
		if (client != null) {
			try {
				client.disconnect(10);
			} catch (MqttException e) {
				e.printStackTrace();
			}
			client = null;
		}

		activity.setConnectionStatus(false);
	}

	@Override
	public void run() {
		while (!break_flag) {
			if (isConnect() == false) {
				connect();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		disconnect();
	}

	public void quit() {
		break_flag = true;
		disconnect();
		this.interrupt();

		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		disconnect();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		activity.onReceiveMessage(topic, msg.toString());
	}

	public boolean publish(String topic, String message, boolean retained, int qos) {
		if (isConnect() == false) return false;
		if (qos < 0 || 2 < qos) return false;
		if (message == null) return false;
		
		try {
			MqttMessage mqtt_msg = new MqttMessage();
			mqtt_msg.setPayload(message.getBytes());
			mqtt_msg.setRetained(retained);
			mqtt_msg.setQos(qos);
			client.publish(topic, mqtt_msg);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
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
		if (thread == null)
			return;

		thread.quit();
		thread = null;
	}

	public boolean isConnect() {
		if (thread == null)
			return false;
		return thread.isConnect();
	}

	public boolean publish(String topic, String message, boolean retained, int qos) {
		if (thread == null) return false;
		return thread.publish(topic, message, retained, qos);
	}
}
