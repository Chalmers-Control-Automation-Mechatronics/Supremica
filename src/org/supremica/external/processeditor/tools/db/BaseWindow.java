package org.supremica.external.processeditor.tools.db;

import java.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class BaseWindow extends JFrame implements ActionListener {

/*
	--< Init >--
*/
	public JDesktopPane desktop;
	private JButton insertButton;
	private JButton extractButton;
	private JButton connectButton;
	private JButton disconnectButton;

	private JLabel insertLabel;
	private JLabel projectIDLabel;
	private JLabel projectNameLabel;
	private JLabel descriptionLabel;
	private JTextField projectIDField;
	private JTextField projectNameField;
	private JTextField descriptionField;
	
	private JLabel extractLabel;
	private JLabel projectIDLabel2;
	private JLabel ROPNameIDLabel;
	private JLabel filePathLabel;
	private JTextField projectIDField2;
	private JTextField ROPNameIDField;
	private JTextField filePathField;

	private static JTextArea printArea;
	private JScrollPane scrollPane;
	
	private JPanel fieldPanel;
	private JPanel insertPanel;
	private JPanel glassPanel;
	
	private ConnectWindow connectWindow = null;
	private Connect dbConnect = null;
	
/*
	--< Constructor >--
*/
	public BaseWindow() {
		setTitle("Database Connection Interface");
		desktop = new JDesktopPane();
		setContentPane(desktop);
		setSize(540,630);
		setLocationRelativeTo(null);
		
		GridBagLayout m = new GridBagLayout();
		setLayout(m);
		GridBagConstraints con;
		
		insertButton = new JButton("INSERT XML");		//INSERT Button
		insertButton.setPreferredSize(new Dimension(110,26));		
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(2,0,10,10);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(insertButton,con);
		add(insertButton);
		insertButton.addActionListener(this);
		
		extractButton = new JButton("EXTRACT XML");		//EXTRACT Button
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(2,0,10,10);
		con.anchor = GridBagConstraints.NORTH;
		m.setConstraints(extractButton,con);
		add(extractButton);
		extractButton.addActionListener(this);

		connectButton = new JButton("CONNECT");		//CONNECT Button
		connectButton.setPreferredSize(new Dimension(110,26));
		con = new GridBagConstraints();
		con.gridx = 3;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(2,0,10,10);
		con.anchor = GridBagConstraints.NORTH;
		m.setConstraints(connectButton,con);
		add(connectButton);
		connectButton.addActionListener(this);
		
		disconnectButton = new JButton("DISCONNECT");		//DISCONNECT Button
		disconnectButton.setPreferredSize(new Dimension(110,26));
		con = new GridBagConstraints();
		con.gridx = 4;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(2,0,10,0);
		con.anchor = GridBagConstraints.NORTH;
		m.setConstraints(disconnectButton,con);
		add(disconnectButton);
		disconnectButton.addActionListener(this);
		
		fieldPanel = new JPanel();		//Field Panel
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.gridwidth = 4;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(fieldPanel,con);
		add(fieldPanel);
		fieldPanel.setPreferredSize(new Dimension(480, 500));
		fieldPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray, 2));
		
		// Field panel

		GridBagLayout m2 = new GridBagLayout();
		fieldPanel.setLayout(m2);
		GridBagConstraints con2;
		
		insertLabel = new JLabel("< INSERT ROP >");
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 1;
		con2.gridwidth = 3;
		con2.insets = new Insets(10,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(insertLabel,con2);
		fieldPanel.add(insertLabel);
		insertLabel.setForeground(Color.gray); 

		projectIDLabel = new JLabel("Project ID");
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 2;
		con2.gridwidth = 1;
		con2.insets = new Insets(10,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(projectIDLabel,con2);
		fieldPanel.add(projectIDLabel);

		projectNameLabel = new JLabel("Project name");
		con2 = new GridBagConstraints();
		con2.gridx = 2;
		con2.gridy = 2;
		con2.gridwidth = 2;
		con2.insets = new Insets(10,10,5,0);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(projectNameLabel,con2);
		fieldPanel.add(projectNameLabel);

		projectIDField = new JTextField(10);		// Project ID Field
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 3;
		con2.gridwidth = 1;
		con2.insets = new Insets(5,10,10,10);
		m2.setConstraints(projectIDField,con2);
		fieldPanel.add(projectIDField);
		
		projectNameField = new JTextField(20);		// Project name Field
		con2 = new GridBagConstraints();
		con2.gridx = 2;
		con2.gridy = 3;
		con2.gridwidth = 2;
		con2.insets = new Insets(5,10,10,0);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(projectNameField,con2);
		fieldPanel.add(projectNameField);

		extractLabel = new JLabel("< EXTRACT ROP >");
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 5;
		con2.gridwidth = 3;
		con2.insets = new Insets(30,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(extractLabel,con2);
		fieldPanel.add(extractLabel);
		extractLabel.setForeground(Color.gray); 
						
		projectIDLabel2 = new JLabel("Project ID");
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 6;
		con2.gridwidth = 1;
		con2.insets = new Insets(10,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(projectIDLabel2,con2);
		fieldPanel.add(projectIDLabel2);
		
		ROPNameIDLabel = new JLabel("ROP ID");
		con2 = new GridBagConstraints();
		con2.gridx = 2;
		con2.gridy = 6;
		con2.gridwidth = 2;
		con2.insets = new Insets(10,10,5,0);		
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(ROPNameIDLabel,con2);
		fieldPanel.add(ROPNameIDLabel);

		projectIDField2 = new JTextField(10);		// Project ID Field
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 7;
		con2.gridwidth = 1;
		con2.insets = new Insets(5,10,10,10);
		m2.setConstraints(projectIDField2,con2);
		fieldPanel.add(projectIDField2);
		
		ROPNameIDField = new JTextField(20);		// ROP name ID Field
		con2 = new GridBagConstraints();
		con2.gridx = 2;
		con2.gridy = 7;
		con2.gridwidth = 2;
		con2.insets = new Insets(5,10,10,0);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(ROPNameIDField,con2);
		fieldPanel.add(ROPNameIDField);

		filePathLabel = new JLabel("Output file path");
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 8;
		con2.gridwidth = 1;
		con2.insets = new Insets(10,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(filePathLabel,con2);
		fieldPanel.add(filePathLabel);
		
		filePathField = new JTextField("c:\\\\xml_output\\\\ROPtest1.xml",32);		// Output file path Field
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 9;
		con2.gridwidth = 3;
		con2.insets = new Insets(10,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(filePathField,con2);
		fieldPanel.add(filePathField);
		
		printArea = new JTextArea("No connection..", 10, 38);
		printArea.setLineWrap(true);
		printArea.setWrapStyleWord(true);
		scrollPane = new JScrollPane(printArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		printArea.setCaretPosition(printArea.getDocument().getLength());
		con2 = new GridBagConstraints();
		con2.gridx = 1;
		con2.gridy = 10;
		con2.gridwidth = 3;
		con2.insets = new Insets(20,10,5,10);
		con2.anchor = GridBagConstraints.WEST;
		m2.setConstraints(scrollPane,con2);
		fieldPanel.add(scrollPane);
		printArea.setBackground(Color.WHITE);
		printArea.setBorder(BorderFactory.createEtchedBorder());

		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
/*
	--< Methods >--
*/
	public static JTextArea getPrintArea() {
		return printArea;
	}
	
/*
	--< Action performed >--
*/

	public void actionPerformed(ActionEvent e) {
		int projectID = 0;
		String projectName = "";
		String ROPNameID = "";
		String ROPXML = "";
		String filePath = "";
		File xmlFile;
		
		// Insert button
		
		if (e.getSource() == insertButton) {
			printArea.append("\n\nINSERT");
			if (connectWindow == null) {
				printArea.append("\nNot connected!");
			}
			else if (connectWindow.getDBConnection() == null) {
				printArea.append("\nNot connected!");
			}
			
			// If connected
			
			else {
				projectName = projectNameField.getText().trim();
				
				// Project ID
				
				if (projectName.isEmpty()) {
					try { 
						projectID = Integer.parseInt(projectIDField.getText().trim());
					}catch (NumberFormatException nfe){
						printArea.append("\nNumberFormatException: " + nfe.getMessage()); 
					}
					try {
						projectID = connectWindow.getDBConnection().checkProjectID(projectID);
					}catch (Exception eDBF) {
						printArea.append("\nUnsuccessful function call: " + eDBF.getMessage());
					}
				}
				
				// Project name
				
				else {
					try {
						projectID = connectWindow.getDBConnection().getProjectID(projectName);
					}catch (Exception eDBF) {
						printArea.append("\nUnsuccessful function call: " + eDBF.getMessage());
					}
				}
				printArea.append("\n--> Project name: " + projectName);
				printArea.append("\n--> Project ID: " + projectID);
				
				// File chooser & scanner
				
				if (projectID != 0) {
					final JFileChooser fc = new JFileChooser(); 
					int returnVal = fc.showOpenDialog(glassPanel); 
					if (returnVal == JFileChooser.APPROVE_OPTION){
						xmlFile = fc.getSelectedFile();
						try {
							Scanner scanner = new Scanner(xmlFile);
							while (scanner.hasNextLine()){
								ROPXML = ROPXML + scanner.nextLine();
								ROPXML.trim();
							} 
							scanner.close();
						}catch (IOException ex){
							printArea.append("\nIOException from Scanner: " + ex.getMessage());
						}					
						try {
							connectWindow.getDBConnection().setROPXML(projectID, ROPXML);
						}catch (Exception eDBF) {
							printArea.append("\nUnsuccessful function call: " + eDBF.getMessage());
						}
					}
					else {
						printArea.append("\nOpen command cancelled by user");
					}
				}
				else {
					printArea.append("\nNot a valid projectID!");
				}	
			}				
		}
		// Extract button
		
		else if (e.getSource() == extractButton) {
			printArea.append("\n\nEXTRACT");
			if (connectWindow == null) {
				printArea.append("\nNot connected!");
			}
			else if (connectWindow.getDBConnection() == null) {
				printArea.append("\nNot connected!");
			}
			else {
				try { 
					projectID = Integer.parseInt(projectIDField2.getText().trim());
				}catch (NumberFormatException nfe){
					printArea.append("\nNumberFormatException: " + nfe.getMessage()); 
				}
				ROPNameID = ROPNameIDField.getText();
				filePath = filePathField.getText();
				try {
					connectWindow.getDBConnection().getROPXML(projectID, ROPNameID, filePath);
				}catch (Exception eDBF) {
					printArea.append("\nUnsuccessful function call: " + eDBF.getMessage());
				}
			}
		}
		// Connect button
		
		else if (e.getSource() == connectButton) {
		
			// Glass Pane	
			glassPanel = (JPanel) getGlassPane();
			glassPanel.setLayout(new FlowLayout());
			glassPanel.setVisible(true);
	
			//	Connect window			
			connectWindow = new ConnectWindow();
			connectWindow.setPreferredSize(new Dimension(300,220));
			glassPanel.add(connectWindow);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
		
		// Disconnect button
		
		else if (e.getSource() == disconnectButton) {
			connectWindow = null;
			printArea.append("\n\nConnection is closed!");
		}
	}
	
/*
	--< Main >--
*/

	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run(){
					JFrame.setDefaultLookAndFeelDecorated(true);
					BaseWindow baseWindow = new BaseWindow();
            }
        });
    }
}