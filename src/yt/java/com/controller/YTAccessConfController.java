package yt.java.com.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.service.YTAccessService;
import yt.java.com.view.YTAccessConfView;

public class YTAccessConfController extends YTController {
	
	private static Logger _logger = LogManager.getLogger();
	private YTAccessService _service;
	private YTAccessConfView _view;

	public YTAccessConfController(YTAccessService service) {
		// view 
		_view = new YTAccessConfView();
		
		// model
		_service = service;
		
		// bind event
	}
}
