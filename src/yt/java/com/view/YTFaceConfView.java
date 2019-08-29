package yt.java.com.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import main.java.com.netsdk.common.BorderEx;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.lib.ToolKits;
import yt.java.com.service.YTFaceService;

public class YTFaceConfView extends JFrame {
	private static Logger _logger = LogManager.getLogger();

	private JSONObject _config;
	
	public YTFaceConfView() {
		// load config
		_logger.info("Load face config.");
		LoadConfig();

		// init view
		_logger.info("Show face config view.");
		InitView();

		// bind event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("Close face config view.");
			}
		});
	}

	private void InitView() {
		setTitle("人脸识别配置");
		setLayout(new BorderLayout());
		pack();
		setSize(800, 560);
		setResizable(false);
		setLocationRelativeTo(null);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// main panel add scroll pane
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);

		// dynamic draw module
		if (_config != null) {
			
		}

		JPanel add_panel = new JPanel();
		add(add_panel, BorderLayout.SOUTH);
		JButton add_btn = new JButton(Res.string().getAdd());
		add_panel.add(add_btn);
		Dimension dimension = new Dimension(60, 22);
		add_btn.setPreferredSize(dimension);
		add_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// device add frame
				new DeviceAddFrame();
			}
		});

		setVisible(true);
	}

	private void LoadConfig() {
		_config = YTFaceService.LoadConfig();
	}

	private void Redraw() {
		
	}

	public void OnSave(ActionListener actionListener) {

	}

	class DeviceAddFrame extends JFrame {
		
		Dimension textDimension;
		Dimension titleDimension;
		
		public DeviceAddFrame() {
			titleDimension = new Dimension(50, 18);
			textDimension = new Dimension(120, 20);
			
			setTitle("添加人脸识别设备");
			setLayout(new BorderLayout());
			pack();
			setSize(300, 200);
			setResizable(false);
			setLocationRelativeTo(null);

			JLabel ipLabel = new JLabel(Res.string().getDeviceIp(), JLabel.CENTER);
			JTextField ipTextArea = new JTextField();
			ipLabel.setPreferredSize(textDimension);
			ipTextArea.setPreferredSize(textDimension);
			
			JLabel portLabel = new JLabel(" " + Res.string().getPort(), JLabel.CENTER);
			JTextField portTextArea = new JTextField();
			portLabel.setPreferredSize(textDimension);
			portTextArea.setPreferredSize(textDimension);

			JLabel usernameLabel = new JLabel(" " + Res.string().getUserName(), JLabel.CENTER);
			JTextField usernameTextArea = new JTextField();
			usernameLabel.setPreferredSize(textDimension);
			usernameTextArea.setPreferredSize(textDimension);

			JLabel passwordLabel = new JLabel(" " + Res.string().getPassword(), JLabel.CENTER);
			JTextField passwordTextArea = new JPasswordField();
			passwordLabel.setPreferredSize(textDimension);
			passwordTextArea.setPreferredSize(textDimension);

			JButton addBtn = new JButton(Res.string().getAdd());
			
			JPanel content_panel = new JPanel();
			JPanel add_panel = new JPanel();
			add(content_panel, BorderLayout.CENTER);
			add(add_panel, BorderLayout.SOUTH);
			
			content_panel.setBorder(new EmptyBorder(0, 5, 5, 5));
			content_panel.setLayout(new FlowLayout());
			Dimension dimension = new Dimension();
			dimension.width = 200;
			content_panel.setPreferredSize(dimension);
			content_panel.add(ipLabel);
			content_panel.add(ipTextArea);
			content_panel.add(portLabel);
			content_panel.add(portTextArea);
			content_panel.add(usernameLabel);
			content_panel.add(usernameTextArea);
			content_panel.add(passwordLabel);
			content_panel.add(passwordTextArea);
			
			add_panel.add(addBtn);
			
			addBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});
			
			setVisible(true);
		}
	}

	class DeviceAddPanel extends JPanel {
		public JLabel nameLabel;
		public JLabel passwordLabel;
		public JLabel ipLabel;
		public JLabel portLabel;

		public JTextField ipTextArea;
		public JTextField portTextArea;
		public JTextField nameTextArea;
		public JPasswordField passwordTextArea;

		public JButton addBtn;
		public JButton logoutBtn;

		DeviceAddPanel() {
			BorderEx.set(this, Res.string().getAddDevice(), 2);
			setLayout(new FlowLayout());

			////////////////////////////////
			addBtn = new JButton(Res.string().getAdd());
			ipLabel = new JLabel(Res.string().getDeviceIp());
			portLabel = new JLabel(" " + Res.string().getPort());
			nameLabel = new JLabel(" " + Res.string().getUserName());
			passwordLabel = new JLabel(" " + Res.string().getPassword());
			ipTextArea = new JTextField();
			nameTextArea = new JTextField();
			passwordTextArea = new JPasswordField();
			portTextArea = new JTextField();

			add(ipLabel);
			add(ipTextArea);
			add(portLabel);
			add(portTextArea);
			add(nameLabel);
			add(nameTextArea);
			add(passwordLabel);
			add(passwordTextArea);
			add(addBtn);

			ipTextArea.setPreferredSize(new Dimension(90, 20));
			nameTextArea.setPreferredSize(new Dimension(90, 20));
			passwordTextArea.setPreferredSize(new Dimension(90, 20));
			portTextArea.setPreferredSize(new Dimension(90, 20));

			addBtn.setPreferredSize(new Dimension(80, 20));
			ToolKits.limitTextFieldLength(portTextArea, 6);
		}
	}

	class DeviceAddChannelPanel extends JPanel {
		DeviceAddChannelPanel() {

		}
	}

	public static void main(String[] args) {
		YTFaceConfView view = new YTFaceConfView();
	}
}
