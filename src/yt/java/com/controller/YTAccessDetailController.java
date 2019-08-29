package yt.java.com.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import yt.java.com.dahua.YTDahuaAccess;
import yt.java.com.service.YTAccessService;
import yt.java.com.view.YTAccessDetailView;

public class YTAccessDetailController extends YTController {
	private static Logger _logger = LogManager.getLogger();
	private YTAccessDetailView _view;
	private YTAccessService _service;

	public YTAccessDetailController(YTAccessService service) {
		// view
		_view = new YTAccessDetailView();
		
		// model
		_service = service;
		
		// bind event process
		_view.SetOpenClickListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String nickname = _view.GetNickname();
				if(nickname.length() == 0) {
					_view.AddText("请选择正确的门禁.");
					return;
				}
				YTDahuaAccess client = _service.GetClientByNickname(nickname);
				int channel = -1;
				boolean ret = false;
				
				if(client != null) {
					channel = client.FindChannelByNickname(nickname);
					ret = client.SetStatus(true, channel);
					if(ret == false) {
						_view.AddText("门禁控制失败.");
					} else {
						_view.AddText("门禁控制成功.");
					}
				} else {
					_logger.warn("Not found the AccessControl. By nickname: " + nickname);
					_view.AddText("没有找到这个门禁.");
				}
			}
		});
		
		_view.SetCloseClickListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String nickname = _view.GetNickname();
				if(nickname.length() == 0) {
					_view.AddText("请选择正确的门禁.");
					return;
				}
				YTDahuaAccess client = _service.GetClientByNickname(nickname);
				int channel = -1;
				boolean ret = false;
				
				if(client != null) {
					channel = client.FindChannelByNickname(nickname);
					ret = client.SetStatus(false, channel);
					if(ret == false) {
						_view.AddText("门禁控制失败.");
					} else {
						_view.AddText("门禁控制成功.");
					}
				} else {
					_logger.warn("Not found the AccessControl. By nickname: " + nickname);
					_view.AddText("没有找到这个门禁.");
				}
			}
		});
	
		_view.SetRecordClickListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String nickname = _view.GetNickname();
				if(nickname.length() == 0) {
					_view.AddText("请选择正确的门禁.");
					return;
				}
				YTDahuaAccess client = _service.GetClientByNickname(nickname);
				int channel = -1;
				JSONObject ret = null;
				
				if(client != null) {
					ret = client.GetStatus(nickname);
//					ret = client.GetConfig(nickname);
					if(ret == null) {
						_view.AddText("门禁状态获取失败.");
					} else {
						String status = "";
						switch (ret.getInteger("door_status"))
						{
						case 1:
							status = "打开";
							break;
						case 2:
							status = "关闭";
							break;
						case 3:
							status = "异常";
							break;
						default:
							status = "未知";
							break;
						}
						_view.AddText("门禁状态>>> IP: " + ret.getString("ip") + " 名称:" + ret.getString("nickname") + " 通道号:" + ret.getInteger("channel") + " 状态:" + status);
					}
				} else {
					_logger.warn("Not found the AccessControl. By nickname: " + nickname);
					_view.AddText("没有找到这个门禁.");
				}
			}
			
		});
	}

}
