package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.service.YTFaceService;
import yt.java.com.service.YTService.SERVICE_STATUS;
import yt.java.com.view.YTFaceConfView;

public class YTFaceConfController extends YTController {
	public YTFaceConfController(YTFaceService service) {
		Logger _logger = LogManager.getLogger();
		YTFaceService _service;
		
		// view 
		YTFaceConfView view = new YTFaceConfView();
		
		// model
		_service = service;
		
		// bind event
		view.OnSave(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// restart service
				if (_service.serviceStatus == SERVICE_STATUS.START) {
					_service.Stop();
					try {
						Thread.sleep(500);
						_service.Start();
					} catch (InterruptedException e1) {
						_logger.warn("Thread sleep exception: " + e.toString());
					}
				}
			}
		});
	}
}
