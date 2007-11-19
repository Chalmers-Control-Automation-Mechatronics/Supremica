package org.supremica.external.processeditor.tools.db;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;

public class DBInterface extends JFrame implements ActionListener, ListSelectionListener {

/*
	--< Initialize >--
*/
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktop;
	private JPanel glassPanel;
	private JMenuBar menuBar;
	private JMenu sessionMenu;
	private JMenu encodingMenu;
	private JMenuItem connectItem, disconnectItem, exitItem,
							encUTF8Item, encUTF16Item;
	private JLabel projectLabel;
	private JLabel projectsLabel;
	private JLabel ROPLabel;
	private JLabel ROPsLabel;
	private JLabel messagesLabel;
	private JScrollPane pListScrollPane;
	private JScrollPane rListScrollPane;
	private JScrollPane scrollMPane;
	private JTextField projectField;
	private JList pList;
	private JList rList;
	private DefaultListModel pListModel;
	private DefaultListModel rListModel;
	private JButton useProjectButton;
	private JButton refreshPButton;
	private JButton refreshRButton;
	private JButton deletePButton;
	private JButton importButton;
	private JButton exportButton;
	private JButton deleteRButton;
	private static JTextArea printArea;
	
	private LoginWindow connectWindow = null;
	private static Connect dbConnect = null;		// Connection setup object
	private SOCGraphContainer graphContainer = null;
	
	private String encodingName = "UTF-8";
	private int projectID = 0;
	private String projectName = "";
	private String ROPNameID = "";

		
/*
	--< Constructor >--
*/
	public DBInterface() {
		setDefaultLookAndFeelDecorated(true);
		setTitle("Database Connection Interface");
		setResizable(false);
		desktop = new JDesktopPane();
		setContentPane(desktop);
		desktop.setBackground(Color.WHITE);
		setSize(580,640);
		setLocationRelativeTo(null);

		// Constrains
		GridBagLayout m = new GridBagLayout();
		setLayout(m);
		GridBagConstraints con;

		// Menu
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		sessionMenu = new JMenu("Session");
		sessionMenu.setMnemonic(KeyEvent.VK_S);
		connectItem = new JMenuItem("Connect to DB...");
		disconnectItem = new JMenuItem("Disconnect");
		exitItem = new JMenuItem("Exit");
		connectItem.addActionListener(this);
		disconnectItem.addActionListener(this);
		exitItem.addActionListener(this);
		sessionMenu.add(connectItem);
		sessionMenu.add(disconnectItem);		
		sessionMenu.addSeparator();
		sessionMenu.add(exitItem);

		encodingMenu = new JMenu("Encoding");
		encodingMenu.setMnemonic(KeyEvent.VK_E);
		ButtonGroup group = new ButtonGroup();
		encUTF8Item = new JRadioButtonMenuItem("Unicode UTF-8", true);
		encUTF16Item = new JRadioButtonMenuItem("Unicode UTF-16");
		group.add(encUTF8Item);
		group.add(encUTF16Item);
		encUTF8Item.addActionListener(this);
		encUTF16Item.addActionListener(this);
		encodingMenu.add(encUTF8Item);
		encodingMenu.add(encUTF16Item);
		
		menuBar.add(sessionMenu);
		menuBar.add(encodingMenu);

		// ****** Project ******

		// Project label
		projectLabel = new JLabel("Project");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(projectLabel,con);
		add(projectLabel);
		
		// Project field
		projectField = new JTextField(15);
		projectField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,5,30);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(projectField,con);
		add(projectField);
		
		// Delete project Button
		deletePButton = new JButton("Delete in DB");
		deletePButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 4;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(deletePButton,con);
		add(deletePButton);
		
		// Create / Use project button
		useProjectButton = new JButton("Use/ Create");
		useProjectButton.setPreferredSize(deletePButton.getPreferredSize());
		useProjectButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 3;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(useProjectButton,con);
		add(useProjectButton);

		// Projects label
		projectsLabel = new JLabel("Projects");
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(projectsLabel,con);
		add(projectsLabel);
		
