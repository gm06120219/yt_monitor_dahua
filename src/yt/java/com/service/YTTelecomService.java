package yt.java.com.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import yt.java.com.listener.YTTelecomMessageListener;
import yt.java.com.view.YTServiceView;

public class YTTelecomService extends YTService {
	private static Logger _logger = LogManager.getLogger();

	// publish topic
	public static String BRANCH_NAME = "daningdian";
	public static String ROOM_NO = "public";
	public final static String GET_SUB_TOPIC_LAST = "device_query"; // listener topic keywords
	public final static String SET_SUB_TOPIC_LAST = "device_cmd"; // listener topic keywords
	public final static String CONF_SUB_TOPIC_LAST = "device_cfg"; // listener topic keywords
	public static String[] TOPIC_LIST = { BRANCH_NAME + "/" + ROOM_NO + "/" + GET_SUB_TOPIC_LAST,
			BRANCH_NAME + "/" + ROOM_NO + "/" + SET_SUB_TOPIC_LAST,
			BRANCH_NAME + "/" + ROOM_NO + "/" + CONF_SUB_TOPIC_LAST };
	public static String STATUS_PUB_TOPIC = BRANCH_NAME + "/" + ROOM_NO + "/" + "device_status";
	public static String CONFIG_PUB_TOPIC = BRANCH_NAME + "/" + ROOM_NO + "/" + "device_cfg_detail";

	private String _url = "iot.aijoyride.com";
	private int _port = 8883;
	private String _clientId = BRANCH_NAME + "_dahua_monitor";

	private String _broker;
	private final String _username = "aio_vvc001";
	private final String _password = "aioisaiobject007";

	// parameters
	private MqttConnectOptions _options;
	private IMqttMessageListener _listener;
	private YTTelecomMessageListener _messageListener;
	private YTServiceView _ui;

	public MqttClient client;

	public YTTelecomService(YTServiceView ui) {
		_broker = "ssl://" + _url + ":" + _port;
		_ui = ui;

		// options
		_options = new MqttConnectOptions();
		_options.setUserName(_username);
		_options.setPassword(_password.toCharArray());
		_options.setCleanSession(true);

		// listener
		_listener = new IMqttMessageListener() {
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				_logger.debug("Topic: " + topic + ", Message: " + message.toString());
				// screening message
				if (_messageListener != null) {
					if (topic.contains(GET_SUB_TOPIC_LAST) == true || topic.contains(SET_SUB_TOPIC_LAST) == true
							|| topic.contains(CONF_SUB_TOPIC_LAST) == true) {
						_messageListener.Process(topic, message.toString());
					}
				}
			}
		};
	}

	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			inputStream = new FileInputStream("./res/telecom.properties");
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			// TODO Refresh ui status
			_logger.error("Not found config properties file.");
			return;
		} catch (IOException e) {
			// TODO Refresh ui status
			_logger.error("Read error with properties file.");
			return;
		}

		// Prase config
		BRANCH_NAME = properties.getProperty("BRANCH_NAME");
		ROOM_NO = properties.getProperty("ROOM_NO");
		_clientId = properties.getProperty("CLIENT_ID");
		_url = properties.getProperty("URL");
		_port = Integer.valueOf(properties.getProperty("PORT"));
	}

	public void Init() {
		// init service monitor
		_monitor = new Runnable() {

			@Override
			public void run() {
				_logger.info("Telecom Service >>> START!");

				// parse config
				LoadConfig();
				_ui.SetCount("1");

				// init client and start
				try {
					client = new MqttClient(_broker, _clientId);
					client.connect(_options);

					IMqttMessageListener[] messageListeners = new IMqttMessageListener[TOPIC_LIST.length];
					for (int i = 0; i < messageListeners.length; i++) {
						messageListeners[i] = _listener;
					}
					client.subscribe(TOPIC_LIST, messageListeners);
					_ui.SetStatus(serviceStatus);
				} catch (MqttException me) {
					_logger.warn("MQTT excep: " + me);
					me.printStackTrace();
				}

				while (true) {
					// Stop
					if (serviceStatus == SERVICE_STATUS.STOP) {
						_logger.info("Telecom Service >>> STOP!");
						try {
							client.disconnect();
						} catch (MqttException e) {
							_logger.warn("Thread sleep exception: " + e.toString());
						}
						_ui.SetStatus(serviceStatus);
						break;
					}

					// every second check once
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						_logger.warn("Thread sleep exception: " + e.toString());
					}
				}
			}

		};
	}

	public void AddMessageListener(YTTelecomMessageListener listener) {
		// on message listener
		_messageListener = listener;
	}

	public void Publish(String topic, String message) throws MqttPersistenceException, MqttException {
		if (client != null) {
			MqttMessage mMessage = new MqttMessage(message.getBytes());
			_logger.debug("topic:" + topic + "\tmessage: " + message);
			client.publish(topic, mMessage);
		} else {
			throw new MqttException(0);
		}
	}
}
