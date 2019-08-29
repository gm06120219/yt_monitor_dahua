package yt.java.com.service;

public class YTService {
	public static class SERVICE_STATUS {
		public static final int BROKEN = -1;
		public static final int STOP = 0;
		public static final int START = 1;
		public static final int WARN = 2;
	}

	public int serviceStatus = SERVICE_STATUS.STOP;
	protected Runnable _monitor;
	
	// init service
	public void Init() {
		
	}
	
	
	// start service
	public void Start() {
		serviceStatus = SERVICE_STATUS.START;
		new Thread(_monitor).start();
	}

	// stop service
	public void Stop() {
		serviceStatus = SERVICE_STATUS.STOP;
	}
}
