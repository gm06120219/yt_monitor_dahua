package yt.java.com.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YTAccessConfView extends JFrame {

	private static Logger _logger = LogManager.getLogger();
	private JPanel _contentPane;
	private JTextField _countText;
	private int _accessCount;
	private String[] _ipList;
	private int[] _portList;
	private String[] _usernameList;
	private String[] _passwordList;
	private String _usernameListString;
	private String _passwordListString;
	private String _ipListString;
	private String _portListString;
	private ActionListener _saveExtraActionListener = null;
	private String _nicknameListString;
	private String _channelListString;
	private String[] _nicknameList;
	private String[] _channelList;
	private JButton _saveBtn;


	public YTAccessConfView() {
		// load config
		_logger.info("Load access config.");
		LoadConfig();
		
		// init view
		_logger.info("Show access config view.");
		InitView();
		
		// bind event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("Close access config view.");
			}
		});
	}

	private void InitView() {
		Border border = BorderFactory.createLineBorder(new Color(138, 138, 138));

		setTitle("门禁配置");
		setBounds(100, 100, 700, 400);
		_contentPane = new JPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		_contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("门禁个数");
		lblNewLabel.setBounds(30, 30, 150, 15);
		_contentPane.add(lblNewLabel);

		_countText = new JTextField();
		_countText.setBounds(200, 27, 100, 22);
		_countText.setText(String.valueOf(_accessCount));
		_contentPane.add(_countText);
		_countText.setColumns(10);

		JLabel lblNickname = new JLabel("昵称（多个以\",\"分开）");
		lblNickname.setBounds(30, 70, 150, 15);
		_contentPane.add(lblNickname);

		JTextArea nicknameText = new JTextArea();
		nicknameText.setLineWrap(true);
		nicknameText.setBorder(border);
		nicknameText.setBounds(200, 65, 450, 32);
		nicknameText.setText(_nicknameListString);
		_contentPane.add(nicknameText);

		JLabel lblIp = new JLabel("IP（多个以\",\"分开）");
		lblIp.setBounds(30, 110, 150, 15);
		_contentPane.add(lblIp);

		JTextArea ipText = new JTextArea();
		ipText.setLineWrap(true);
		ipText.setBorder(border);
		ipText.setBounds(200, 105, 450, 32);
		ipText.setText(_ipListString);
		_contentPane.add(ipText);

		JLabel lblChannel = new JLabel("通道号（多个以\",\"分开）");
		lblChannel.setBounds(30, 150, 150, 15);
		_contentPane.add(lblChannel);

		JTextArea channelText = new JTextArea();
		channelText.setWrapStyleWord(true);
		channelText.setLineWrap(true);
		channelText.setBorder(border);
		channelText.setBounds(200, 145, 450, 32);
		channelText.setText(_channelListString);
		_contentPane.add(channelText);

		JLabel label_1 = new JLabel("端口号（多个以\",\"分开）");
		label_1.setBounds(30, 190, 150, 15);
		_contentPane.add(label_1);

		JTextArea portText = new JTextArea();
		portText.setWrapStyleWord(true);
		portText.setLineWrap(true);
		portText.setBorder(border);
		portText.setBounds(200, 185, 450, 32);
		portText.setText(_portListString);
		_contentPane.add(portText);

		JLabel label_2 = new JLabel("用户名（多个以\",\"分开）");
		label_2.setBounds(30, 230, 150, 15);
		_contentPane.add(label_2);

		JTextArea usernameText = new JTextArea();
		usernameText.setWrapStyleWord(true);
		usernameText.setLineWrap(true);
		usernameText.setBorder(border);
		usernameText.setBounds(200, 225, 450, 32);
		usernameText.setText(_usernameListString);
		_contentPane.add(usernameText);

		JLabel label_3 = new JLabel("密码（多个以\",\"分开）");
		label_3.setBounds(30, 270, 150, 15);
		_contentPane.add(label_3);

		JTextArea passwordText = new JTextArea();
		passwordText.setLineWrap(true);
		passwordText.setWrapStyleWord(true);
		passwordText.setBorder(border);
		passwordText.setBounds(200, 265, 450, 32);
		passwordText.setText(_passwordListString);
		_contentPane.add(passwordText);

		_saveBtn = new JButton("保存");
		_saveBtn.setBounds(300, 320, 100, 25);
		_saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 1. check value
				_accessCount = Integer.valueOf(_countText.getText());
				_nicknameListString = nicknameText.getText();
				_ipListString = ipText.getText();
				_portListString = portText.getText();
				_channelListString = channelText.getText();
				_usernameListString = usernameText.getText();
				_passwordListString = passwordText.getText();

				if (CheckConfig() != true) {
					// pop alert
					ShowAlertDialog("参数有误，请重新配置");
					return;
				}

				// 2. save value
				SaveConfig();
			}
		});

		_contentPane.add(_saveBtn);

		setVisible(true);
	}
	
	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			properties.load(new InputStreamReader(new FileInputStream("./res/access.properties"), "UTF-8"));
		} catch (FileNotFoundException e) {
			// TODO Refresh ui status
			_logger.error("Not found config properties file.");
			return;
		} catch (IOException e) {
			// TODO Refresh ui status
			_logger.error("Read error with properties file.");
			return;
		}

		// Prase config
		_accessCount = Integer.parseInt(properties.getProperty("COUNT"));
		_nicknameListString = properties.getProperty("NICKNAME_LIST");
		_ipListString = properties.getProperty("IP_LIST");
		_channelListString = properties.getProperty("CHANNEL_LIST");
		_portListString = properties.getProperty("PORT_LIST");
		_usernameListString = properties.getProperty("USERNAME_LIST");
		_passwordListString = properties.getProperty("PASSWORD_LIST");
		
		// TODO process parse exception
	}

	private boolean CheckConfig() {
		if (_accessCount <= 0) {
			return false;
		}

		_nicknameList = _nicknameListString.split(",");
		if (_nicknameList.length != _accessCount) {
			return false;
		}

		_ipList = _ipListString.split(",");
		if (_ipList.length != _accessCount) {
			return false;
		}
		for (int i = 0; i < _ipList.length; i++) {
			if (_ipList[i].split("\\.").length != 4) {
				return false;
			}
		}

		_channelList = _channelListString.split(",");
		if (_channelList.length != _accessCount) {
			return false;
		}
		for (int i = 0; i < _channelList.length; i++) {
			if (Integer.valueOf(_channelList[i]) <= 0 || Integer.valueOf(_channelList[i]) > 4) {
				return false;
			}
		}

		String[] tempPortList = _portListString.split(",");
		if (tempPortList.length != _accessCount) {
			return false;
		}
		for (int i = 0; i < tempPortList.length; i++) {
			if (Integer.parseInt(tempPortList[i]) <= 0 || Integer.parseInt(tempPortList[i]) >= 65535) {
				return false;
			}
		}

		_usernameList = _usernameListString.split(",");
		if (_usernameList.length != _accessCount) {
			return false;
		}

		_passwordList = _passwordListString.split(",");
		if (_passwordList.length != _accessCount) {
			return false;
		}
		return true;
	}

	private void SaveConfig() {
		Properties p = new Properties();
		FileOutputStream oFile = null;
		try {
			oFile = new FileOutputStream("./res/access.properties");
			p.setProperty("COUNT", String.valueOf(_accessCount));
			p.setProperty("NICKNAME_LIST", _nicknameListString);
			p.setProperty("IP_LIST", _ipListString);
			p.setProperty("CHANNEL_LIST", _channelListString);
			p.setProperty("PORT_LIST", _portListString);
			p.setProperty("USERNAME_LIST", _usernameListString);
			p.setProperty("PASSWORD_LIST", _passwordListString);
			p.store(oFile, "");
			oFile.close();
			ShowAlertDialog("保存成功");
			if (_saveExtraActionListener != null) {
				_saveExtraActionListener.actionPerformed(null);
			}
			this.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ShowAlertDialog(String message) {
		JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
	}

	public void OnSave(ActionListener listener) {
		_saveExtraActionListener = listener;
	}
}
