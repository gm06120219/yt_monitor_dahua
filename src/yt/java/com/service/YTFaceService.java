package yt.java.com.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.controller.YTMainController;
import yt.java.com.tools.JSONTools;
import yt.java.com.view.YTServiceView;

public class YTFaceService extends YTService {
	private static Logger _logger = LogManager.getLogger();
	private JSONObject _config;
	private static final String PROPERTIES_PATH = "./res/face.json";

	private YTServiceView _view;

	public YTFaceService(YTServiceView view) {
		// view
		_view = view;

		// init
		Init();
	}

	@Override
	public void Init() {
		_logger.info("Init");

		// start service
		_monitor = new Runnable() {

			@Override
			public void run() {
				_logger.info("Start client connect.");
				
				// load config
				try {
					_config = YTFaceService.LoadConfig();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(_config == null) {
					// TODO stop service, and show alert dialog
					return;
				}
				
				// start connect
				
			}

		};
	}

	public static JSONObject LoadConfig() throws IOException {
		_logger.info("Load config");
		JSONObject json = null;
		try {
			json = JSONTools.ReadJsonFile(PROPERTIES_PATH);
			_logger.info("Service config: " + json.toJSONString());
		} catch (IOException e) {
			_logger.warn("Service config: null.");
			throw e;
		}
		return json;
	}
	
	public static void SaveConfig(JSONObject content) throws IOException {
		_logger.info("Save config");
		try {
			JSONTools.WriteJsonFile(PROPERTIES_PATH, content);
			_logger.info("Save service config success. Content: " + content.toJSONString());
		} catch(IOException e) {
			_logger.warn("Save service config execption: " + e.toString());
		}
	}

}
