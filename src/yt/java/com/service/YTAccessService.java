package yt.java.com.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.dahua.YTDahuaAccess;
import yt.java.com.tools.JSONTools;
import yt.java.com.view.YTServiceView;

public class YTAccessService extends YTService {
	private static Logger _logger = LogManager.getLogger();

	private YTDahuaAccess[] _accessList;
	private YTServiceView _ui;
	
	private int _count; // count of access control client
	private JSONArray _accessInfoList;
	
	public YTAccessService(YTServiceView ui) {
		_ui = ui;
	}

	private JSONArray LoadConfig() {
		JSONArray infoList = new JSONArray();
		
		// get content of configuration
		Properties tempProperties;
		InputStreamReader isr = null;
		try {
			tempProperties = new Properties();
			tempProperties.load(new InputStreamReader(new FileInputStream("./res/access.properties"), "UTF-8"));
		} catch (FileNotFoundException e) {
			// TODO Refresh ui status
			_logger.error("Not found config properties file.");
			return null;
		} catch (IOException e) {
			// TODO Refresh ui status
			_logger.error("Read error with properties file.");
			return null;
		} catch (Exception e) {
			_logger.error("Config parse error.");
			return null;
		}
		_count = Integer.parseInt(tempProperties.getProperty("COUNT"));
		String[] nicknameList = tempProperties.getProperty("NICKNAME_LIST").split(",");
		String[] ipList = tempProperties.getProperty("IP_LIST").split(",");
		String[] channelList = tempProperties.getProperty("CHANNEL_LIST").split(",");
		String[] portList = tempProperties.getProperty("PORT_LIST").split(",");
		String[] usernameList = tempProperties.getProperty("USERNAME_LIST").split(",");
		String[] passwordList = tempProperties.getProperty("PASSWORD_LIST").split(",");
		
		// {
		//   ip: "",
		//   port: 0,
		//   username: "",
		//   password: "",
		//   door_list: [{nickname: "", channel: 0}]
		// }
		for (int i = 0; i < _count; i++) {
			int indexOfIp = JSONTools.HasKeyValue(infoList, "ip", ipList[i]);
			if(indexOfIp != -1) {
				// add channel info to door list
				JSONObject temp = new JSONObject();
				temp.put("nickname", nicknameList[i]);
				temp.put("channel", Integer.valueOf(channelList[i]));
				infoList.getJSONObject(indexOfIp).getJSONArray("door_list").add(temp);
			} else {
				// add controller info to list
				JSONObject temp = new JSONObject();

				temp.put("ip", ipList[i]);
				temp.put("port", Integer.valueOf(portList[i]));
				temp.put("username", usernameList[i]);
				temp.put("password", passwordList[i]);
				JSONArray tempArray = new JSONArray();
				JSONObject tempDoor = new JSONObject();
				tempDoor.put("nickname", nicknameList[i]);
				tempDoor.put("channel", Integer.valueOf(channelList[i]));
				tempArray.add(tempDoor);
				temp.put("door_list", tempArray);
				infoList.add(temp);
			}
		}
		_logger.debug("Access Client: " + infoList.toJSONString());
		return infoList;	
	}
	
	@Override
	public void Init() {
		super.Init();
		// init service monitor
		_monitor = new Runnable() {

			private int _accessDisconnectCount;

			@Override
			public void run() {
				_logger.info("Access Service >>> START!");
				
				// parse config
				_accessInfoList = LoadConfig();
				_count = _accessInfoList.size();
				
				_ui.SetCount(String.valueOf(_count));
				
				// init accesser' list
				_accessList = new YTDahuaAccess[_count];
				for (int i = 0; i < _count; i++) {
					_accessList[i] = new YTDahuaAccess(_accessInfoList.getJSONObject(i));
				}
				
				// Start
				for (int i = 0; i < _accessList.length; i++) {
					_accessList[i].ConnectStart();
				}
				_ui.SetStatus(serviceStatus);

				while (true) {
					// Check outline device
					_accessDisconnectCount = 0;
					for (int i = 0; i < _accessList.length; i++) {
						if (_accessList[i].stop) {
							_accessDisconnectCount++;
						}
					}

					// Warning & Broken
					if (_accessDisconnectCount > 0) {
						if (_accessDisconnectCount == _count) {
							serviceStatus = YTService.SERVICE_STATUS.WARN;
							_ui.SetStatus(serviceStatus);
						} else {
							for (int i = 0; i < _accessList.length; i++) {
								_accessList[i].ConnectStop();
							}
							serviceStatus = YTService.SERVICE_STATUS.BROKEN;
							_ui.SetStatus(serviceStatus);
							break;
						}
					}

					// Stop
					if (serviceStatus == SERVICE_STATUS.STOP) {
						_logger.info("Access Service >>> STOP!");
						for (int i = 0; i < _accessList.length; i++) {
							_accessList[i].ConnectStop();
						}
						_accessList = null;
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

	public JSONObject SetDeviceStatus(String nickname, boolean isOpen) {
		_logger.debug("set access device by: " + nickname + " , set open: " + isOpen);
		YTDahuaAccess client = null;
		int channel = -1;
		
		// find client
		for (int i = 0; i < _accessList.length; i++) {
			channel = _accessList[i].FindChannelByNickname(nickname);
			if(channel != -1) {
				client = _accessList[i];
				break;
			}
		}
		
		if(client == null) {
			_logger.warn("Cannot get access device by nickname: " + nickname);
			return null;
		}
		
		// set device status
		boolean ret = client.SetStatus(isOpen, channel);

		JSONObject retJson = new JSONObject();
		if(ret) {
			retJson.put("ip", client.GetIp());
			retJson.put("channel", channel);
			retJson.put("nickname", nickname);
			retJson.put("online", true);
			retJson.put("is_open", isOpen);
		} else {
			_logger.warn("Set access device status failure");
			return null;
		}
		
		return retJson;
	}

	public YTDahuaAccess GetClientByNickname(String nickname) {
		YTDahuaAccess ret = null;
		for (int i = 0; i < _accessList.length; i++) {
			if(_accessList[i].FindChannelByNickname(nickname) != -1) {
				ret = _accessList[i];
				break;
			}
		}
		
		return ret;
	}

	public String GetNicknameByIpAndChannel(String ip, int channel) {
		for (int i = 0; i < _accessList.length; i++) {
			if(_accessList[i].GetIp().equals(ip)) {
				return _accessList[i].GetNicknameByChannel(channel);
			}
		}
		return null;
	}
	
	public JSONArray GetAllDeviceStatus() {
		JSONArray retArray = new JSONArray();
		for (int i = 0; i < _accessList.length; i++) {
			JSONArray tempArray = _accessList[i].GetAllStatus();
			for (int j = 0; j < tempArray.size(); j++) {
				retArray.add(tempArray.getJSONObject(j));
			}
		}
		return retArray;
	}
	
	public JSONObject GetDeviceStatus() {
		return null;
	}
}
