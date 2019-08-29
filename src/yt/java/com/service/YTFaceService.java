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
				_config = YTFaceService.LoadConfig();
				if(_config == null) {
					// TODO stop service, and show alert dialog
					return;
				}
				
				// start connect
				
			}

		};
	}

	public static JSONObject LoadConfig() {
		_logger.info("Load config");
		JSONObject json = null;
		try {
			File jsonFile = new File(PROPERTIES_PATH);
			FileReader fileReader = new FileReader(jsonFile);

			Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
			int ch = 0;
			StringBuffer sb = new StringBuffer();
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
			fileReader.close();
			reader.close();
			json = JSONObject.parseObject(sb.toString());
			_logger.info("Service config: " + json.toJSONString());
		} catch (IOException e) {
			_logger.warn("Service config: null.");
		}
		return json;
	}

}
