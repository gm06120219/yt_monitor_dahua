package yt.java.com.controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.alibaba.fastjson.JSONObject;

import yt.java.com.listener.YTTelecomMessageListener;
import yt.java.com.service.YTService;
import yt.java.com.service.YTTelecomService;
import yt.java.com.view.YTMainView;
import yt.java.com.view.YTServiceView;

public class YTTelecomController extends YTController {
	private static Logger _logger = LogManager.getLogger();

	private YTServiceView _view;
	private YTTelecomService _service;

	public YTTelecomController(YTMainView ui) {
		// View
		_view = ui.GetTelecomViewer();

		// Model
		_service = new YTTelecomService(_view);
		_service.Init();

		// Bind event
		_service.AddMessageListener(new YTTelecomMessageListener() {
			@Override
			// the composition of topic: branch name / room number / command
			public void Process(String topic, String message) {
				// categorizing by topic
				 
				if (topic.split("/").length < 3) {
					_logger.warn("Topic wrong. Topic: " + topic);
					return;
				}
				
				String device_type = "";
				try{
					JSONObject msgJsonObject = JSONObject.parseObject(message);	
					device_type = msgJsonObject.getString("device_type");
				} catch(Exception e) {
					device_type = "EMPTY";
					_logger.warn("Parse message failure, no device_type object.");
				}
				
				_logger.info("Get message>>> topic: " + topic + " message: " + message);

				// switch device type
				switch (device_type) {
				// customers' number
				case YTCustomerController.DEVICE_TYPE:
					YTMainController.customerController.ProcessTelecomMessage(topic, message);
					break;
					
				case YTAccessController.DEVICE_TYPE:
					YTMainController.accessController.ProcessTelecomMessage(topic, message);
					break;

				default:
					_logger.warn("Unexpected device type: " + device_type);
				}
			}
		});
		
		// Bind ui event
		_view.SetStartOnClick(new ServiceLauncher(_service));
		
//		 _view.SetDetailOnClick(new ActionListener() {
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					EventQueue.invokeLater(new Runnable() {
//						public void run() {
//							try {
//								new YTTelecomDetailController(_service);
//							} catch (Exception e) {
//								_logger.warn("Thread sleep exception: " + e.toString());
//							}
//						}
//					});
//				}
//			});
		
		_view.SetConfigOnClick(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							new YTTelecomConfController(_service);
						} catch (Exception e) {
							_logger.warn("Thread sleep exception: " + e.toString());
						}
					}
				});
			}
		});
	}

	public boolean IsRunning() {
		boolean ret = false;
		switch (_service.serviceStatus) {
		case YTService.SERVICE_STATUS.START:
		case YTService.SERVICE_STATUS.WARN:
			ret = true;
			break;
		default:
			ret = false;
			break;
		}
		return ret;
	}

	public void Publish(String topic, String message) {
		if (IsRunning() == true) {
			try {
				_service.Publish(topic, message);
			} catch (MqttException e) {
				_logger.warn("Publish failure. " + e.getMessage());
			}
		}
	}

}
