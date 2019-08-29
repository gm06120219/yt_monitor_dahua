package yt.java.com.dahua;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import main.java.com.netsdk.lib.NetSDKLib.LLong;

public class YTDahuaFace extends YTDahuaConnecter {
	private static Logger _logger = LogManager.getLogger();
	private String _ip;
	private int _port;
	private String _username;
	private String _password;
	private JSONArray _faceList;
	private YTDahuaClient _client;
	private DefaultSubscribeCallback _subscribeCallback;
	private LLong _loginHandle;
	
	public YTDahuaFace(JSONObject info) {
		_logger.info("Init");
		
		_ip = info.getString("ip");
		_port = info.getInteger("port");
		_username = info.getString("username");
		_password = info.getString("password");
		_faceList = info.getJSONArray("face_list");

		// TODO check parameters
		
		_client = new YTDahuaClient();
		_subscribeCallback = new DefaultSubscribeCallback();
		
	}
	
	@Override
	public void run() {
		_logger.info("Face client is running.");
		if (Connect() != true) {
			_logger.info("Face client stop by connect failure.");
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
	
	private boolean Connect() {
		boolean ret = false;
		_loginHandle = this._client.Login(this._ip, this._port, this._username, this._password);
		if (_loginHandle.longValue() == 0) {
			_logger.warn("Login failure. By IP: " + _ip);
			ret = false;
		} else {
			_logger.info("Login success. By IP: " + _ip);
			ret = true;
		}
		return ret;
	}
	
	private void Disconnect() {
		_client.Logout();
		_client.Clean();
		_logger.info("Disconnect success. By IP: " + _ip);
	}

	// TODO
	public JSONObject GetStatus(String nickname) {
		return null;
	}
	
	// TODO
	public JSONArray GetAllStatus() {
		return null;
	}
	
	// TODO
	public JSONArray GetFaceLibraries() {
		return null;
	}
	
	// TODO
	public boolean AddFaceLibrary(String libraryName) {
		return true;
	}
	
	// TODO
	public boolean DeleteFaceLibrary(String libraryName) {
		return true;
	}
	
	// TODO
	public boolean ModifyFaceLibrary(String libraryName, String newLibraryName) {
		return true;
	}
	
	// TODO
	public boolean EnableSupervise(String libraryName, int percent) {
		return true;
	}
	
	// TODO
	public boolean DisableSupervise(String libraryName) {
		return true;
	}
	
	// 
}
