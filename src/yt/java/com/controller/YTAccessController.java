package yt.java.com.controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.service.YTAccessService;
import yt.java.com.service.YTService;
import yt.java.com.service.YTTelecomService;
import yt.java.com.view.YTMainView;
import yt.java.com.view.YTServiceView;

public class YTAccessController extends YTController {
	public final static String DEVICE_TYPE = "access_control";
	private static Logger _logger = LogManager.getLogger();
	private YTServiceView _view;
	private YTAccessService _service;

	public YTAccessController(YTMainView ui) {
		// view
		_view = ui.GetAccessViewer();

		// model
		_service = new YTAccessService(_view);
		_service.Init();

		// bind ui event
		_view.SetConfigOnClick(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						new YTAccessConfController(_service);
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
							new YTAccessDetailController(_service);
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
		_logger.debug("process telecom message.");

		String branchName = topic.split("/")[0];
		String command = topic.split("/")[2];

		// check branch name with topic
		if (branchName.equals(YTTelecomService.BRANCH_NAME) != true) {
			_logger.info("Not this branch. " + branchName);
			return;
		}

		// process message
		switch (command) {
		case YTTelecomService.SET_SUB_TOPIC_LAST:
			ProcessSetMessage(message);
			break;
		case YTTelecomService.GET_SUB_TOPIC_LAST:
			ProcessGetMessage(message);
			break;
		case YTTelecomService.CONF_SUB_TOPIC_LAST:
			ProcessConfMessage(message);
			break;
		default:
			break;
		}
	}

	private void ProcessSetMessage(String message) {
		JSONObject retJsonObject = new JSONObject();
		retJsonObject.put("home_name", YTTelecomService.BRANCH_NAME);
		retJsonObject.put("device_type", "access_control");
		retJsonObject.put("device_mfr", "dahua");

		// check service
		if (IsRunning() != true) {
			_logger.error("Service not running! ");
			retJsonObject.put("error_no", 10001);
			retJsonObject.put("error_msg", "Service Access-Control is not running. ");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// check message parse
		JSONObject msg_json = null;
		try {
			msg_json = JSONObject.parseObject(message);
		} catch (Exception e) {
			_logger.warn("Parse message error. Msg: " + message.toString());
			retJsonObject.put("error_no", 10022);
			retJsonObject.put("error_msg", "Service Access-Control: parse parameter failure.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		if (msg_json == null) {
			_logger.info("Message null.");
			retJsonObject.put("error_no", 10017);
			retJsonObject.put("error_msg", "Service Access-Control: invalid parameter.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		if (msg_json.getString("home_name").equals(YTTelecomService.BRANCH_NAME) != true) {
			_logger.warn("Message invalid home_name. " + message);
			retJsonObject.put("error_no", 10018);
			retJsonObject.put("error_msg",
					"Service Access-Control: wrong parameter. home_name : " + msg_json.getString("home_name"));
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// check message device type
		if (msg_json.getString("device_type").equals(DEVICE_TYPE) != true) {
			_logger.warn("Message invalid device_type. " + message);
			retJsonObject.put("error_no", 10018);
			retJsonObject.put("error_msg",
					"Service Access-Control: wrong parameter. device_type : " + msg_json.getString("device_type"));
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// get parameters
		JSONObject control_info_json;
		boolean is_open;
		String ip;
		int channel;
		try {
			control_info_json = msg_json.getJSONObject("device_control");
			is_open = control_info_json.getBooleanValue("is_open");
			ip = control_info_json.getString("ip");
			channel = control_info_json.getIntValue("channel");
		} catch (Exception e) {
			_logger.warn("Message does not have ip | channel | is_open parameters. " + message);
			retJsonObject.put("error_no", 10019);
			retJsonObject.put("error_msg", "Service Access-Control: empty parameter.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		String nickname = _service.GetNicknameByIpAndChannel(ip, channel);
		JSONObject ret = _service.SetDeviceStatus(nickname, is_open);
		if (ret != null) {
			JSONArray status = new JSONArray();
			status.add(ret);
			retJsonObject.put("status", status);
		} else {
			_logger.warn("Set access device failure");
			retJsonObject.put("error_no", 20001);
			retJsonObject.put("error_msg", "Service internal error.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}
		_logger.debug("publish topic:" + YTTelecomService.STATUS_PUB_TOPIC);
		_logger.debug("publish message: " + retJsonObject.toString());
		YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
	}

	private void ProcessGetMessage(String message) {
		JSONObject retJsonObject = new JSONObject();
		retJsonObject.put("home_name", YTTelecomService.BRANCH_NAME);
		retJsonObject.put("device_type", "access_control");
		retJsonObject.put("device_mfr", "dahua");

		// check service
		if (IsRunning() != true) {
			_logger.error("Service not running! ");
			retJsonObject.put("error_no", 10001);
			retJsonObject.put("error_msg", "Service Access-Control is not running. ");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// check message parse
		JSONObject msg_json = null;
		try {
			msg_json = JSONObject.parseObject(message);
		} catch (Exception e) {
			_logger.warn("Parse message error. Msg: " + message.toString());
			retJsonObject.put("error_no", 10022);
			retJsonObject.put("error_msg", "Service Access-Control: parse parameter failure.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		if (msg_json == null) {
			_logger.info("Message null.");
			retJsonObject.put("error_no", 10017);
			retJsonObject.put("error_msg", "Service Access-Control: invalid parameter.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		if (msg_json.getString("home_name").equals(YTTelecomService.BRANCH_NAME) != true) {
			_logger.warn("Message invalid home_name. " + message);
			retJsonObject.put("error_no", 10018);
			retJsonObject.put("error_msg",
					"Service Access-Control: wrong parameter. home_name : " + msg_json.getString("home_name"));
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}

		// check message device type
		if (msg_json.getString("device_type").equals(DEVICE_TYPE) != true) {
			_logger.warn("Message invalid device_type. " + message);
			retJsonObject.put("error_no", 10018);
			retJsonObject.put("error_msg",
					"Service Access-Control: wrong parameter. device_type : " + msg_json.getString("device_type"));
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}
		
		// response
		JSONArray ret = _service.GetAllDeviceStatus();
		if (ret != null) {
			retJsonObject.put("status", ret.toJSONString());
		} else {
			_logger.warn("Set access device failure");
			retJsonObject.put("error_no", 20001);
			retJsonObject.put("error_msg", "Service internal error.");
			YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());
			return;
		}
		_logger.debug("publish topic:" + YTTelecomService.STATUS_PUB_TOPIC);
		_logger.debug("publish message: " + retJsonObject.toString());
		YTMainController.telecomController.Publish(YTTelecomService.STATUS_PUB_TOPIC, retJsonObject.toJSONString());

	}

	private void ProcessConfMessage(String message) {
		// TODO Auto-generated method stub
	}
}
