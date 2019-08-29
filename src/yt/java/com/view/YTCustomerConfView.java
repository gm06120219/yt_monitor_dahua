package yt.java.com.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YTCustomerConfView extends JFrame {

	private static Logger _logger = LogManager.getLogger();
	private JPanel contentPane;
	private JTextField countText;
	private int _customersCount;
	private String[] _ipList;
	private int[] _portList;
	private String[] _usernameList;
	private String[] _passwordList;
	private String _usernameListString;
	private String _passwordListString;
	private String _ipListString;
	private String _portListString;
	private ActionListener _saveExtraActionListener = null;

	public YTCustomerConfView() {
		// load config
		_logger.info("Load customer config.");
		LoadConfig();
		
		// init view 
		_logger.info("Show customer config view.");
		InitView();

		// bind event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("Close customer config view.");
			}
		});
	}

	private void InitView() {
		setTitle("客流统计配置");
		setBounds(100, 100, 700, 380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("客流统计监控个数");
		lblNewLabel.setBounds(30, 30, 150, 15);
		contentPane.add(lblNewLabel);

		JLabel lblIp = new JLabel("IP地址（多个以\"，\"分开）");
		lblIp.setBounds(30, 80, 150, 15);
		contentPane.add(lblIp);

		JLabel label_1 = new JLabel("端口号（多个以\"，\"分开）");
		label_1.setBounds(30, 130, 150, 15);
		contentPane.add(label_1);

		JLabel label_2 = new JLabel("用户名（多个以\"，\"分开）");
		label_2.setBounds(30, 180, 150, 15);
		contentPane.add(label_2);

		JLabel label_3 = new JLabel("密码（多个以\"，\"分开）");
		label_3.setBounds(30, 230, 150, 15);
		contentPane.add(label_3);

		JTextArea passwordText = new JTextArea();
		passwordText.setLineWrap(true);
		passwordText.setWrapStyleWord(true);
		passwordText.setBounds(200, 230, 450, 40);
		passwordText.setText(_passwordListString);
		contentPane.add(passwordText);

		JTextArea usernameText = new JTextArea();
		usernameText.setWrapStyleWord(true);
		usernameText.setLineWrap(true);
		usernameText.setBounds(200, 175, 450, 40);
		usernameText.setText(_usernameListString);
		contentPane.add(usernameText);

		JTextArea portText = new JTextArea();
		portText.setWrapStyleWord(true);
		portText.setLineWrap(true);
		portText.setBounds(200, 125, 450, 40);
		portText.setText(_portListString);
		contentPane.add(portText);

		JTextArea ipText = new JTextArea();
		ipText.setLineWrap(true);
		ipText.setBounds(200, 75, 450, 40);
		ipText.setText(_ipListString);
		contentPane.add(ipText);

		countText = new JTextField();
		countText.setBounds(200, 27, 100, 20);
		countText.setText(String.valueOf(_customersCount));
		contentPane.add(countText);
		countText.setColumns(10);

		JButton saveBtn = new JButton("保存");
		saveBtn.setBounds(300, 300, 100, 25);
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 1. check value
				_customersCount = Integer.valueOf(countText.getText());
				_ipListString = ipText.getText();
				_portListString = portText.getText();
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
		
		contentPane.add(saveBtn);
		setVisible(true);
	}

	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			inputStream = new FileInputStream("./res/customers.properties");
			properties.load(inputStream);
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
		_customersCount = Integer.parseInt(properties.getProperty("COUNT"));
		_ipListString = properties.getProperty("IP_LIST");
		_portListString = properties.getProperty("PORT_LIST");
		_usernameListString = properties.getProperty("USERNAME_LIST");
		_passwordListString = properties.getProperty("PASSWORD_LIST");
		_ipList = _ipListString.split(",");
		String[] tempPortList = _portListString.split(",");
		_portList = new int[tempPortList.length];
		for (int i = 0; i < tempPortList.length; i++) {
			_portList[i] = Integer.parseInt(tempPortList[i]);
		}
		_usernameList = _usernameListString.split(",");
		_passwordList = _passwordListString.split(",");
		
		// TODO process parse exception
	}

	private boolean CheckConfig() {
		if (_customersCount <= 0) {
			return false;
		}

		_ipList = _ipListString.split(",");
		if (_ipList.length != _customersCount) {
			for (int i = 0; i < _ipList.length; i++) {
				if (_ipList[i].split(".").length != 4) {
					return false;
				}
			}
		}

		String[] tempPortList = _portListString.split(",");
		_portList = new int[tempPortList.length];
		for (int i = 0; i < tempPortList.length; i++) {
			_portList[i] = Integer.parseInt(tempPortList[i]);
			if (_portList[i] <= 0 || _portList[i] >= 65535) {
				return false;
			}
		}
		if (_portList.length != _customersCount) {
			return false;
		}

		_usernameList = _usernameListString.split(",");
		if (_usernameList.length != _customersCount) {
			return false;
		}

		_passwordList = _passwordListString.split(",");
		if (_passwordList.length != _customersCount) {
			return false;
		}
		return true;
	}

	private void SaveConfig() {
		Properties p = new Properties();
		FileOutputStream oFile = null;
		try {
			oFile = new FileOutputStream("./res/customers.properties");
			p.setProperty("COUNT", String.valueOf(_customersCount));
			p.setProperty("IP_LIST", _ipListString);
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
