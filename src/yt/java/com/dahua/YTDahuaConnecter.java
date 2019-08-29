package yt.java.com.dahua;

/**
 * Single device model
 * @author acer2
 *
 */
public class YTDahuaConnecter extends Thread {
	public boolean stop = true;

	public void ConnectStart() {
		stop = false;
		start();
	}
	
	/**
	 * Stop service, you have to check the variable named "stop".
	 * If the value of "stop" is true, stop the service.
	 */
	public void ConnectStop() {
		stop = true;
	}
	
	@Override
	public void run() {
		super.run();
	}
	
}
