/**
 * 
 */
package yt.java.com.controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.service.YTCustomerService;
import yt.java.com.service.YTService;
import yt.java.com.service.YTTelecomService;
import yt.java.com.view.YTMainView;
import yt.java.com.view.YTServiceView;

/**
 * @author liguangming
 *
 */
public class YTCustomerController extends YTController {
	protected static final String DEVICE_TYPE = "customers";
	private static Logger _logger = LogManager.getLogger();
	private YTServiceView _view;
	private YTCustomerService _service;

	public YTCustomerController(YTMainView ui) {
		// View
		_view = ui.GetCustomerViewer();

		// Model
		_service = new YTCustomerService(_view); // customers' number
		_service.Init();

		// Others
		// bind ui event
		_view.SetConfigOnClick(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							new YTCustomerConfController(_service);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		_view.SetDetailOnClick(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							new YTCustomerDetailController(_service);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		_view.SetStartOnClick(new ServiceLauncher(_service));
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

	public void ProcessTelecomMessage(String topic, String message) {
		JSONObject retJsonObject = new JSONObject();
		retJsonObject.put("home_name", YTTelecomService.BRANCH_NAME);
		retJsonObject.put("device_type", "customers_count");
		retJsonObject.put("device_mfr", "dahua");

		// check branch name with topic
		String branchName = topic.split("/")[0];
		if (branchName.equals(YTTelecomService.BRANCH_NAME) != true) {
			_logger.info("Not this branch. " + branchName);
			return;
		}

		// check message parse
		JSONObject msgJsonObject = null;
		try {
			msgJsonObject = JSONObject.parseObject(message);
		} catch (Exception e) {
			_logger.warn("Parse message error. Msg: " + message.toString());
			return;
		}

		if (msgJsonObject == null) {
			_logger.info("Message null.");
			return;
		}
		
		if (msgJsonObject.getString("home_name").equals(YTTelecomService.BRANCH_NAME) != true) {
			_logger.warn("Message invalid home_name. " + message);
			return;
		}

		// check message device type
		if (msgJsonObject.getString("device_type").equals("customers_counter") != true) {
			_logger.warn("Message invalid device_type. " + message);
			return;
		}

		// check message time
		Long starttime = msgJsonObject.getLong("start_timestamp");
		Long endtime = msgJsonObject.getLong("end_timestamp");
		if (starttime > endtime) {
			retJsonObject.put("error_no", 10017);
			retJsonObject.put("error_msg", "Invalid parameters.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// check service
		if (IsRunning() != true) {
			retJsonObject.put("error_no", 10001);
			retJsonObject.put("error_msg", "Service customers is not running. ");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}
		
		// get data
		try {
			JSONArray ret = _service.GetDeviceStatus(starttime * 1000, endtime * 1000);
			_logger.debug("customer device status: " + ret.toString());
			retJsonObject.put("status", ret.toString());
		} catch (Exception e) {
			retJsonObject.put("error_no", 10002);
			retJsonObject.put("error_msg", "Service customers data parse error.");
		}
		
		_logger.debug("publish topic:" + YTTelecomService.STATUS_PUB_TOPIC);
		_logger.debug("publish message: " + retJsonObject.toString());
		YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
	}
}
