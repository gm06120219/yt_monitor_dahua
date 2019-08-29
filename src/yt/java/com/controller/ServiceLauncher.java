package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import yt.java.com.dahua.YTDahuaConnecter;
import yt.java.com.service.YTService;
import yt.java.com.view.YTServiceView;

/**
 * Service launcher
 * 
 * @author liguangming
 * @param <T>
 *
 */
public class ServiceLauncher implements ActionListener {
	YTService _service;

	public ServiceLauncher(YTService service)  {
		_service = service;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Start")) {
			_service.Start();
		} else {
			_service.Stop();
		}
	}

}
