package yt.java.com.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.com.netsdk.common.BorderEx;
import main.java.com.netsdk.common.Res;
import yt.java.com.controller.ServiceLauncher;

public class YTMainView extends JFrame implements Runnable {
	private static Logger _logger = LogManager.getLogger();
	private JPanel MainPanel;
	public YTServiceView telecomViewer; // 通讯服务UI视图对象
	public YTServiceView faceViewer; // 人脸识别服务UI对象
	public YTServiceView customerViewer; // 客流统计服务UI视图对象
	public YTServiceView accessViewer; // 门禁控制服务UI视图对象
	private String _version = "1.0.0";

	public YTMainView() {
		// load config
		_logger.info("Load main config.");
		LoadConfig();
		
		// init view
		_logger.info("Show main view.");
		InitView();
		
		// bind event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_logger.info("on close");
				int i = JOptionPane.showConfirmDialog(null, "确定要退出系统吗？", "退出系统", JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.YES_OPTION) {
					_logger.info("YTMonitor>>>CLOSE!!!");
					System.exit(0);
				} else {
					_logger.info("cancel close");
				}
			}
		});
	}

	private void InitView() {
		setTitle("YT Monitor V" + _version);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // default is hidden
		
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	e.printStackTrace();
        } 
		
		setBounds(100, 100, 700, 320);
		MainPanel = new JPanel();
		MainPanel.setBorder(null);
		setContentPane(MainPanel);
		MainPanel.setLayout(null);

		JPanel HeadPanel = new JPanel();
		HeadPanel.setBounds(0, 0, 692, 60);
		MainPanel.add(HeadPanel);
		HeadPanel.setLayout(null);

		JLabel TitleLabel = new JLabel(Res.string().getSoftware());
		TitleLabel.setBounds(282, 30, 128, 22);
		HeadPanel.add(TitleLabel);
		TitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setBounds(0, 0, 120, 25);
		HeadPanel.add(menuBar);

		JMenu menu = new JMenu(Res.string().getService());
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem(Res.string().getAutoRun());
		menu.add(menuItem);

		JMenuItem menuItem_1 = new JMenuItem(Res.string().getExit());
		menu.add(menuItem_1);

		JMenu menu_1 = new JMenu(Res.string().getLog());
		menuBar.add(menu_1);

		JMenuItem menuItem_2 = new JMenuItem(Res.string().getLogLevel());
		menu_1.add(menuItem_2);

		JMenuItem menuItem_3 = new JMenuItem(Res.string().getLogAutoUpload());
		menu_1.add(menuItem_3);
		
		JMenuItem menuItem_4 = new JMenuItem(Res.string().getLogUpload());
		menu_1.add(menuItem_4);
		

		JPanel BodyPanel = new JPanel();
		BorderEx.set(BodyPanel, Res.string().getServiceList(), 2);
		BodyPanel.setBounds(10, 70, 664, 190);
		MainPanel.add(BodyPanel);
		BodyPanel.setLayout(null);

		// 服务列表
		JPanel ServiceTabPanel = new JPanel();
		ServiceTabPanel.setBounds(25, 20, 600, 150);
		BodyPanel.add(ServiceTabPanel);
		ServiceTabPanel.setLayout(null);

		JPanel ServiceTabHeadPanel = new JPanel();
		ServiceTabHeadPanel.setBounds(0, 0, 630, 30);
		ServiceTabPanel.add(ServiceTabHeadPanel);
		ServiceTabHeadPanel.setLayout(null);

		JLabel TabTitleLabel_1 = new JLabel(Res.string().getStatus(), SwingConstants.CENTER);
		TabTitleLabel_1.setBounds(0, 0, 60, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_1);

		JLabel TabTitleLabel_2 = new JLabel(Res.string().getCount(), SwingConstants.CENTER);
		TabTitleLabel_2.setBounds(60, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_2);

		JLabel TabTitleLabel_3 = new JLabel(Res.string().getModule(), SwingConstants.CENTER);
		TabTitleLabel_3.setBounds(150, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_3);

		JLabel TabTitleLabel_4 = new JLabel(Res.string().getDetail(), SwingConstants.CENTER);
		TabTitleLabel_4.setBounds(240, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_4);

		JLabel TabTitleLabel_5 = new JLabel(Res.string().getAction(), SwingConstants.CENTER);
		TabTitleLabel_5.setBounds(330, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_5);

		JLabel TabTitleLabel_6 = new JLabel(Res.string().getConf(), SwingConstants.CENTER);
		TabTitleLabel_6.setBounds(420, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_6);

		JLabel TabTitleLabel_7 = new JLabel(Res.string().getLog(), SwingConstants.CENTER);
		TabTitleLabel_7.setBounds(510, 0, 90, 30);
		ServiceTabHeadPanel.add(TabTitleLabel_7);

		JPanel ServiceTabLine_1 = new JPanel();
		ServiceTabLine_1.setLayout(null);
		ServiceTabLine_1.setBounds(0, 30, 630, 30);
		ServiceTabPanel.add(ServiceTabLine_1);

		JPanel StatusPanel_1 = new JPanel();
		StatusPanel_1.setBackground(Color.LIGHT_GRAY);
		StatusPanel_1.setBounds(20, 5, 20, 20);
		ServiceTabLine_1.add(StatusPanel_1);

		JLabel PidLabel_1 = new JLabel("0", SwingConstants.CENTER);
		PidLabel_1.setBounds(60, 0, 90, 30);
		ServiceTabLine_1.add(PidLabel_1);

		JLabel ModuleLabel_1 = new JLabel("通讯中间件", SwingConstants.CENTER);
		ModuleLabel_1.setBounds(150, 0, 90, 30);
		ServiceTabLine_1.add(ModuleLabel_1);

		JButton DetailBtn_1 = new JButton("Detail");
		DetailBtn_1.setBounds(250, 5, 70, 20);
		ServiceTabLine_1.add(DetailBtn_1);

		JButton StartBtn_1 = new JButton("Start");
		StartBtn_1.setBounds(340, 5, 70, 20);
		ServiceTabLine_1.add(StartBtn_1);

		JButton ConfBtn_1 = new JButton("Conf");
		ConfBtn_1.setBounds(430, 5, 70, 20);
		ServiceTabLine_1.add(ConfBtn_1);

		JButton LogBtn_1 = new JButton("Log");
		LogBtn_1.setBounds(520, 5, 70, 20);
		ServiceTabLine_1.add(LogBtn_1);

		JPanel ServiceTabLine_2 = new JPanel();
		ServiceTabLine_2.setLayout(null);
		ServiceTabLine_2.setBounds(0, 60, 630, 30);
		ServiceTabPanel.add(ServiceTabLine_2);

		JPanel StatusPanel_2 = new JPanel();
		StatusPanel_2.setBackground(Color.LIGHT_GRAY);
		StatusPanel_2.setBounds(20, 5, 20, 20);
		ServiceTabLine_2.add(StatusPanel_2);

		JLabel PidLabel_2 = new JLabel("0", SwingConstants.CENTER);
		PidLabel_2.setBounds(60, 0, 90, 30);
		ServiceTabLine_2.add(PidLabel_2);

		JLabel ModuleLabel_2 = new JLabel("人脸抓拍", SwingConstants.CENTER);
		ModuleLabel_2.setBounds(150, 0, 90, 30);
		ServiceTabLine_2.add(ModuleLabel_2);

		JButton DetailBtn_2 = new JButton("Detail");
		DetailBtn_2.setBounds(250, 5, 70, 20);
		ServiceTabLine_2.add(DetailBtn_2);

		JButton StartBtn_2 = new JButton("Start");
		StartBtn_2.setBounds(340, 5, 70, 20);
		ServiceTabLine_2.add(StartBtn_2);

		JButton ConfBtn_2 = new JButton("Conf");
		ConfBtn_2.setBounds(430, 5, 70, 20);
		ServiceTabLine_2.add(ConfBtn_2);

		JButton LogBtn_2 = new JButton("Log");
		LogBtn_2.setBounds(520, 5, 70, 20);
		ServiceTabLine_2.add(LogBtn_2);

		JPanel ServiceTabLine_3 = new JPanel();
		ServiceTabLine_3.setBounds(0, 90, 630, 30);
		ServiceTabPanel.add(ServiceTabLine_3);
		ServiceTabLine_3.setLayout(null);

		JPanel StatusPanel_3 = new JPanel();
		StatusPanel_3.setBackground(Color.LIGHT_GRAY);
		StatusPanel_3.setBounds(20, 5, 20, 20);
		ServiceTabLine_3.add(StatusPanel_3);

		JLabel PidLabel_3 = new JLabel("0", SwingConstants.CENTER);
		PidLabel_3.setBounds(60, 0, 90, 30);
		ServiceTabLine_3.add(PidLabel_3);

		JLabel ModuleLabel_3 = new JLabel("客流统计", SwingConstants.CENTER);
		ModuleLabel_3.setBounds(150, 0, 90, 30);
		ServiceTabLine_3.add(ModuleLabel_3);

		JButton DetailBtn_3 = new JButton("Detail");
		DetailBtn_3.setBounds(250, 5, 70, 20);
		ServiceTabLine_3.add(DetailBtn_3);

		JButton StartBtn_3 = new JButton("Start");
		StartBtn_3.setBounds(340, 5, 70, 20);
		ServiceTabLine_3.add(StartBtn_3);

		JButton ConfBtn_3 = new JButton("Conf");
		ConfBtn_3.setBounds(430, 5, 70, 20);
		ServiceTabLine_3.add(ConfBtn_3);

		JButton LogBtn_3 = new JButton("Log");
		LogBtn_3.setBounds(520, 5, 70, 20);
		ServiceTabLine_3.add(LogBtn_3);

		JPanel ServiceTabLine_4 = new JPanel();
		ServiceTabLine_4.setLayout(null);
		ServiceTabLine_4.setBounds(0, 120, 630, 30);
		ServiceTabPanel.add(ServiceTabLine_4);

		JPanel StatusPanel_4 = new JPanel();
		StatusPanel_4.setBackground(Color.LIGHT_GRAY);
		StatusPanel_4.setBounds(20, 5, 20, 20);
		ServiceTabLine_4.add(StatusPanel_4);

		JLabel PidLabel_4 = new JLabel("0", SwingConstants.CENTER);
		PidLabel_4.setBounds(60, 0, 90, 30);
		ServiceTabLine_4.add(PidLabel_4);

		JLabel ModuleLabel_4 = new JLabel("门禁控制", SwingConstants.CENTER);
		ModuleLabel_4.setBounds(150, 0, 90, 30);
		ServiceTabLine_4.add(ModuleLabel_4);

		JButton DetailBtn_4 = new JButton("Detail");
		DetailBtn_4.setBounds(250, 5, 70, 20);
		ServiceTabLine_4.add(DetailBtn_4);

		JButton StartBtn_4 = new JButton("Start");
		StartBtn_4.setBounds(340, 5, 70, 20);
		ServiceTabLine_4.add(StartBtn_4);

		JButton ConfBtn_4 = new JButton("Conf");
		ConfBtn_4.setBounds(430, 5, 70, 20);
		ServiceTabLine_4.add(ConfBtn_4);

		JButton LogBtn_4 = new JButton("Log");
		LogBtn_4.setBounds(520, 5, 70, 20);
		ServiceTabLine_4.add(LogBtn_4);

		JPanel BottomPanel = new JPanel();
		BottomPanel.setBounds(0, 240, 692, 230);
		MainPanel.add(BottomPanel);
		BottomPanel.setLayout(null);
		
		setVisible(true);
		
		this.telecomViewer = new YTServiceView(StatusPanel_1, PidLabel_1, ModuleLabel_1, DetailBtn_1, StartBtn_1,
				ConfBtn_1, LogBtn_1);
		this.faceViewer = new YTServiceView(StatusPanel_2, PidLabel_2, ModuleLabel_2, DetailBtn_2, StartBtn_2,
				ConfBtn_2, LogBtn_2);
		this.customerViewer = new YTServiceView(StatusPanel_3, PidLabel_3, ModuleLabel_3, DetailBtn_3, StartBtn_3,
				ConfBtn_3, LogBtn_3);
		this.accessViewer = new YTServiceView(StatusPanel_4, PidLabel_4, ModuleLabel_4, DetailBtn_4, StartBtn_4,
				ConfBtn_4, LogBtn_4);
	}

	private void LoadConfig() {
		// Get config
		Properties properties;
		FileInputStream inputStream;
		try {
			properties = new Properties();
			inputStream = new FileInputStream("./res/main.properties");
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
		_version = properties.getProperty("VERSION");
	}

	@Override
	public void run() {

	}

	public YTServiceView GetTelecomViewer() {
		return this.telecomViewer;
	}

	public YTServiceView GetCustomerViewer() {
		return this.customerViewer;
	}

	public YTServiceView GetAccessViewer() {
		return this.accessViewer;
	}

	public YTServiceView GetFaceViewer() {
		return this.faceViewer;
	}

}
