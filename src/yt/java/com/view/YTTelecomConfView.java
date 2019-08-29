package yt.java.com.view;

import java.awt.EventQueue;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YTTelecomConfView extends JFrame {
	private static Logger _logger = LogManager.getLogger();
	private JPanel contentPane;
	private JTextField branchNameText;
	private JTextField roomNoText;
	private JTextField clientIdText;
	private JTextField urlText;
	private JTextField portText;
	private String _branchName;
	private String _roomNo;
	private String _clientId;
	private String _url;
	private int _port;
	private ActionListener _saveExtraActionListener = null;


	public YTTelecomConfView() {
		// load config
		_logger.info("Load telecom config.");
		LoadConfig();
		
		// init ui
		_logger.info("Show telecom config view.");
		InitView();
		
		// bind event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("Close telecom config view.");
			}
		});
	}

	private void InitView() {
		setTitle("通讯配置");
		setBounds(100, 100, 450, 380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("门店名称(拼音全拼)");
		lblNewLabel.setBounds(30, 30, 150, 15);
		contentPane.add(lblNewLabel);
		
		branchNameText = new JTextField();
		branchNameText.setText(_branchName);
		branchNameText.setColumns(10);
		branchNameText.setBounds(200, 27, 160, 20);
		contentPane.add(branchNameText);

		JLabel lblIp = new JLabel("房间编号（MQTT通讯）");
		lblIp.setBounds(30, 80, 150, 15);
		contentPane.add(lblIp);
		
		roomNoText = new JTextField();
		roomNoText.setText(_roomNo);
		roomNoText.setColumns(10);
		roomNoText.setBounds(200, 77, 160, 20);
		contentPane.add(roomNoText);

		JLabel lblid = new JLabel("客户端ID（MQTT通讯）");
		lblid.setBounds(30, 130, 150, 15);
		contentPane.add(lblid);
		
		clientIdText = new JTextField();
		clientIdText.setText(_clientId);
		clientIdText.setColumns(10);
		clientIdText.setBounds(200, 127, 160, 20);
		contentPane.add(clientIdText);
		
		JLabel lblmqtt = new JLabel("服务端地址（MQTT通讯）");
		lblmqtt.setBounds(30, 180, 150, 15);
		contentPane.add(lblmqtt);
		
		urlText = new JTextField();
		urlText.setText(_url);
		urlText.setColumns(10);
		urlText.setBounds(200, 177, 160, 20);
		contentPane.add(urlText);
		
		JLabel lblmqtt_1 = new JLabel("服务端端口（MQTT通讯）");
		lblmqtt_1.setBounds(30, 230, 150, 15);
		contentPane.add(lblmqtt_1);
		
		portText = new JTextField();
		portText.setText(String.valueOf(_port));
		portText.setColumns(10);
		portText.setBounds(200, 227, 160, 20);
		contentPane.add(portText);

		JButton saveBtn = new JButton("保存");
		saveBtn.setBounds(175, 300, 100, 25);
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 1. check value
				_branchName = branchNameText.getText();
				_roomNo = roomNoText.getText();
				_clientId = clientIdText.getText();
				_url = urlText.getText();
				_port = Integer.valueOf(portText.getText());

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
	}

	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			inputStream = new FileInputStream("./res/telecom.properties");
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
		// TODO process parse exception
		_branchName = properties.getProperty("BRANCH_NAME");
		_roomNo = properties.getProperty("ROOM_NO");
		_clientId = properties.getProperty("CLIENT_ID");
		_url = properties.getProperty("URL");
		_port = Integer.parseInt(properties.getProperty("PORT"));
	}

	private boolean CheckConfig() {
		// check branch name & room no & client id
		if (_branchName.length() == 0 || _roomNo.length() == 0 || _clientId.length() == 0) {
			return false;
		}
		
		// check url
		if (_url.split("\\.").length != 3) {
			return false;
		}
		
		// check port
		if (_port < 0 || _port > 65535) {
			return false;
		}
		
		return true;
	}

	private void SaveConfig() {
		Properties p = new Properties();
		FileOutputStream oFile = null;
		try {
			oFile = new FileOutputStream("./res/telecom.properties");
			p.setProperty("BRANCH_NAME", _branchName);
			p.setProperty("ROOM_NO", _roomNo);
			p.setProperty("CLIENT_ID", _clientId);
			p.setProperty("URL", _url);
			p.setProperty("PORT", String.valueOf(_port));
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
