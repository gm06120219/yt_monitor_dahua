package yt.java.com.dahua;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import main.java.com.netsdk.demo.module.LoginModule;
import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.NetSDKLib.CFG_ACCESS_EVENT_INFO;
import main.java.com.netsdk.lib.NetSDKLib.CtrlType;
import main.java.com.netsdk.lib.NetSDKLib.EM_OPEN_DOOR_TYPE;
import main.java.com.netsdk.lib.NetSDKLib.LLong;
import main.java.com.netsdk.lib.NetSDKLib.NET_CTRL_ACCESS_OPEN;
import main.java.com.netsdk.lib.NetSDKLib.NET_DOOR_STATUS_INFO;
import main.java.com.netsdk.lib.NetSDKLib.NET_TIME;
import main.java.com.netsdk.lib.NetSDKLib.fMessCallBack;
import main.java.com.netsdk.lib.ToolKits;

public class YTDahuaAccess extends YTDahuaConnecter {
	private static Logger _logger = LogManager.getLogger();

	private String _ip;
	private String _username;
	private Integer _port;
	private String _password;
	private JSONArray _doorList;
	private int[] _channelList;

	private YTDahuaClient _client;
	private DefaultSubscribeCallback _subscribeCallback;
	private LLong _loginHandle;

	private boolean _is_listening = false;

	public YTDahuaAccess(JSONObject info) {
		_ip = info.getString("ip");
		_port = info.getInteger("port");
		_username = info.getString("username");
		_password = info.getString("password");
		_doorList = info.getJSONArray("door_list");
		_channelList = new int[_doorList.size()];
		for (int i = 0; i < _doorList.size(); i++) {
			_channelList[i] = _doorList.getJSONObject(i).getInteger("channel");
		}

		_client = new YTDahuaClient();
		_subscribeCallback = new DefaultSubscribeCallback();
	}

	public String GetIp() {
		return _ip;
	}

	/**
	 * Connect
	 * 
	 * @param Callback to process subscribe event, implements interface
	 *                 fVideoStatSumCallBack / 订阅事件触发后的回调，实现接口fVideoStatSumCallBack
	 * @return
	 */
	// TODO registering disconnect callback function
	// TODO process login exception
	private boolean Connect() {
		boolean ret = false;
		_loginHandle = this._client.Login(this._ip, this._port, this._username, this._password);
		if (_loginHandle.longValue() == 0) {
			_logger.warn("Login Failure. By IP: " + _ip);
			ret = false;
		} else {
			_logger.info("Login Success. By IP: " + _ip);
			ret = true;
		}
		return ret;
	}

	/**
	 * Disconnect
	 * 
	 * @return
	 */
	// TODO add disconnect log
	private void Disconnect() {
		_client.Logout();
		_client.Clean();
	}

	public int FindChannelByNickname(String nickname) {
		for (int i = 0; i < _doorList.size(); i++) {
			if (_doorList.getJSONObject(i).getString("nickname").equals(nickname) == true) {
				return _doorList.getJSONObject(i).getInteger("channel");
			}
		}

		return -1;
	}
	

	public String GetNicknameByChannel(int channel) {
		for (int i = 0; i < _doorList.size(); i++) {
			if (_doorList.getJSONObject(i).getIntValue("channel") == channel) {
				return _doorList.getJSONObject(i).getString("nickname");
			}
		}
		return null;
	}

