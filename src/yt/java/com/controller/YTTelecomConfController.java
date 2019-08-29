package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yt.java.com.service.YTCustomerService;
import yt.java.com.service.YTService.SERVICE_STATUS;
import yt.java.com.service.YTTelecomService;
import yt.java.com.view.YTCustomerConfView;
import yt.java.com.view.YTTelecomConfView;

public class YTTelecomConfController extends YTController {

	private static Logger _logger = LogManager.getLogger();
	private YTTelecomService _service;

	public YTTelecomConfController(YTTelecomService telecomService) {
		// View
		YTTelecomConfView frame = new YTTelecomConfView();
		frame.setVisible(true);
		_logger.info("Show telecom config view.");

		// Model
		_service = telecomService;

		// Others
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
