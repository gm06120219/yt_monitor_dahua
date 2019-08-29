package yt.java.com.dahua;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.LLong;
import main.java.com.netsdk.lib.NetSDKLib.NET_IN_ATTACH_VIDEOSTAT_SUM;
import main.java.com.netsdk.lib.NetSDKLib.NET_IN_DOFINDNUMBERSTAT;
import main.java.com.netsdk.lib.NetSDKLib.NET_IN_FINDNUMBERSTAT;
import main.java.com.netsdk.lib.NetSDKLib.NET_NUMBERSTAT;
import main.java.com.netsdk.lib.NetSDKLib.NET_OUT_ATTACH_VIDEOSTAT_SUM;
import main.java.com.netsdk.lib.NetSDKLib.NET_OUT_DOFINDNUMBERSTAT;
import main.java.com.netsdk.lib.NetSDKLib.NET_OUT_FINDNUMBERSTAT;
import main.java.com.netsdk.lib.NetSDKLib.NET_TIME;
import main.java.com.netsdk.lib.NetSDKLib.NET_VIDEOSTAT_SUMMARY;
import main.java.com.netsdk.lib.NetSDKLib.fVideoStatSumCallBack;
import yt.java.com.tools.StructureTools;

public class YTDahuaCustomer extends YTDahuaConnecter {
	private LLong _loginHandle;
	private int _channel = 0; // default channel is 0 / 每台客流统计监控默认都是0通道
	private YTDahuaClient _client;
	private int queryCount; // count with start query response
	private String _ip;
	private int _port;
	private String _username;
	private String _password;
	private StdCallCallback _subscribeCallback = null;
	private LLong _subscribeHandle;

	public YTDahuaCustomer(String ip, int port, String username, String password) {
		System.out.println("new customer");
		_ip = ip;
		_port = port;
		_username = username;
		_password = password;
		_client = new YTDahuaClient();
		_subscribeCallback = new DefaultSubscribeCallback();
	}
	
	public String GetIp() {
		return _ip;
	}
	
	public int GetChannel() {
		return _channel;
	}
	
	public boolean GetConnectStatus() {
		return _client.isConnect;
	}

	/**
	 * Connect
	 * 
	 * @param Callback to process subscribe event, implements interface
	 *                 fVideoStatSumCallBack / 订阅事件触发后的回调，实现接口fVideoStatSumCallBack
	 * @return
	 */
	// TODO registering disconnect callback function
	// TODO add connect log
	// TODO process login exception
	private void Connect() {
		_loginHandle = this._client.Login(this._ip, this._port, this._username, this._password);
	}

	/**
	 * Disconnect
	 * 
	 * @return
	 */
	// TODO add disconnect log
	private void Disconnect() {
		UnsubscribeEvent();
		_client.Logout();
		_client.Clean();
	}

	/**
	 * 
	 * @param processCallback Callback to process event, implements interface fVideoStatSumCallBack
	 *                        / 客流事件触发后的回调，实现接口fVideoStatSumCallBack
	 */
	public void RegisterSubscribeCallback(StdCallCallback processCallback) {
		if (_subscribeCallback != null) {
			_subscribeCallback = processCallback;			
		}
	}

	/**
	 * Subscribe customers' enter and exit event / 订阅客流事件
	 * 
	 * @param processCallback Callback to process event, implements interface fVideoStatSumCallBack
	 *                        / 客流事件触发后的回调，实现接口fVideoStatSumCallBack
	 * @return listen handle, use to stop / 订阅句柄，用于停用订阅
	 */
	// TODO subscribe exception
	private LLong SubscribeEvent(StdCallCallback processCallback) {
		NET_IN_ATTACH_VIDEOSTAT_SUM pInParam = new NET_IN_ATTACH_VIDEOSTAT_SUM();
		pInParam.nChannel = _channel;
		pInParam.cbVideoStatSum = processCallback;
		pInParam.dwUser = new Pointer(0);

		NET_OUT_ATTACH_VIDEOSTAT_SUM pOutParam = new NET_OUT_ATTACH_VIDEOSTAT_SUM();

		int waitTime = 5000;

		_subscribeHandle = YTDahuaSDK.netsdk.CLIENT_AttachVideoStatSummary(_loginHandle, pInParam, pOutParam, waitTime);

		return _subscribeHandle;
	}

