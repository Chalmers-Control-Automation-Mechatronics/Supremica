package org.supremica.external.processeditor.tools.db;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginWindow extends JInternalFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private JButton connectButton;
	private JButton exitButton;
	private JTextField hostNameField;
	private JTextField portField;
	private JTextField userNameField;
	private JPasswordField passwordField;
	protected DBInterface dbi;
	
	private Connect dbConnect = null;		// Connection setup object
	
	public LoginWindow(DBInterface dbi) {
		super("Connect to host",false,false);
		this.dbi = dbi;
		GridBagLayout m = new GridBagLayout();
		setLayout(m);
		GridBagConstraints con;
		
		JLabel hostNameLabel = new JLabel("Host name");
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 2;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(hostNameLabel,con);
		add(hostNameLabel);
		
		JLabel portLabel = new JLabel("Port");
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 0;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(portLabel,con);
		add(portLabel);
		
		hostNameField = new JTextField("129.16.80.73",15);		//Host name
		hostNameField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 1;
		con.gridwidth = 2;
		con.insets = new Insets(0,0,15,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(hostNameField,con);
		add(hostNameField);
		
		portField = new JTextField("1433",7);		//Port
		portField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,15,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(portField,con);
		add(portField);
		
		JLabel userNameLabel = new JLabel("User name");
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 2;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(userNameLabel,con);
		add(userNameLabel);
		
		JLabel passwordLabel = new JLabel("Password");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.gridwidth = 2;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(passwordLabel,con);
		add(passwordLabel);
		
		userNameField = new JTextField("test123",12);		//User name
		userNameField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 3;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(userNameField,con);
		add(userNameField);
		
		passwordField = new JPasswordField("test123",10);		//Password
		passwordField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 3;
		con.gridwidth = 2;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(passwordField,con);
		add(passwordField);
		
		connectButton = new JButton("OK");		//Connect button
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 4;
		con.gridwidth = 1;
		con.insets = new Insets(25,0,5,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(connectButton,con);
		add(connectButton);
		connectButton.addActionListener(this);
		
		exitButton = new JButton("Exit");		//Exit button
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 4;
		con.gridwidth = 1;
		con.insets = new Insets(25,0,5,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(exitButton,con);
		add(exitButton);
		exitButton.addActionListener(this);

		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e){
		if (e.getSource() == connectButton || e.getSource() == hostNameField || e.getSource() == portField
			|| e.getSource() == userNameField || e.getSource() == passwordField) {
			String hostNameStr = hostNameField.getText();
			String portNumberStr = portField.getText();
			String userNameStr = userNameField.getText();
			char[] passwordChars = passwordField.getPassword();
			String passwordStr = new String(passwordChars);
			dbConnect = new Connect("jdbc:sqlserver://", hostNameStr, portNumberStr, "ProductionControlDB", 
				userNameStr, passwordStr, "direct");
			dbConnect.displayDbProperties();
			DBInterface.setDBConnection(dbConnect);
			dbi.refreshProjects();
			this.dispose();
		}
		else if (e.getSource() == exitButton){
			this.dispose();
		}
	}
}