package yt.java.com.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import yt.java.com.service.YTCustomerService;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.com.netsdk.common.FunctionList;
import main.java.com.netsdk.demo.module.LoginModule;
import main.java.com.netsdk.demo.module.RealPlayModule;

import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class YTCustomerDetailView extends JFrame {

	private static Logger _logger = LogManager.getLogger();
	private JPanel _contentPane;
	private JTextField _startDateText;
	private JTextField _endDateText;
	private JButton _queryButton;
	private JTextArea _devDataText;

	public YTCustomerDetailView() {
		// load config
		_logger.info("Load customer config.");
		LoadConfig();
		
		// init view 
		_logger.info("Show customer detail view.");
		InitView();
		
		// bind event
		addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		_logger.info("Close customer config view.");
	    	}
	    });
	}
	
	private void LoadConfig() {
		
	}

	private void InitView() {
		setTitle("客流统计详情");
		setBounds(100, 100, 510, 345);
		_contentPane = new JPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		_contentPane.setLayout(null);
		
		JPanel QueryDataPanel = new JPanel();
		QueryDataPanel.setBounds(10, 10, 474, 293);
		_contentPane.add(QueryDataPanel);
		QueryDataPanel.setLayout(null);
		
		JLabel label_1 = new JLabel("结束日期");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(264, 228, 80, 20);
		QueryDataPanel.add(label_1);
		
		JLabel label = new JLabel("开始日期");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(10, 228, 80, 20);
		QueryDataPanel.add(label);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setBounds(10, 30, 454, 188);
		QueryDataPanel.add(scrollPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		_devDataText = new JTextArea();
		_devDataText.setLocation(17, 0);
		_devDataText.setEditable(false);
		scrollPane.setViewportView(_devDataText);
		_devDataText.setToolTipText("");
		_devDataText.setLineWrap(true);
		
		_queryButton = new JButton("查询");
		_queryButton.setBackground(Color.WHITE);
		_queryButton.setBounds(203, 260, 80, 23);
		QueryDataPanel.add(_queryButton);
		
		_startDateText = new JTextField();
		_startDateText.setBounds(90, 228, 120, 20);
		QueryDataPanel.add(_startDateText);
		_startDateText.setText("20190625");
		_startDateText.setColumns(10);
		
		_endDateText = new JTextField();
		_endDateText.setBounds(344, 228, 120, 20);
		QueryDataPanel.add(_endDateText);
		_endDateText.setText("20190709");
		_endDateText.setColumns(10);
		
		JLabel label_3 = new JLabel("设备状态");
		label_3.setHorizontalAlignment(SwingConstants.CENTER);
		label_3.setBounds(203, 10, 80, 15);
		QueryDataPanel.add(label_3);
		
		setVisible(true);
	}

	public void AddQueryAction(ActionListener ac) {
		_queryButton.addActionListener(ac);
	}
	
	public void SetDeviceStatus(String s) {
		_devDataText.setText(s);
	}
	
	public String GetStarttime() {
		return _startDateText.getText();
	}
	
	public String GetEndtime() {
		return _endDateText.getText();
	}
	
	public void alert(String message) {
		JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
	}
}