		// Project list
		pListModel = new DefaultListModel();
		pListModel.addElement("Hit refresh");
		pList = new JList(pListModel);
		pList.setPreferredSize(new Dimension(300,200));
		pList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pList.setSelectedIndex(0);
		pList.addMouseListener(new ActionJList(pList, this));
		pList.addListSelectionListener(this);
		pList.setVisibleRowCount(5);
		pListScrollPane = new JScrollPane(pList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 2;
		con.gridwidth = 2;
		con.gridheight = 3;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(pListScrollPane,con);		
		add(pListScrollPane);
		
		// Refresh projects button
		refreshPButton = new JButton("Refresh");
		refreshPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 5;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,40,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(refreshPButton,con);
		add(refreshPButton);	
		
		// ****** ROP Components ******
		
		// ROP label
		ROPLabel = new JLabel("ROP");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 7;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(ROPLabel,con);
		add(ROPLabel);
		
		// Import button
		importButton = new JButton("Import selected");
		importButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 8;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(importButton,con);
		add(importButton);
		
		// Export button
		exportButton = new JButton("Export new ...");
		exportButton.setPreferredSize(importButton.getPreferredSize());
		exportButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 9;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(exportButton,con);
		add(exportButton);
		
		// Delete ROP button
		deleteRButton = new JButton("Delete in DB");
		deleteRButton.setPreferredSize(importButton.getPreferredSize());
		deleteRButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 10;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(deleteRButton,con);
		add(deleteRButton);

		// ROPs label
		ROPsLabel = new JLabel("ROPs");
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 7;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(ROPsLabel,con);
		add(ROPsLabel);
		
		// ROP list
		rListModel = new DefaultListModel();
		rListModel.addElement("Hit refresh");
		rList = new JList(rListModel);
		rList.setPreferredSize(new Dimension(300,200));
		rList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rList.setSelectedIndex(0);
		rList.addListSelectionListener(this);
		rList.setVisibleRowCount(5);
		rListScrollPane = new JScrollPane(rList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 8;
		con.gridwidth = 2;
		con.gridheight = 3;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(rListScrollPane,con);		
		add(rListScrollPane);
		
		// Refres ROPs button
		refreshRButton = new JButton("Refresh");
		refreshRButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 11;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(refreshRButton,con);
		add(refreshRButton);
		
		// ****** Messages ******
		
		// Messages label
		messagesLabel = new JLabel("Messages");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 13;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(messagesLabel,con);
		add(messagesLabel);
		
		// Print area		
		printArea = new JTextArea("No connection..");
		printArea.setLineWrap(true);
		printArea.setWrapStyleWord(true);
		scrollMPane = new JScrollPane(printArea);
		scrollMPane.setPreferredSize(new Dimension(515,180));
		scrollMPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		printArea.setCaretPosition(printArea.getDocument().getLength());
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 14;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(scrollMPane,con);
		add(scrollMPane);
		printArea.setBackground(Color.WHITE);
		printArea.setBorder(BorderFactory.createEtchedBorder());
		
		// Set visible
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

/*
	--< Methods >--
*/

	// Get print area
	public static JTextArea getPrintArea() {
		return printArea;
	}
	
	public static void setDBConnection(Connect dbCon) {
		dbConnect = dbCon;
	}
	
    public void setGraphContainer(SOCGraphContainer graphContainer) {
		this.graphContainer = graphContainer;
	}
	
	// Is Connected
	private boolean isConnected() {
		if (dbConnect != null)
			return true;
		else
			return false;
	}
	
	// Not connected
	private void notConnected() {
		printArea.append("\nNot connected!");
		pListModel.removeAllElements();
		rListModel.removeAllElements();
		pListModel.addElement("Hit refresh");
		rListModel.addElement("Hit refresh");
	}
	
	// Initialize values
	private void initValues() {
		if (rList.isSelectionEmpty()) {
			ROPNameID = "";
		}
		else
			ROPNameID = rListModel.getElementAt(rList.getSelectedIndex()).toString();
			if (ROPNameID.equals("Hit refresh"))
				ROPNameID = "";

		if (projectField.getText().isEmpty()) {
			projectName = "";
		}
		else
			projectName = projectField.getText();
	}
	
	public void setProject() {
		if (isConnected()) {
			initValues();
			if (projectName.isEmpty()) {
				printArea.append("\nType or select a project name!");
			}
			else {
				projectID = dbConnect.getProjectID(projectName);
				printArea.append("\nProject ID: " + projectID);
				
				Vector<String> ROPs = new Vector<String>();
				rListModel.removeAllElements();
				ROPs = dbConnect.getAllROPs(projectID);
				for (int i=0; i<ROPs.size(); i++) {
					rListModel.addElement(ROPs.elementAt(i));
				}
			}
		}
		else
			notConnected();	
	}	

/*
	--< Action performed >--
*/

	public void actionPerformed(ActionEvent e) {

		// Connect Item
		if (e.getSource() == connectItem) {
			glassPanel = (JPanel) getGlassPane();
			glassPanel.setLayout(new FlowLayout());
			connectWindow = new LoginWindow();
			connectWindow.setPreferredSize(new Dimension(300,220));
			glassPanel.add(connectWindow);
			glassPanel.setVisible(true);
		}
		
		// Disconnect Item
		else if (e.getSource() == disconnectItem) {
			dbConnect = null;
			printArea.append("\nDisconnected");
			projectID = 0;
		}
		
		// Exit Item
		else if (e.getSource() == exitItem) {
			this.dispose();
		}
		
		// UTF-8 Item
		else if (e.getActionCommand() == "Unicode UTF-8") {
			encodingName = "UTF8";
		}
		
		// UTF-16 Item
		else if (e.getActionCommand() == "Unicode UTF-16") {
			encodingName = "UTF16";
		}
		
		// ------------------------------
		
		// Use/ Create project button
		else if (e.getSource() == useProjectButton || e.getSource() == projectField) {
			setProject();
		}
		
		// Delete project Button
		else if (e.getSource() == deletePButton) {
			if (isConnected()) {
				initValues();
				if (projectName.isEmpty() || projectID == 0) {
					printArea.append("\nChoose a project!");
				}
				else {
					int okFlag = JOptionPane.showConfirmDialog(null,"Permanently delete project: " + projectName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
					if (okFlag == JOptionPane.YES_OPTION) {
						dbConnect.deleteProject(projectID);
						printArea.append("\nProject deleted..");
						projectID = 0;
					}						
				}
			}
			else
				notConnected();
		}
		
		// Refresh projects Button
		else if (e.getSource() == refreshPButton) {
			if (isConnected()) {
				Vector<String> projects = new Vector<String>();
				pListModel.removeAllElements();
				projects = dbConnect.getAllProjects();
				for (int i=0; i<projects.size(); i++) {
					pListModel.addElement(projects.elementAt(i));
				}
				projectID = 0;
			}
			else
				notConnected();
		}
		
		// Import selected Button
		else if (e.getSource() == importButton) {
			if (isConnected()) {
				initValues();
				if (projectID > 0 && !ROPNameID.isEmpty()) {
					@SuppressWarnings("unused")
					Object o = null;
					o = dbConnect.getROPXML(projectID, ROPNameID);
					
					if(graphContainer != null){
						graphContainer.insertResource(o, null);
					}
				}
				else {
					if (projectID == 0)
						printArea.append("\nChoose a project!");
					if (ROPNameID.isEmpty())						
						printArea.append("\nChoose a ROP!");
				}
			}
			else
				notConnected();
		}

		// Export new Button
		else if (e.getSource() == exportButton) {
			File xmlFile = null;
			String ROPXML = "";
			if (isConnected()) {
				initValues();
				if (projectID == 0) {
					printArea.append("\nChoose a project!");
				}
				else {
					Object o = new Object();//!!! change
					if(1 == graphContainer.getSelectedCount()){
						o = graphContainer.getSelectedResourceCell();
						if(o != null){
							o = ((ResourceCell)o).getFunction();
						}
					}
					
					try {
						dbConnect.setROPXML(projectID, o);
					}catch (Exception eENF) {
						printArea.append("\nUnsuccessful function call: " + eENF.getMessage());
					}
				}
			}
			else
				notConnected();
		}
		
		// Delete ROP Button
		else if (e.getSource() == deleteRButton) {
			if (isConnected()) {
				initValues();
				if (projectID > 0 && !ROPNameID.isEmpty()) {
					int okFlag = JOptionPane.showConfirmDialog(null,"Permanently delete ROP: " + ROPNameID + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
					if (okFlag == JOptionPane.YES_OPTION) {
						dbConnect.deleteROP(projectID, ROPNameID);
						printArea.append("\nROP deleted..");
					}
				}
				else {
					if (projectID == 0)
						printArea.append("\nChoose a project!");
					if (ROPNameID.isEmpty())
						printArea.append("\nChoose a ROP!");
				}
			}
			else
				notConnected();
		}

		// Refresh ROPs Button
		else if (e.getSource() == refreshRButton) {
			if (isConnected()) {
				if (projectID == 0) {
					printArea.append("\nNo project selected!");
				}
				else {
					Vector<String> ROPs = new Vector<String>();
					rListModel.removeAllElements();
					ROPs = dbConnect.getAllROPs(projectID);
					for (int i=0; i<ROPs.size(); i++) {
						rListModel.addElement(ROPs.elementAt(i));
					}
				}
			}
			else
				notConnected();
		}
	}
	
/*
	--< Value changed >--
*/

	public void valueChanged(ListSelectionEvent e) {
		JList lst = (JList) e.getSource();
		ListModel dlm = lst.getModel();
		if (lst.getValueIsAdjusting() == true) {
			if (e.getSource() == pList) {
				pList.setSelectedIndex(lst.getSelectedIndex());
				projectField.setText(dlm.getElementAt(lst.getSelectedIndex()).toString());
			}
			else if (e.getSource() == rList) {
				rList.setSelectedIndex(lst.getSelectedIndex());
			}
		}
	}
	
/*
	--< Main >--
*/

	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run(){
					new DBInterface();
            }
        });
    }
}
/*
	--< ActionJList >--
*/

class ActionJList extends MouseAdapter{
	protected JList list;
	protected DBInterface dbi;
    
	public ActionJList(JList l, DBInterface dbi){
	  list = l;
	  this.dbi = dbi;
	}
    
	public void mouseClicked(MouseEvent e){
		if(e.getClickCount() == 2){
			dbi.setProject();
		}
	}
}