package yt.java.com.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Black;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import main.java.com.netsdk.common.BorderEx;
import main.java.com.netsdk.common.Res;
import main.java.com.netsdk.lib.ToolKits;
import yt.java.com.service.YTFaceService;

public class YTFaceConfView extends JFrame {
	private static final long serialVersionUID = 1L;

	private static Logger _logger = LogManager.getLogger();

	private JSONObject _config;
	private JPanel _content_panel;

	private JPanel _save_panel;

	private ActionListener _action_listener = null;

	public YTFaceConfView() {
		// load config
		_logger.info("Load face config.");
		try {
			LoadConfig();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(this, "配置文件加载出错.", "错误", JOptionPane.WARNING_MESSAGE);
			return;
		}

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
		setTitle(Res.string().getFaceDevice() + Res.string().getConf());
		setLayout(null);
		pack();
		setSize(816, 580);
		setResizable(false);
		setLocationRelativeTo(null);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}


		// content panel
		_content_panel = new JPanel();
		_content_panel.setBounds(0, 0, 800, 500);
		_content_panel.setLayout(null);
		BorderEx.set(_content_panel, Res.string().getDeviceList(), 2);
		add(_content_panel);

		Redraw();

		// save panel
		_save_panel = new JPanel();
		_save_panel.setBounds(0, 500, 800, 60);
		_save_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(_save_panel, BorderLayout.SOUTH);
		JButton save_btn = new JButton(Res.string().getSave());
		_save_panel.add(save_btn);
		Dimension dimension = new Dimension(60, 22);
		save_btn.setPreferredSize(dimension);
		save_btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_action_listener != null) {
					_action_listener.actionPerformed(e);
				}
				try {
					SaveConfig();
					TipsDialog("保存成功");
					Dispose();
				} catch (IOException e2) {
					AlertDialog("保存配置出错，请检查配置填写");
				}
			}
		});

		setVisible(true);
	}

	private void LoadConfig() throws IOException {
		_config = YTFaceService.LoadConfig();
	}

	private void SaveConfig() throws IOException {
		YTFaceService.SaveConfig(_config);
	}
	
	private void Redraw() {
		_content_panel.removeAll();
		
		// dynamic draw module
		int device_count = 0;
		JSONArray device_list = null;
		if (_config != null) {
			try {
				device_count = _config.getIntValue("device_count");
				device_list = _config.getJSONArray("device_list");
			} catch (Exception e) {
				_logger.warn("Parse Face config error without device_count.");
			}

			if (device_count > 0 && device_list != null && device_list.size() == device_count) {
				// add device panel
				for (int i = 0; i < device_count; i++) {
					DevicePanel tempPanel = new DevicePanel(i, device_list.getJSONObject(i));
					_content_panel.add(tempPanel);
				}
			} else {
				_logger.warn("Parse Face config error with device_list.");
			}
		} else {
			_logger.info("Null config");
		}

		JButton add_button = new JButton(Res.string().getAdd());
		add_button.setBounds(20, 30 + device_count * 120, 80, 24);
		add_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// device add frame
				new DeviceDetailFrame();
			}
		});
		_content_panel.add(add_button);
	}

	private void AlertDialog(String message) {
		JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.WARNING_MESSAGE);
	}
	
	private void TipsDialog(String message) {
		JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
	}
	
	private void Dispose() {
		dispose();
	}
	
	public void OnSave(ActionListener l) {
		_action_listener = l;
	}

	class DevicePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JSONObject _content;

		public DevicePanel(int index, JSONObject content) {
			_content = content;

			String ip = "";
			int port = -1;
			String username = "";
			String password = "";
			JSONArray channel_list = null;

			try {
				ip = content.getString("ip");
				port = content.getIntValue("port");
				username = content.getString("username");
				password = content.getString("password");
				channel_list = content.getJSONArray("channel_list");
			} catch (Exception e) {
				_logger.warn("Parse Face config error by device info.");
				return;
			}

			setLayout(new FlowLayout());
			setBorder(new EmptyBorder(5, 0, 0, 0));
			setBounds(10, 20 + index * 120, 780, 120);

			JLabel title_label = new JLabel(Res.string().getFaceDevice() + "-" + (index + 1));
			title_label.setPreferredSize(new Dimension(780, 30));
			add(title_label);

			Dimension titleDimension = new Dimension(50, 18);
			Dimension textDimension = new Dimension(100, 20);

			JLabel ipLabel = new JLabel(Res.string().getDeviceIp(), JLabel.CENTER);
			JTextField ipTextArea = new JTextField(ip);
			ipLabel.setPreferredSize(titleDimension);
			ipTextArea.setPreferredSize(textDimension);
			ipTextArea.setEditable(false);

			JLabel portLabel = new JLabel(Res.string().getPort(), JLabel.CENTER);
			JTextField portTextArea = new JTextField(String.valueOf(port));
			portLabel.setPreferredSize(titleDimension);
			portTextArea.setPreferredSize(textDimension);
			portTextArea.setEditable(false);

			JLabel usernameLabel = new JLabel(Res.string().getUserName(), JLabel.CENTER);
			JTextField usernameTextArea = new JTextField(username);
			usernameLabel.setPreferredSize(titleDimension);
			usernameTextArea.setPreferredSize(textDimension);
			usernameTextArea.setEditable(false);

			JLabel passwordLabel = new JLabel(Res.string().getPassword(), JLabel.CENTER);
			JTextField passwordTextArea = new JTextField(password);
			passwordLabel.setPreferredSize(titleDimension);
			passwordTextArea.setPreferredSize(textDimension);
			passwordTextArea.setEditable(false);

			Dimension dimension = new Dimension();
			dimension.width = 180;
			setPreferredSize(dimension);
			add(ipLabel);
			add(ipTextArea);
			add(portLabel);
			add(portTextArea);
			add(usernameLabel);
			add(usernameTextArea);
			add(passwordLabel);
			add(passwordTextArea);

			// edit button
			JButton edit_button = new JButton(Res.string().getEdit());
			edit_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new DeviceDetailFrame(_content);
				}
			});
			add(edit_button);

			// delete button
			JButton delete_button = new JButton(Res.string().getDelete());
			delete_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
				}
			});
			add(delete_button);

			ChannelPanel channel_panel = new ChannelPanel(ip, index, channel_list);
			add(channel_panel);
		}
	}

	class ChannelPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JTextField _nickname_textfield;
		private JSONArray _device_list;
		private JComboBox<String> _channel_combobox;
		private String _ip;

		public ChannelPanel(String ip, int index, JSONArray device_list) {
			_device_list = device_list;
			_ip = ip;

			setLayout(new FlowLayout());
			setBorder(new EmptyBorder(5, 0, 0, 0));
			setSize(780, 30);

			JLabel ipLabel = new JLabel(Res.string().getChannel(), JLabel.CENTER);
			_channel_combobox = new JComboBox<String>();
			ipLabel.setPreferredSize(new Dimension(50, 18));
			_channel_combobox.setPreferredSize(new Dimension(100, 20));

			for (int i = 0; i < device_list.size(); i++) {
				_channel_combobox.addItem(device_list.getJSONObject(i).getString("channel"));
			}
			_channel_combobox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int temp_index = _channel_combobox.getSelectedIndex();
					_nickname_textfield.setText(_device_list.getJSONObject(temp_index).getString("nickname"));
				}
			});
			add(ipLabel);
			add(_channel_combobox);

			JLabel nickname_label = new JLabel(Res.string().getNickname(), JLabel.CENTER);
			nickname_label.setPreferredSize(new Dimension(50, 18));
			_nickname_textfield = new JTextField(device_list.getJSONObject(0).getString("nickname"));
			_nickname_textfield.setPreferredSize(new Dimension(150, 20));
			_nickname_textfield.setEditable(false);

			add(nickname_label);
			add(_nickname_textfield);

			// add channel button
			JButton add_channel_button = new JButton(Res.string().getAdd());
			add_channel_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new ChannelDetailFrame(_ip);
				}
			});
			add(add_channel_button);

			// edit channel button
			JButton edit_channel_button = new JButton(Res.string().getEdit());
			edit_channel_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new ChannelDetailFrame(_ip, Integer.valueOf((String) _channel_combobox.getSelectedItem()),
							_nickname_textfield.getText());
				}
			});
			add(edit_channel_button);

			// delete channel button
			JButton delete_channel_button = new JButton(Res.string().getDelete());
			add(delete_channel_button);
		}
	}

	class DeviceDetailFrame extends JFrame {

		private static final long serialVersionUID = 1L;
		Dimension textDimension;
		Dimension titleDimension;

		JTextField _ip_textfield;
		JTextField _port_textfield;
		JTextField _username_textfield;
		JTextField _password_textfield;

		// add
		public DeviceDetailFrame() {
			setTitle(Res.string().getAdd() + Res.string().getFaceDevice());
			InitView();
		}

		// edit
		public DeviceDetailFrame(JSONObject content) {
			setTitle(Res.string().getEdit() + Res.string().getFaceDevice());
			InitView();
			SetContent(content);
		}

		private void InitView() {
			setLayout(new BorderLayout());
			pack();
			setSize(300, 200);
			setResizable(false);
			setLocationRelativeTo(null);

			titleDimension = new Dimension(50, 18);
			textDimension = new Dimension(120, 20);

			JLabel ipLabel = new JLabel(Res.string().getDeviceIp(), JLabel.CENTER);
			_ip_textfield = new JTextField();
			ipLabel.setPreferredSize(textDimension);
			_ip_textfield.setPreferredSize(textDimension);

			JLabel portLabel = new JLabel(" " + Res.string().getPort(), JLabel.CENTER);
			_port_textfield = new JTextField();
			portLabel.setPreferredSize(textDimension);
			_port_textfield.setPreferredSize(textDimension);

			JLabel usernameLabel = new JLabel(" " + Res.string().getUserName(), JLabel.CENTER);
			_username_textfield = new JTextField();
			usernameLabel.setPreferredSize(textDimension);
			_username_textfield.setPreferredSize(textDimension);

			JLabel passwordLabel = new JLabel(" " + Res.string().getPassword(), JLabel.CENTER);
			_password_textfield = new JTextField();
			passwordLabel.setPreferredSize(textDimension);
			_password_textfield.setPreferredSize(textDimension);

			JButton saveBtn = new JButton(Res.string().getSave());

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
			content_panel.add(_ip_textfield);
			content_panel.add(portLabel);
			content_panel.add(_port_textfield);
			content_panel.add(usernameLabel);
			content_panel.add(_username_textfield);
			content_panel.add(passwordLabel);
			content_panel.add(_password_textfield);

			add_panel.add(saveBtn);

			saveBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});

			setVisible(true);
		}

		private void SetContent(JSONObject content) {
			String ip = "";
			int port = -1;
			String username = "";
			String password = "";

			try {
				ip = content.getString("ip");
				port = content.getIntValue("port");
				username = content.getString("username");
				password = content.getString("password");
			} catch (Exception e) {
				_logger.warn("Parse Face config error by device info.");
				return;
			}
			_ip_textfield.setText(ip);
			_port_textfield.setText(String.valueOf(port));
			_username_textfield.setText(username);
			_password_textfield.setText(password);
		}

		private void OnSave() {

		}
	}

	class ChannelDetailFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private String _ip;
		private int _channel;
		private String _nickname;
		private JTextField _channel_textfield;
		private JTextField _nickname_textfield;

		// add
		ChannelDetailFrame(String ip) {
			_ip = ip;
			setTitle(Res.string().getAdd() + Res.string().getChannel());
			InitView();
		}

		// edit
		ChannelDetailFrame(String ip, int channel, String nickname) {
			_ip = ip;
			_channel = channel;
			_nickname = nickname;
			setTitle(Res.string().getEdit() + Res.string().getChannel());
			InitView();
			SetContent(_channel, _nickname);
		}

		// init view
		private void InitView() {
			setLayout(new BorderLayout());
			pack();
			setSize(300, 200);
			setResizable(false);
			setLocationRelativeTo(null);

			Dimension titleDimension = new Dimension(80, 18);
			Dimension textDimension = new Dimension(120, 20);

			JLabel ipLabel = new JLabel(Res.string().getDeviceIp(), JLabel.CENTER);
			JTextField ipTextArea = new JTextField(_ip);
			ipLabel.setPreferredSize(titleDimension);
			ipTextArea.setPreferredSize(textDimension);
			ipTextArea.setEditable(false);

			JLabel channel_label = new JLabel(Res.string().getChannel(), JLabel.CENTER);
			_channel_textfield = new JTextField();
			channel_label.setPreferredSize(titleDimension);
			_channel_textfield.setPreferredSize(textDimension);

			JLabel nickname_label = new JLabel(" " + Res.string().getUserName(), JLabel.CENTER);
			_nickname_textfield = new JTextField();
			nickname_label.setPreferredSize(titleDimension);
			_nickname_textfield.setPreferredSize(textDimension);

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
			content_panel.add(channel_label);
			content_panel.add(_channel_textfield);
			content_panel.add(nickname_label);
			content_panel.add(_nickname_textfield);

			JButton save_button = new JButton(Res.string().getSave());
			add_panel.add(save_button);

			save_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});

			setVisible(true);
		}

		private void SetContent(int channel, String nickname) {
			_channel_textfield.setText("" + channel);
			_nickname_textfield.setText(nickname);
		}

		// save
		private void OnSave() {

		}
	}

	public static void main(String[] args) {
		YTFaceConfView view = new YTFaceConfView();
	}
}
