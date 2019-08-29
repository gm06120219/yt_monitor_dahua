package yt.java.com.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.controller.YTMainController;

public class YTMain {
	private static Logger _logger = LogManager.getLogger();
	public static void main(String[] args) {
		_logger.info("YTMonitor >>> START!!!");
		new YTMainController();
	}
}