	/**
	 * Unsubscribe customers' enter and exit event / 取消订阅客流事件
	 * 
	 * @param listenHandle The subscribe handle / 订阅句柄
	 * @return result of unsubscribe / 取消订阅结果
	 */
	private boolean UnsubscribeEvent() {
		boolean retBoolean;
		if (_subscribeHandle.longValue() != 0) {
			retBoolean = YTDahuaSDK.netsdk.CLIENT_DetachVideoStatSummary(_subscribeHandle);
			_subscribeHandle.setValue(0L);
		} else {
			retBoolean = false;
		}
		return retBoolean;
	}

	/**
	 * Get the number of customers per day / 按天查询客流量
	 * 
	 * @param startTimestamp start time stamp / 开始时间戳（毫秒）
	 * @param endTimestamp   end time stamp / 结束时间戳（毫秒）
	 * @return JSON: { "channel" : channelNo, "enter" : enterNo, "exit": exitNo,
	 *         "start_time" : startTimestamp, "end_time": endTimestamp } / JSON数组
	 * @throws ParseException parse time exception / 解析时间异常
	 */
	public JSONArray QueryCustomersNumberPerDay(Long startTimestamp, Long endTimestamp) throws ParseException {

		// get the data from camera
		LLong queryHandle = StartQueryCustomersNumber(this._loginHandle, _channel, startTimestamp, endTimestamp);
		NET_NUMBERSTAT[] retNetList = QueryCustomersNumber(queryHandle);

		for (int i = 0; i < retNetList.length; i++) {
			System.out.printf("Time: %s-%s >>> Channel %d, Rule: %s, Enter: %d, Exit: %d. \r\n",
					retNetList[i].stuStartTime.toStringDay(), retNetList[i].stuEndTime.toStringDay(),
					retNetList[i].nChannelID, new String(retNetList[i].szRuleName), retNetList[i].nEnteredSubTotal,
					retNetList[i].nExitedSubtotal);
		}

		StopQueryCustomersNumber(queryHandle);

		// parse query results into JSON
		JSONArray retJsonList = new JSONArray();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < retNetList.length; i++) {
			JSONObject temp = new JSONObject();
			temp.put("channel", retNetList[i].nChannelID);
			temp.put("enter", retNetList[i].nEnteredSubTotal);
			temp.put("exit", retNetList[i].nExitedSubtotal);
			calendar.setTime(format.parse(retNetList[i].stuStartTime.toString()));
			temp.put("start_time", calendar.getTimeInMillis());
			calendar.setTime(format.parse(retNetList[i].stuEndTime.toString()));
			temp.put("end_time", calendar.getTimeInMillis());
			retJsonList.add(temp);
		}