	/**
	 * Open door or close door
	 * 
	 * @param isOpen
	 */
	public boolean SetStatus(boolean isOpen, int channel) {
		_logger.info("Access Control: IP- " + _ip + " Channel-" + channel + " Open-" + isOpen);
		int emType;

		NET_CTRL_ACCESS_OPEN param = new NET_CTRL_ACCESS_OPEN();
		param.nChannelID = channel - 1;
		param.szTargetID = null;
		param.szUserID = new byte[32];
		param.emOpenDoorType = EM_OPEN_DOOR_TYPE.EM_OPEN_DOOR_TYPE_REMOTE;
		param.write();

		if (isOpen == true) {
			emType = CtrlType.CTRLTYPE_CTRL_ACCESS_OPEN;
		} else {
			emType = CtrlType.CTRLTYPE_CTRL_ACCESS_CLOSE;
		}
		boolean ret = YTDahuaSDK.netsdk.CLIENT_ControlDevice(_loginHandle, emType, param.getPointer(), 1500);
		if (ret == true) {
			_logger.info("Access Control success");
		} else {
			_logger.warn("Access Control failure. Error no: " + YTDahuaSDK.netsdk.CLIENT_GetLastError());
		}
		return ret;
	}

	/**
	 * get open or close status of door
	 * 
	 * @return
	 */
	// TODO
	public JSONObject GetStatus(String nickname) {

		int channel = FindChannelByNickname(nickname);
		if (channel == -1) {
			_logger.warn("Cannot find device with nickname: " + nickname);
			return null;
		}
		
		NET_DOOR_STATUS_INFO info = new NET_DOOR_STATUS_INFO();
		info.nChannel = channel - 1;
//		pBuf.write();

		JSONObject retJson = null;
		IntByReference pRetLen = new IntByReference(0);
		
		Pointer pBuf =  new Memory(info.size());
		ToolKits.SetStructDataToPointer(info, pBuf, 0);

		boolean ret = YTDahuaSDK.netsdk.CLIENT_QueryDevState(_loginHandle, NetSDKLib.NET_DEVSTATE_DOOR_STATE,
				pBuf, info.size(), pRetLen, 3000);
		if (ret == true) {
			
			ToolKits.GetPointerDataToStruct(pBuf, 0, info);
			retJson = new JSONObject();
			retJson.put("ip", _ip);
			retJson.put("nickname", nickname);
			retJson.put("channel", channel);
			retJson.put("door_status", info.emStateType);
			_logger.debug("IP: " + _ip + " nickname:" + nickname + " channel:" + channel + " status:" + info.emStateType
					+ " ret_length:" + pRetLen.getValue());
		} else {
			_logger.warn("Cannot get device status with ip: " + _ip + " channel: " + channel);
		}

		return retJson;
	}
	
	public JSONArray GetAllStatus() {
		JSONArray retArray = new JSONArray();
		for (int i = 0; i < _doorList.size(); i++) {
			JSONObject retJson = null;
			int channel = _doorList.getJSONObject(i).getIntValue("channel");
			String nickname = _doorList.getJSONObject(i).getString("nickname");
			
			NET_DOOR_STATUS_INFO info = new NET_DOOR_STATUS_INFO();
			info.nChannel = _channelList[i] - 1;
			IntByReference pRetLen = new IntByReference(0);
			Pointer pBuf =  new Memory(info.size());
			ToolKits.SetStructDataToPointer(info, pBuf, 0);
			
			boolean ret = YTDahuaSDK.netsdk.CLIENT_QueryDevState(_loginHandle, NetSDKLib.NET_DEVSTATE_DOOR_STATE,
					pBuf, info.size(), pRetLen, 3000);
			if (ret == true) {
				ToolKits.GetPointerDataToStruct(pBuf, 0, info);
				retJson = new JSONObject();
				retJson.put("ip", _ip);
				retJson.put("nickname", nickname);
				retJson.put("channel", channel);
				retJson.put("door_status", info.emStateType);
				_logger.debug("IP: " + _ip + " nickname:" + nickname + " channel:" + channel + " status:" + info.emStateType
						+ " ret_length:" + pRetLen.getValue());
			} else {
				_logger.warn("Cannot get device status with ip: " + _ip + " channel: " + channel);
			}
			retArray.add(retJson);
		}

		return retArray;
	}

