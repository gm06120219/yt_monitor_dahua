package yt.java.com.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.dahua.YTDahuaCustomer;
import yt.java.com.view.YTServiceView;

public class YTCustomerService extends YTService {
	private static Logger _logger = LogManager.getLogger();
	private YTDahuaCustomer[] _customersList;
	private int _customersCount = 0;
	private int _customersDisconnectCount = 0;
	private String[] _ipList;
	private int[] _portList;
	private String[] _usernameList;
	private String[] _passwordList;
	private YTServiceView _ui;

	public YTCustomerService(YTServiceView view) {
		_ui = view;
	}
	
	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			inputStream = new FileInputStream("./res/customers.properties");
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
		_customersCount = Integer.parseInt(properties.getProperty("COUNT"));
		String ipListString = properties.getProperty("IP_LIST");
		String portListString = properties.getProperty("PORT_LIST");
		String usernameListString = properties.getProperty("USERNAME_LIST");
		String passwordListString = properties.getProperty("PASSWORD_LIST");
		_ipList = ipListString.split(",");
		String[] tempPortList = portListString.split(",");
		_portList = new int[tempPortList.length];
		for (int i = 0; i < tempPortList.length; i++) {
			_portList[i] = Integer.parseInt(tempPortList[i]);
		}
		_usernameList = usernameListString.split(",");
		_passwordList = passwordListString.split(",");
		
		// TODO process parse exception
	}

	@Override
	public void Init() {
		// init service monitor
		_monitor = new Runnable() {
			@Override
			public void run() {
				_logger.info("Customer Service >>> START!");
				
				// parse config
				LoadConfig();
				_ui.SetCount(String.valueOf(_customersCount));
				
				// init customers' list
				_customersList = new YTDahuaCustomer[_customersCount];
				for (int i = 0; i < _customersList.length; i++) {
					_customersList[i] = new YTDahuaCustomer(_ipList[i], _portList[i], _usernameList[i], _passwordList[i]);
				}
				
				// Start
				for (int i = 0; i < _customersList.length; i++) {
					_customersList[i].ConnectStart();
				}
				_ui.SetStatus(serviceStatus);

				while (true) {
					// Check outline device
					_customersDisconnectCount = 0;
					for (int i = 0; i < _customersList.length; i++) {
						if (_customersList[i].stop) {
							_customersDisconnectCount++;
						}
					}

					// Warning & Broken
					if (_customersDisconnectCount > 0) {
						if (_customersDisconnectCount == _customersCount) {
							serviceStatus = YTService.SERVICE_STATUS.WARN;
							_ui.SetStatus(serviceStatus);
						} else {
							for (int i = 0; i < _customersList.length; i++) {
								_customersList[i].ConnectStop();
							}
							serviceStatus = YTService.SERVICE_STATUS.BROKEN;
							_ui.SetStatus(serviceStatus);
							break;
						}
					}

					// Stop
					if (serviceStatus == SERVICE_STATUS.STOP) {
						_logger.info("Customer Service >>> STOP!");
						for (int i = 0; i < _customersList.length; i++) {
							_customersList[i].ConnectStop();
						}
						_customersList = null;
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

	public JSONArray GetDeviceStatus(Long startTimestamp, Long endTimestamp) throws ParseException {
		JSONArray ret = new JSONArray();
		if (serviceStatus != SERVICE_STATUS.STOP && serviceStatus != SERVICE_STATUS.BROKEN) {
			for (int i = 0; i < _customersList.length; i++) {
				JSONArray tempRet = _customersList[i].QueryCustomersNumberPerDay(startTimestamp, endTimestamp);
				int enter = 0;
				int exit = 0;
				for (int j = 0; j < tempRet.size(); j++) {
					enter += tempRet.getJSONObject(j).getInteger("enter");
					exit += tempRet.getJSONObject(j).getInteger("exit");
				}
				
				JSONObject temp = new JSONObject();
				temp.put("ip", _customersList[i].GetIp());
				temp.put("channel", _customersList[i].GetChannel());
				temp.put("name", ""); // TODO
				temp.put("enter", enter);
				temp.put("exit", exit);
				temp.put("connect", _customersList[i].GetConnectStatus());
				ret.add(temp);
			}
		}
		return ret;
	}
}