		return retJsonList;
	}

	/**
	 * Enable query number of customers / 启用客流查询
	 * 
	 * @param loginHandle    The handle of login interface return / 登入句柄
	 * @param channel        The person number camera channel / 客流监控通道
	 * @param startTimestamp start time stamp / 开始时间戳（毫秒）
	 * @param endTimestamp   end time stamp / 结束时间戳（毫秒）
	 * @return query handle, for QueryPersonNumber() / 客流查询句柄
	 */
	private LLong StartQueryCustomersNumber(LLong loginHandle, int channel, Long startTimestamp, Long endTimestamp) {
		// time format
		Calendar start = Calendar.getInstance();
		start.setTime(new Date(startTimestamp));
		NET_TIME startNetTime = new NET_TIME();
		startNetTime.setTime(start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH),
				start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), start.get(Calendar.SECOND));
		Calendar end = Calendar.getInstance();
		end.setTime(new Date(endTimestamp));
		NET_TIME endNetTime = new NET_TIME();
		endNetTime.setTime(end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1, end.get(Calendar.DAY_OF_MONTH),
				end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE), end.get(Calendar.SECOND));

		// input parameter
		NET_IN_FINDNUMBERSTAT pstInParam = new NET_IN_FINDNUMBERSTAT();
		pstInParam.nWaittime = 3000;
		pstInParam.nChannelID = channel;
		pstInParam.stStartTime = startNetTime;
		pstInParam.stEndTime = endNetTime;
		pstInParam.nGranularityType = 2; // 查询粒度 0:分钟,1:小时,2:日,3:周,4:月,5:季,6:年
		pstInParam.nPlanID = 1;
		pstInParam.emRuleType = NetSDKLib.EM_RULE_TYPE.EM_RULE_NUMBER_STAT;
		pstInParam.nMinStayTime = 0;

		NET_OUT_FINDNUMBERSTAT pstOutParam = new NET_OUT_FINDNUMBERSTAT();
		LLong queryHandle = YTDahuaSDK.netsdk.CLIENT_StartFindNumberStat(loginHandle, pstInParam, pstOutParam);
		if (queryHandle.longValue() == 0) {
			// TODO throw data exception
			System.err.println("Failed to start query customers number. ErrNo: " + ToolKits.getErrorCodePrint());
		} else {
			this.queryCount = pstOutParam.dwTotalCount;
			System.out.println("Success to start query customers number. Count: " + pstOutParam.dwTotalCount);
		}

		return queryHandle;
	}

	/**
	 * Query number of customers per day / 按天查询客流
	 * 
	 * @param queryHandle query handle, return by StartQueryCustomersNumber /
	 *                    启用客流查询返回的句柄
	 * @return the number list of customers per day / 每天的客流数据
	 */
	private NET_NUMBERSTAT[] QueryCustomersNumber(LLong queryHandle) {
		NET_IN_DOFINDNUMBERSTAT pstInParam = new NET_IN_DOFINDNUMBERSTAT();
		pstInParam.nBeginNumber = 0;
		pstInParam.nCount = this.queryCount;
		pstInParam.nWaittime = 5000;

		NET_OUT_DOFINDNUMBERSTAT pstOutParam = new NET_OUT_DOFINDNUMBERSTAT();
		int dwsize = new NET_NUMBERSTAT().dwSize;
		int size = pstInParam.nCount * dwsize;
		pstOutParam.pstuNumberStat = new Memory(size);
		pstOutParam.nBufferLen = size;
		for (int i = 0; i < pstInParam.nCount; i++) {
			pstOutParam.pstuNumberStat.setInt(i * dwsize, dwsize);
		}

		int iRet = YTDahuaSDK.netsdk.CLIENT_DoFindNumberStat(queryHandle, pstInParam, pstOutParam);
		if (iRet == 0) {
			System.out.println("Fail to query customers number. ErrNo:" + ToolKits.getErrorCodePrint());
		} else {
			System.out.println("Success to query customers number. Count: " + pstOutParam.nCount);
		}

		NET_NUMBERSTAT[] retNumberstats = new NET_NUMBERSTAT[pstOutParam.nCount];
		try {
			retNumberstats = StructureTools.Pointer2StructureList(pstOutParam.pstuNumberStat, NET_NUMBERSTAT.class,
					pstOutParam.nCount);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retNumberstats;
	}

	/**
	 * Stop query number of customers / 停用客流查询
	 * 
	 * @param queryHandle query handle, return by StartQueryCustomersNumber / 客流查询句柄
	 * @return result of stop / 是否停用成功
	 */
	private boolean StopQueryCustomersNumber(LLong queryHandle) {
		return YTDahuaSDK.netsdk.CLIENT_StopFindNumberStat(queryHandle);
	}

	// TODO add exception process 
	// TODO add log
	@Override
	public void run() {
		System.out.println("Service customers running.");
		Connect();
		stop = false;
		try {
			// TEST Code for get 6.27 to 7.4 data
			/**/
			Calendar cTemp = Calendar.getInstance();
			cTemp.set(2019, 5, 27, 0, 0, 0);
			Long startTime = cTemp.getTimeInMillis();
			cTemp.set(2019, 6, 4, 0, 0, 0);
			Long endTime = cTemp.getTimeInMillis();
			QueryCustomersNumberPerDay(startTime, endTime);
			

			// listen event
			 SubscribeEvent(_subscribeCallback);

			// Check service stop command every second
			while (true) {
				if (stop == true) {
					Disconnect();
					break;
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			Disconnect();
			stop = true;
		} catch (ParseException e) {
			e.printStackTrace();
			Disconnect();
			stop = true;
		}
	}

}