	/**
	 * Start device alarm listen 
	 * @param message_cb callback of alarm message pop
	 * @return
	 */
	public boolean StartAlarmListen (fMessCallBack message_cb) {
		_logger.info("Start alarm listening.  IP: " + this._ip);
		if (_is_listening) {
			// already listening
			_logger.warn("Already listening.  IP: " + this._ip);
			return true;
		}
		
		YTDahuaSDK.netsdk.CLIENT_SetDVRMessCallBack(message_cb, null); // set alarm listen callback

		if (!YTDahuaSDK.netsdk.CLIENT_StartListenEx(LoginModule.m_hLoginHandle)) {
			_logger.warn("CLIENT_StartListenEx Failed!" + ToolKits.getErrorCodePrint());
			return false;
		} else { 
			_logger.info("CLIENT_StartListenEx success."); 
		}
		
		_is_listening = true;
		return true;
	}
	
	public boolean StopAlarmListen () {
		if (!_is_listening) {
			return true;
		}
		
	   	if (!LoginModule.netsdk.CLIENT_StopListen(LoginModule.m_hLoginHandle)) { 
	   		_logger.warn("CLIENT_StopListen Failed!" + ToolKits.getErrorCodePrint());
			return false;
		} else { 
			_logger.info("CLIENT_StopListen success."); 
		}
	   	
	   	_is_listening = false;	
		return true;
	}
	
	// TODO
	public JSONObject GetConfig(String nickname) {
		int channel = FindChannelByNickname(nickname);
		if (channel == -1) {
			_logger.warn("Cannot find device with nickname: " + nickname);
			return null;
		}

		JSONObject retJson = null;
		CFG_ACCESS_EVENT_INFO pBuf = new CFG_ACCESS_EVENT_INFO();
		boolean ret = ToolKits.GetDevConfig(_loginHandle, channel - 1, NetSDKLib.CFG_CMD_ACCESS_EVENT, pBuf);
		if (ret == true) {
			System.out.println(pBuf.nUnlockHoldInterval);
			retJson = new JSONObject();
			retJson.put("ip", _ip);
			retJson.put("nickname", nickname);
			retJson.put("channel", channel);
			retJson.put("door_status", 2);
		} else {
			_logger.warn("Get config failure. error no: ");
		}
		return retJson;
	}

	/**
	 * get system time
	 * 
	 * @return timestamp
	 */
	// TODO
	public long GetSystemTime() {
		NET_TIME pDeviceTime = new NET_TIME();

		boolean ret = YTDahuaSDK.netsdk.CLIENT_QueryDeviceTime(_loginHandle, pDeviceTime, 3000);
		if (ret == false) {
			System.out.println("query device time failure");
			return 0;
		}

		return 0;
	}

	public boolean Reboot() {
		NET_CTRL_ACCESS_OPEN param = new NET_CTRL_ACCESS_OPEN();
		param.nChannelID = 0;
		param.szTargetID = null;
		param.szUserID = new byte[32];
		param.emOpenDoorType = EM_OPEN_DOOR_TYPE.EM_OPEN_DOOR_TYPE_REMOTE;
		boolean ret = YTDahuaSDK.netsdk.CLIENT_ControlDevice(_loginHandle, CtrlType.CTRLTYPE_CTRL_REBOOT,
				param.getPointer(), 3000);
		if (ret == true) {
			_logger.info("Access reboot success");
		} else {
			_logger.warn("Access reboot failure. Error no: " + YTDahuaSDK.netsdk.CLIENT_GetLastError());
		}
		return ret;
	}

	// TODO add exception process
	// TODO add log
	@Override
	public void run() {
		_logger.info("AccessControl client is running.");
		if (Connect() != true) {
			_logger.info("AccessControl client stop by connect failure.");
			stop = true;
			return;
		}
		stop = false;
		try {
			// Check service stop command every second
			while (true) {

				if (stop == true) {
					Disconnect();
					break;
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			Disconnect();
			stop = true;
		}
	}

}