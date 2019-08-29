package yt.java.com.dahua;

import com.sun.jna.Pointer;

import main.java.com.netsdk.lib.NetSDKLib.LLong;
import main.java.com.netsdk.lib.NetSDKLib.NET_VIDEOSTAT_SUMMARY;
import main.java.com.netsdk.lib.NetSDKLib.fVideoStatSumCallBack;

// TODO 
public class DefaultSubscribeCallback implements fVideoStatSumCallBack {
	@Override
	public void invoke(LLong lAttachHandle, NET_VIDEOSTAT_SUMMARY pBuf, int dwBufLen, Pointer dwUser) {
		System.out.printf("Time: %s >>> Enter: %d, Exit: %d. \r\n", pBuf.stuTime.toString(),
				pBuf.stuEnteredSubtotal.nTotal, pBuf.stuExitedSubtotal.nTotal);
	}
}