package yt.java.com.controller;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.view.YTMainView;

public class YTMainController extends YTController {
	private static Logger logger = LogManager.getLogger();
	public static YTCustomerController customerController;
	public static YTAccessController accessController;
	public static YTTelecomController telecomController;
	public static YTFaceController faceController;
	private YTMainView view;
	
	public YTMainController() {
		// View
		view = new YTMainView();
		
		// Model: main view no model
		
		// Others
		// module controller
		customerController = new YTCustomerController(view);
		telecomController = new YTTelecomController(view);
		accessController = new YTAccessController(view);
		faceController = new YTFaceController(view);
	}
}

