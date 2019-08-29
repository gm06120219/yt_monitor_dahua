package yt.java.com.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YTAccessDetailView extends JFrame {
	private static Logger _logger = LogManager.getLogger();
	private JPanel _contentPane;
	private JComboBox<String> _nicknameComboBox;
	private String _nicknameListString;
	private String[] _nicknameList;
	private JTextArea _detailTextarea;
	private JScrollPane _scrollPane;
	private JButton _openBtn;
	private JButton _closeBtn;
	private JButton _recordBtn;
	private ActionListener _openBtnOnClick;
	private ActionListener _closeBtnOnClick;
	private ActionListener _recordBtnOnClick;

	public YTAccessDetailView() throws HeadlessException {
		// load configuration
		_logger.info("Load access config.");
		LoadConfig();
		
		// initialization view
		_logger.info("Show access detail view.");
		InitView();

		// add components dynamic
		ViewAddComponentsDynamic();

		// bind event process
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("Close access detail view.");
			}
		});
		
		_openBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_openBtnOnClick != null) {
					_openBtnOnClick.actionPerformed(e);
				}
			}
		});
		
		_closeBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (_closeBtnOnClick != null) {
					_closeBtnOnClick.actionPerformed(e);
				}

			}
		});
		
		_recordBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (_recordBtnOnClick != null) {
					_recordBtnOnClick.actionPerformed(e);
				}

			}
		});
	}

	private void InitView() {
		// parameters
		Border border = BorderFactory.createLineBorder(new Color(138, 138, 138));

		// main frame
		setTitle("门禁详情");
		setBounds(100, 100, 700, 400);

		// main content
		_contentPane = new JPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		_contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("选择门禁");
		lblNewLabel.setBounds(30, 30, 100, 15);
		lblNewLabel.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_contentPane.add(lblNewLabel);

		_nicknameComboBox = new JComboBox<String>();
		_nicknameComboBox.setBounds(150, 27, 150, 22);
		_nicknameComboBox.addItem("");
		_nicknameComboBox.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_contentPane.add(_nicknameComboBox);

		_openBtn = new JButton("打开");
		_openBtn.setBounds(325, 28, 80, 20);
		_openBtn.setBackground(Color.WHITE);
		_openBtn.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_contentPane.add(_openBtn);

		_closeBtn = new JButton("关闭");
		_closeBtn.setBounds(430, 28, 80, 20);
		_closeBtn.setBackground(Color.WHITE);
		_closeBtn.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_contentPane.add(_closeBtn);

		_recordBtn = new JButton("记录");
		_recordBtn.setBounds(535, 28, 80, 20);
		_recordBtn.setBackground(Color.WHITE);
		_recordBtn.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_contentPane.add(_recordBtn);

		_detailTextarea = new JTextArea();
		_detailTextarea.setBounds(0, 0, 605, 265);
		_detailTextarea.setBackground(Color.WHITE);
		_detailTextarea.setFont(new Font("微软雅黑 Light", Font.PLAIN, 10));
		_detailTextarea.setBorder(border);
		_detailTextarea.setEditable(false);
		_detailTextarea.setLineWrap(true);

		_scrollPane = new JScrollPane();
		_scrollPane.setViewportBorder(null);
		_scrollPane.setBounds(30, 70, 615, 265);
		_scrollPane.setBackground(Color.WHITE);
		_scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.setViewportView(_detailTextarea);
		_contentPane.add(_scrollPane);
		
		setVisible(true);
	}

	private void LoadConfig() {
		Properties properties;
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

		// Parse configuration
		_nicknameListString = properties.getProperty("NICKNAME_LIST");
		_nicknameList = _nicknameListString.split(",");
		
		// TODO process parse exception
	}

	private void ViewAddComponentsDynamic() {
		if(_nicknameList != null) {
			for (int i = 0; i < _nicknameList.length; i++) {
				_nicknameComboBox.addItem(_nicknameList[i]);
			}	
		}
	}

	public void SetText(String text) {
		_detailTextarea.setText(text + "\r\n");
	}

	public void AddText(String text) {
		String srcText = _detailTextarea.getText();
		_detailTextarea.setText(srcText + text + "\r\n");
	}

	public void ClearText() {
		_detailTextarea.setText("");
	}

	public void SetOpenClickListener(ActionListener listener) {
		_openBtnOnClick = listener;
	}

	public void SetCloseClickListener(ActionListener listener) {
		_closeBtnOnClick = listener;
	}

	public void SetRecordClickListener(ActionListener listener) {
		_recordBtnOnClick = listener;
	}

	public String GetNickname() {
		return (String) _nicknameComboBox.getSelectedItem();
	}
}
