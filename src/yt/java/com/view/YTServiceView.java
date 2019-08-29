package yt.java.com.view;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import yt.java.com.service.YTService;

public class YTServiceView {
	private JPanel _statusJPanel;
	private JLabel _pidJLabel;
	private JLabel _moduleJLabel;
	private JButton _detailJButton;
	private ActionListener _detailListener = null;
	private JButton _actionJButton;
	private ActionListener _actionListener = null;
	private JButton _configJButton;
	private ActionListener _configListener = null;
	private JButton _logJButton;
	private ActionListener _logListener = null;
	
	public YTServiceView(JPanel statusPanel,
			JLabel pidLab,
			JLabel moduleLab,
			JButton detailBtn,
			JButton actionBtn,
			JButton configBtn,
			JButton logBtn) {
		this._statusJPanel = statusPanel;
		this._pidJLabel = pidLab;
		this._moduleJLabel = moduleLab;
		this._detailJButton = detailBtn;
		this._actionJButton = actionBtn;
		this._configJButton = configBtn;
		this._logJButton = logBtn;
	}
	
	/**
	 * Set status view
	 * @param status 0:stop,1:running,2:warning,-1:broken stop
	 */
	public void SetStatus(int status) {
		switch (status) {
		case YTService.SERVICE_STATUS.BROKEN:
			_actionJButton.setText("Start");
			_statusJPanel.setBackground(Color.RED);
			break;
		case YTService.SERVICE_STATUS.STOP:
			_actionJButton.setText("Start");
			_statusJPanel.setBackground(Color.LIGHT_GRAY);
			break;
		case YTService.SERVICE_STATUS.START:
			_actionJButton.setText("Stop");
			_statusJPanel.setBackground(Color.GREEN);
			break;
		case YTService.SERVICE_STATUS.WARN:
			_actionJButton.setText("Stop");
			_statusJPanel.setBackground(Color.ORANGE);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
	}
	
	public void SetDetailOnClick(ActionListener cb) {
		this._detailListener = cb;
		this._detailJButton.addActionListener(this._detailListener);
	}
	
	public void SetStartOnClick(ActionListener cb) {
		this._actionListener = cb;
		this._actionJButton.addActionListener(this._actionListener);
	}
	
	public void SetConfigOnClick(ActionListener cb) {

		this._configListener = cb;
		this._configJButton.addActionListener(this._configListener);
	}
	
	public void SetLogOnClick(ActionListener cb) {

		this._logListener = cb;
		this._logJButton.addActionListener(this._logListener);
	}
	
	public void SetCount(String id) {
		this._pidJLabel.setText(id);
	}
}
