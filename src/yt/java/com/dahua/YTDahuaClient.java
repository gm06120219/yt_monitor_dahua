package yt.java.com.dahua;

import java.io.File;

import javax.swing.SwingUtilities;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import main.java.com.netsdk.lib.NetSDKLib;
import main.java.com.netsdk.lib.ToolKits;
import main.java.com.netsdk.lib.NetSDKLib.LLong;
import main.java.com.netsdk.lib.NetSDKLib.NET_DEVICEINFO_Ex;
import main.java.com.netsdk.lib.NetSDKLib.NET_PARAM;

/**
 * Connect to Dahua device / 链接大华设备
 * 
 * @author acer2
 *
 */
public class YTDahuaClient {
	private static final int WAITTIME = 5000; // 登录请求响应超时时间设置为5S
	private static final int RECTRYTIMES = 1; // 登录时尝试建立链接1次

	public boolean isInit = false;
	public String ip;
	public int port;
	public String username;
	public String password;
	public LLong loginHandle;
	public NET_PARAM deviceParam;
	public NET_DEVICEINFO_Ex deviceInfo;
	public boolean isConnect = false;

	public YTDahuaClient() {
		// 打开日志，可选
		File path = new File("./YTLog/");
		if (!path.exists()) {
			path.mkdir();
		}
		String logPath = path.getAbsoluteFile().getParent() + "\\YTLog\\DahuaSDK\\" + ToolKits.getDate() + ".log";

		NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
		setLog.bSetFilePath = 1;
		setLog.szLogFilePath = logPath.getBytes();
		setLog.bSetFileSize = 0;
		setLog.nFileSize = 5 * 1024 * 1024 * 8;
		// setLog.bSetFileNum = 1;
		// setLog.nFileNum = 1;
		boolean bLogopen = YTDahuaSDK.netsdk.CLIENT_LogOpen(setLog);
		if (!bLogopen) {
			System.err.println("Failed to open NetSDK log");
		}
	}

	/**
	 * Login device
	 * 
	 * @param ip
	 * @param port
	 * @param username
	 * @param password
	 */
	public LLong Login(String ip, int port, String username, String password) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		DisConnect disConnect = new DisConnect(this);
		ReConnect haveReConnect = new ReConnect(this);

		// init
		this.isInit = YTDahuaSDK.netsdk.CLIENT_Init(disConnect, null);
		YTDahuaSDK.netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);
		YTDahuaSDK.netsdk.CLIENT_SetConnectTime(WAITTIME, RECTRYTIMES);
		if (this.deviceParam != null) {
			YTDahuaSDK.netsdk.CLIENT_SetNetworkParam(this.deviceParam);
		}

		// login
		IntByReference errorCode = new IntByReference(0);
		this.deviceInfo = new NET_DEVICEINFO_Ex();
		this.loginHandle = YTDahuaSDK.netsdk.CLIENT_LoginEx2(this.ip, this.port, this.username, this.password, 0, null,
				this.deviceInfo, errorCode);
		if (loginHandle.longValue() != 0) {
			isConnect = true;
		}

		return this.loginHandle;
	}

	/**
	 * Login device with param
	 * 
	 * @param ip
	 * @param port
	 * @param username
	 * @param password
	 * @param deviceParam
	 * @return
	 */
	public LLong Login(String ip, int port, String username, String password, NET_PARAM deviceParam) {
		this.deviceParam = deviceParam;
		return this.Login(ip, port, username, password);
	}

	/**
	 * Logout device
	 * 
	 * @return
	 */
	public boolean Logout() {
		return YTDahuaSDK.netsdk.CLIENT_Logout(this.loginHandle);
	}

	/**
	 * Clean up
	 */
	public void Clean() {
		YTDahuaSDK.netsdk.CLIENT_Cleanup();
	}
}

/**
 * TODO: Disconnect callback
 */
class DisConnect implements NetSDKLib.fDisConnect {
	private YTDahuaClient _connecter;

	public DisConnect(YTDahuaClient connecter) {
		_connecter = connecter;
	}

	public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
		System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
		// 断线提示
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_connecter.isConnect = false;
				System.out.println("device disconnect");
			}
		});
	}
}

/**
 * TODO: Reconnect callback
 */
class ReConnect implements NetSDKLib.fHaveReConnect {
	private YTDahuaClient _connecter;

	public ReConnect(YTDahuaClient connecter) {
		_connecter = connecter;
	}

	public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
		System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

		// 重连提示
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_connecter.isConnect = true;
				String x = "device reconnect";
				System.out.println(x);
			}
		});
	}
}