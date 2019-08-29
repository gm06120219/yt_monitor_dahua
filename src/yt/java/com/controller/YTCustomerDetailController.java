package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import yt.java.com.service.YTCustomerService;
import yt.java.com.service.YTService.SERVICE_STATUS;
import yt.java.com.view.YTCustomerDetailView;

public class YTCustomerDetailController extends YTController {
	
	private static Logger _logger = LogManager.getLogger();
	private YTCustomerService _service;
	private YTCustomerDetailView _view;

	public YTCustomerDetailController(YTCustomerService customerService) {
		// view
		_view = new YTCustomerDetailView();
		
		// model
		_service = customerService;
		
		// bind event
		_view.AddQueryAction(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_service.serviceStatus != SERVICE_STATUS.START && _service.serviceStatus != SERVICE_STATUS.WARN) {
					return;
				}
				
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Long startTimestamp = null;
				Long endTimestamp = null;
				
				try {
					calendar.setTime(format.parse(_view.GetStarttime()));
					startTimestamp = calendar.getTimeInMillis();
					calendar.setTime(format.parse(_view.GetEndtime()));
					endTimestamp = calendar.getTimeInMillis();
					JSONArray ret = _service.GetDeviceStatus(startTimestamp, endTimestamp);
					String retString = "";
					for (int i = 0; i < ret.size(); i++) {
						JSONObject temp = ret.getJSONObject(i);
						retString += "IP: " + temp.getString("ip") + " ";
						retString += "Channel: " + temp.getInteger("channel") + " ";
						retString += "Status: " + temp.getBoolean("connect") + " ";
						retString += "Enter: " + temp.getInteger("enter") + " ";
						retString += "Exit: " + temp.getInteger("exit") + "\n";
					}
					System.out.println(retString);
					_view.SetDeviceStatus(retString);
				} catch (ParseException e3) {
					_view.alert("开始结束时间不对，请重新填入，如：20190501");
				}
			}
		});
	}
}
