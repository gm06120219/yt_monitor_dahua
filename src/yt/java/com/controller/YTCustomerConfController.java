package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.service.YTCustomerService;
import yt.java.com.service.YTService.SERVICE_STATUS;
import yt.java.com.view.YTCustomerConfView;

public class YTCustomerConfController extends YTController {

	private static Logger _logger = LogManager.getLogger();
	private YTCustomerService _service;

	public YTCustomerConfController(YTCustomerService customerService) {
		// view 
		YTCustomerConfView frame = new YTCustomerConfView();
		
		// model
		_service = customerService;
		
		// bind event
		frame.OnSave(new ActionListener() {
			
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
