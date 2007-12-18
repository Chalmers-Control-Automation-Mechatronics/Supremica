package org.supremica.external.processeditor.tools.db;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;

/**
 * This class is used to create and display a user interface with a
 * database on MS SQL Server 2005
 */
public class DBInterface extends JFrame implements ActionListener, ListSelectionListener {

/*
	------------------
	--< Initialize >--
	------------------
*/
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktop;
	private JPanel glassPanel;
	private JPanel settingsPane;
	private JMenuBar menuBar;
	private JMenu sessionMenu;
	private JMenu optionsMenu;
	private JMenu runMenu;
	private JMenu encodingMenu;
	private JMenu dataTypeMenu;
	private JMenuItem connectItem, disconnectItem, exitItem,
							encUTF8Item, encUTF16Item, SOCItem, fileItem,
								queryItem, linkPItem;
	private JLabel pathLabel;
	private JLabel transferTypeLabel;
	private JLabel encodingLabel;
	private JLabel projectLabel;
	private JLabel projectsLabel;
	private JLabel chkStdLabel;
	private JLabel standardsLabel;
	private JLabel messagesLabel;
	private JScrollPane pListScrollPane;
	private JScrollPane sListScrollPane;
	private JScrollPane scrollMPane;
	private JTextField projectField;
	private JList pList;
	private JList sList;
	private DefaultListModel pListModel;
	private DefaultListModel sListModel;
	private JButton pathButton;
	private JButton useProjectButton;
	private JButton importPButton;
	private JButton exportPButton;
	private JButton deletePButton;
	private JButton refreshPButton;
	private JButton importButton;
	private JButton exportButton;
	private JButton deleteSButton;
	private JButton refreshSButton;
	private JButton clearButton;
	private static JTextArea printArea;

	private final String[] standardsAbbr = { "PR", "VR", "ROP", "EOP", "IL" };
	private final String[] standardsFull = {"Physical resources (Cell name ID)", "Virtual resources (Cell name ID)",
			"Relations of Operations (ROP name ID)", "Execution of Operations (EOP name ID)", "Interlocks (IL name ID)"};
	private JComboBox standardList = null;
	private String standardString = "Relations of Operations (ROP name ID)";

	private LoginWindow loginWindow = null;
	private static Connect dbConnect = null;		// Connection setup object
	private SOCGraphContainer graphContainer = null;

	@SuppressWarnings("unused")
	private String encodingName = "UTF-8";
	@SuppressWarnings("unused")
	private String transferTypeName = "SOC object";

	public int projectID = 0;
	private String projectName = "";
	private String standardName = "";
	private File[] xmlFiles = null;
	private File dir = null;
	private String outputPath = "C:\\tmp";

/*
	-------------------
	--< Constructor >--
	-------------------
*/
	/**
	 * Creates and displays a predefined graphical user interface
	 */
	public DBInterface() {
		setDefaultLookAndFeelDecorated(true);
		setTitle("Database Connection Interface");
		setResizable(false);
		desktop = new JDesktopPane();
		setContentPane(desktop);
		desktop.setBackground(Color.WHITE);
		setSize(570,780);
		setLocationRelativeTo(null);

		// Constrains
		GridBagLayout m = new GridBagLayout();
		setLayout(m);
		GridBagConstraints con;

		// Menu
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Session menu
		sessionMenu = new JMenu("Session ");
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

		// Options menu
		optionsMenu = new JMenu("Options ");
		optionsMenu.setMnemonic(KeyEvent.VK_O);

		dataTypeMenu = new JMenu("Transfer type ");
		ButtonGroup dataTypeGroup = new ButtonGroup();
		SOCItem = new JRadioButtonMenuItem("To/from SOC", true);
		fileItem = new JRadioButtonMenuItem("To/from file");
		SOCItem.addActionListener(this);
		fileItem.addActionListener(this);
		dataTypeGroup.add(SOCItem);
		dataTypeGroup.add(fileItem);
		dataTypeMenu.add(SOCItem);
		dataTypeMenu.add(fileItem);

		encodingMenu = new JMenu("File encoding ");
		ButtonGroup encodingGroup = new ButtonGroup();
		encUTF8Item = new JRadioButtonMenuItem("UTF-8", true);
		encUTF16Item = new JRadioButtonMenuItem("UTF-16");
		encUTF8Item.addActionListener(this);
		encUTF16Item.addActionListener(this);
		encodingGroup.add(encUTF8Item);
		encodingGroup.add(encUTF16Item);
		encodingMenu.add(encUTF8Item);
		encodingMenu.add(encUTF16Item);

		optionsMenu.add(dataTypeMenu);
		optionsMenu.add(encodingMenu);
		menuBar.add(sessionMenu);
		menuBar.add(optionsMenu);

		// Run Menu
		runMenu = new JMenu("Run    ");
		runMenu.setMnemonic(KeyEvent.VK_R);
		queryItem = new JMenuItem("DB query...");
		linkPItem = new JMenuItem("Link project ");
		queryItem.addActionListener(this);
		linkPItem.addActionListener(this);
		runMenu.add(queryItem);
		runMenu.add(linkPItem);
		menuBar.add(runMenu);


		// ****** Settings panel ******

		// Settings panel
		GridBagLayout m2 = new GridBagLayout();
		settingsPane = new JPanel(m2);
		settingsPane.setPreferredSize(new Dimension(540,30));
		settingsPane.setMinimumSize(settingsPane.getPreferredSize());
		settingsPane.setMaximumSize(settingsPane.getPreferredSize());
		settingsPane.setBackground(Color.WHITE);
		settingsPane.setBorder(BorderFactory.createEtchedBorder());
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.anchor = GridBagConstraints.WEST;
		con.fill = GridBagConstraints.BOTH;
		con.insets = new Insets(15,0,0,0);
		m.setConstraints(settingsPane,con);
		add(settingsPane);

		// Path button
		pathButton = new JButton("Set path ");
		pathButton.setFont(new Font("Arial", Font.PLAIN,11));
		pathButton.setPreferredSize(new Dimension(77,20));
		pathButton.setMinimumSize(pathButton.getPreferredSize());
		pathButton.setMaximumSize(pathButton.getPreferredSize());
		pathButton.addActionListener(this);
		pathButton.setEnabled(false);
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(4,5,5,5);
		con.anchor = GridBagConstraints.WEST;
		m2.setConstraints(pathButton,con);
		settingsPane.add(pathButton);

		// Path label
		pathLabel = new JLabel(outputPath);
		pathLabel.setForeground(Color.LIGHT_GRAY);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 1;
		pathLabel.setPreferredSize(new Dimension(280,16));
		pathLabel.setMinimumSize(pathLabel.getPreferredSize());
		pathLabel.setMaximumSize(pathLabel.getPreferredSize());
		con.insets = new Insets(0,10,0,0);
		con.anchor = GridBagConstraints.WEST;
		m2.setConstraints(pathLabel,con);
		settingsPane.add(pathLabel);

		// Transfer type label
		transferTypeLabel = new JLabel(transferTypeName);
		transferTypeLabel.setForeground(Color.BLACK);
		con = new GridBagConstraints();
		con.gridx = 3;
		con.gridy = 1;
		transferTypeLabel.setPreferredSize(new Dimension(80,16));
		transferTypeLabel.setMinimumSize(transferTypeLabel.getPreferredSize());
		transferTypeLabel.setMaximumSize(transferTypeLabel.getPreferredSize());
		con.insets = new Insets(0,20,0,10);
		m2.setConstraints(transferTypeLabel,con);
		settingsPane.add(transferTypeLabel);

		// Encoding type label
		encodingLabel = new JLabel(encodingName);
		encodingLabel.setForeground(Color.LIGHT_GRAY);
		con = new GridBagConstraints();
		con.gridx = 4;
		con.gridy = 1;
		encodingLabel.setPreferredSize(new Dimension(40,16));
		encodingLabel.setMinimumSize(encodingLabel.getPreferredSize());
		encodingLabel.setMaximumSize(encodingLabel.getPreferredSize());
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.anchor = GridBagConstraints.EAST;
		con.fill = GridBagConstraints.REMAINDER;
		con.insets = new Insets(0,0,0,10);
		m2.setConstraints(encodingLabel,con);
		settingsPane.add(encodingLabel);


		// ****** Project components ******

		// Project label
		setLayout(m);
		projectLabel = new JLabel("Project");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.gridwidth = 1;
		con.insets = new Insets(10,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(projectLabel,con);
		add(projectLabel);

		// Project field
		projectField = new JTextField(15);
		projectField.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 3;
		con.gridwidth = 1;
		con.ipadx = 160;
		con.insets = new Insets(0,0,7,30);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(projectField,con);
		add(projectField);

		// Create / Use project button
		useProjectButton = new JButton("Create/ use");
		useProjectButton.setMinimumSize(new Dimension(130,26));
		useProjectButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 4;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,7,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(useProjectButton,con);
		add(useProjectButton);

		// Import project Button
		importPButton = new JButton("Import from DB");
		importPButton.setMinimumSize(new Dimension(130,26));
		importPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 5;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,7,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(importPButton,con);
		add(importPButton);

		// Export project Button
		exportPButton = new JButton("Export to DB");
		exportPButton.setMinimumSize(new Dimension(130,26));
		exportPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 6;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,7,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(exportPButton,con);
		add(exportPButton);

		// Delete project Button
		deletePButton = new JButton("Delete in DB");
		deletePButton.setMinimumSize(new Dimension(130,26));
		deletePButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 7;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(deletePButton,con);
		add(deletePButton);

		// Projects label
		projectsLabel = new JLabel("Projects");
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 2;
		con.gridwidth = 1;
		con.insets = new Insets(10,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(projectsLabel,con);
		add(projectsLabel);

		// Project list
		pListModel = new DefaultListModel();
		pListModel.addElement("No projects");
		pList = new JList(pListModel);
		pList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pList.setSelectedIndex(0);
		pList.addListSelectionListener(this);
		pList.setVisibleRowCount(5);
		pListScrollPane = new JScrollPane(pList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 3;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.gridheight = 4;
		con.fill = GridBagConstraints.BOTH;
		con.ipadx = 300;
		con.ipady = 100;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(pListScrollPane,con);
		add(pListScrollPane);

		// Refresh projects button
		refreshPButton = new JButton("Refresh");
		refreshPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 7;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,30,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(refreshPButton,con);
		add(refreshPButton);

		// Check standards label
		chkStdLabel = new JLabel("");
		con = new GridBagConstraints();
		con.gridx = 3;
		con.gridy = 7;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,40,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(chkStdLabel,con);
		add(chkStdLabel);

		// ****** Standards components ******

		// Standard label
		standardList = new JComboBox(standardsAbbr);
		standardList.setSelectedIndex(2);
		standardList.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 9;
		con.gridwidth = 1;
		con.ipadx = 30;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(standardList,con);
		add(standardList);

		// Import button
		importButton = new JButton("Import from DB");
		importButton.setMinimumSize(new Dimension(130,26));
		importButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 10;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(importButton,con);
		add(importButton);

		// Export button
		exportButton = new JButton("Export to DB");
		exportButton.setMinimumSize(new Dimension(130,26));
		exportButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 11;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(exportButton,con);
		add(exportButton);

		// Delete standards button
		deleteSButton = new JButton("Delete in DB");
		deleteSButton.setMinimumSize(new Dimension(130,26));
		deleteSButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 12;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(deleteSButton,con);
		add(deleteSButton);

		// Standards label
		standardsLabel = new JLabel(standardString);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 9;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(standardsLabel,con);
		add(standardsLabel);

		// Standards list
		sListModel = new DefaultListModel();
		sListModel.addElement("Select a project");
		sList = new JList(sListModel);
		sList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sList.setSelectedIndex(0);
		sList.addListSelectionListener(this);
		sList.setVisibleRowCount(5);
		sListScrollPane = new JScrollPane(sList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 10;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.gridheight = 3;
		con.fill = GridBagConstraints.BOTH;
		con.ipadx = 300;
		con.ipady = 118;
		con.insets = new Insets(0,0,5,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(sListScrollPane,con);
		add(sListScrollPane);

		// Refresh standards button
		refreshSButton = new JButton("Refresh");
		refreshSButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 13;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(refreshSButton,con);
		add(refreshSButton);

		// ****** Messages ******

		// Messages label
		messagesLabel = new JLabel("Messages");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 15;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(messagesLabel,con);
		add(messagesLabel);

		// Clear button
		clearButton = new JButton("clear");
		clearButton.setFont(new Font("Arial", Font.BOLD, 10));
		clearButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 3;
		con.gridy = 15;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.EAST;
		m.setConstraints(clearButton,con);
		add(clearButton);

		// Print area
		printArea = new JTextArea("No connection..");
		printArea.setLineWrap(true);
		printArea.setWrapStyleWord(true);
		scrollMPane = new JScrollPane(printArea);
		scrollMPane.setPreferredSize(new Dimension(545,180));
		scrollMPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		printArea.setCaretPosition(printArea.getDocument().getLength());
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 16;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.fill = GridBagConstraints.BOTH;
		con.ipadx = 380;
		con.ipady = 200;
		con.insets = new Insets(0,0,25,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(scrollMPane,con);
		add(scrollMPane);
		printArea.setBackground(Color.WHITE);
		printArea.setBorder(BorderFactory.createEtchedBorder());

		// Set visible
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

/*
	---------------
	--< Methods >--
	---------------
*/
	// Get print area
	/**
	 * Returns the printArea object in the user interface
	 * @return		the printArea object
	 */
	public static JTextArea getPrintArea() {
		return printArea;
	}

	// Clear print area
	/**
	 * Clears the printArea object in the user interface
	 */
	public static void clearPrintArea() {
		printArea.setText("");
	}

	// Set DB connection
	/**
	 * Sets the dbConnect object, not an instance method
	 * @param	dbCon the database connection setup object
	 */
	public static void setDBConnection(Connect dbCon) {
		dbConnect = dbCon;
	}

	// Set Graph Container
    public void setGraphContainer(SOCGraphContainer graphContainer) {
		this.graphContainer = graphContainer;
	}

	// Is connected
	/**
	 * Returns true if a connection object is defined, otherwise
	 * false
	 * @return		true if a connection object is defined
	 */
	private boolean isConnected() {
		if (dbConnect != null) {
			if (dbConnect.isConnected()) {
				return true;
			}
		}
		return false;
	}

	// Not connected
	/**
	 * Updates the user interface information if no connection exists
	 */
	private void notConnected() {
		printArea.append("\nNot connected!");
		pListModel.removeAllElements();
		sListModel.removeAllElements();
		pListModel.addElement("No projects");
		sListModel.addElement("Select a project");
		chkStdLabel.setText("");
	}

	// Init values
	/**
	 * Initializes a number of variables used to send queries to the database
	 */
	private void initValues() {
		printArea.setCaretPosition(printArea.getDocument().getLength());
		if (sList.isSelectionEmpty()) {
			standardName = "";
		}
		else
			standardName = sListModel.getElementAt(sList.getSelectedIndex()).toString();
			if (standardName.equals("Select a project"))
				standardName = "";

		if (projectField.getText().isEmpty()) {
			projectName = "";
		}
		else
			projectName = projectField.getText();
	}
	// Get standard names
	/**
	 * Get standard names
	 */
	private ArrayList<String> getStandardNames() {
		ArrayList<String> standardNames = new ArrayList<String>();
		int[] indices = sList.getSelectedIndices();
		for (int i=0; i<indices.length; i++) {
			standardNames.add((String)sListModel.getElementAt(indices[i]));
		}
		return standardNames;
	}

	// Set standards in use
	/**
	 * Updates the user interface information with what type of standards
	 * that are present in different projects
	 */
	private void setStandardsInUse() {
		if (isConnected()) {
			setProject();
			int count = 0;
			ArrayList<Integer> stdInUse = dbConnect.getStandardsInUse(projectID);
			ArrayList<String> stdToDisplay = new ArrayList<String>();
			if (stdInUse.get(0) == 1) {
				stdToDisplay.add(" PR");
				count = count + 1;
			}
			if (stdInUse.get(1) == 1) {
				stdToDisplay.add(" VR");
				count = count + 1;
			}
			if (stdInUse.get(2) == 1) {
				stdToDisplay.add(" ROP");
				count = count + 1;
			}
			if (stdInUse.get(3) == 1) {
				stdToDisplay.add(" EOP");
				count = count + 1;
			}
			if (stdInUse.get(4) == 1) {
				stdToDisplay.add(" IL");
				count = count + 1;
			}
			switch(count){
			case 0: stdToDisplay.removeAll(stdToDisplay); stdToDisplay.add("Empty"); break;
			case 5: stdToDisplay.removeAll(stdToDisplay); stdToDisplay.add("All"); break;
			}
			chkStdLabel.setText(stdToDisplay.toString());
		}
	}

	// Refresh projects
	/**
	 * Updates the user interface with the latest projects in the database
	 */
	public void refreshProjects() {
		if (isConnected()) {
			initValues();
			Vector<String> projects = new Vector<String>();
			pListModel.removeAllElements();
			projects = dbConnect.getAllProjects();
			Collections.sort(projects, String.CASE_INSENSITIVE_ORDER);
			for (int i=0; i<projects.size(); i++) {
				pListModel.addElement(projects.elementAt(i));
			}
			sListModel.removeAllElements();
			sListModel.addElement("Select a project");
			sList.setSelectedIndex(0);
			chkStdLabel.setText("");
			projectID = 0;
		}
		else
			notConnected();
	}

	// Set project
	/**
	 * Sets the project database ID by using the project name
	 */
	public void setProject() {
		if (isConnected()) {
			initValues();
			if (projectName.isEmpty()) {
				printArea.append("\nType or select a project name!");
			}
			else {
				projectID = dbConnect.getProjectID(projectName);
				ArrayList<String> list = new ArrayList<String>();
				sListModel.removeAllElements();
				int index = standardList.getSelectedIndex();
				list = dbConnect.getAllStandards(projectID, index);
				Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
				for (int i=0; i<list.size(); i++) {
					sListModel.addElement(list.get(i));
				}
				sList.ensureIndexIsVisible(sListModel.size());
			}
		}
		else
			notConnected();
	}

/*
	------------------------
	--< Action performed >--
	------------------------
*/
	public void actionPerformed(ActionEvent e) {

		// Connect Item
		if (e.getSource() == connectItem) {
			glassPanel = (JPanel) getGlassPane();
			glassPanel.setLayout(new FlowLayout());
			loginWindow = new LoginWindow(this);
			loginWindow.setPreferredSize(new Dimension(300,220));
			glassPanel.add(loginWindow);
			glassPanel.setVisible(true);
		}

		// Disconnect Item
		else if (e.getSource() == disconnectItem) {
			dbConnect = null;
			projectID = 0;
			notConnected();
			printArea.append("\nDisconnected");
		}

		// Exit Item
		else if (e.getSource() == exitItem) {
			this.dispose();
		}

		// UTF-8 Item
		else if (e.getSource() == encUTF8Item) {
			encodingName = "UTF-8";
			encodingLabel.setText(encodingName);
		}

		// UTF-16 Item
		else if (e.getSource() == encUTF16Item) {
			encodingName = "UTF-16";
			encodingLabel.setText(encodingName);
		}

		// SOC type Item
		else if (e.getSource() == SOCItem) {
			transferTypeName = "SOC object";
			transferTypeLabel.setText(transferTypeName);
			pathButton.setEnabled(false);
			pathLabel.setForeground(Color.LIGHT_GRAY);
			encodingLabel.setForeground(Color.LIGHT_GRAY);
		}

		// File type Item
		else if (e.getSource() == fileItem) {
			transferTypeName = "XML file";
			transferTypeLabel.setText(transferTypeName);
			pathLabel.setForeground(Color.BLACK);
			encodingLabel.setForeground(Color.BLACK);
			pathButton.setEnabled(true);
		}

		// Query Item
		else if (e.getSource() == queryItem) {
			if (isConnected()) {
				int answer = JOptionPane.showConfirmDialog(null,"This command has not yet been defined,\nRun the example query below?\n\nSELECT Machine_ID, Machine_name\nFROM PR_machines\n", "DB query", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {
					printArea.append("\nResultSet for example query:\n");
					dbConnect.sendQuery("select Machine_ID, Machine_name from PR_machines");
				}
			}
			else
				notConnected();
		}

		// Link project Item
		else if (e.getSource() == linkPItem) {
			if (isConnected()) {
				if (!projectField.getText().isEmpty()) {
					int inputValue = JOptionPane.showConfirmDialog(this, "Use current file path to create log-file?", "Output path", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (inputValue == JOptionPane.OK_OPTION) {
						String resultSet = dbConnect.linkProject(projectID);
						File logFile = new File (outputPath  + "\\" + "log_" + projectName + ".txt" );
						try {
							Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
							w.write(resultSet);
							w.close();
							printArea.append("\nLog file created: " + logFile.toString());
						}catch (IOException ioe) {
							printArea.append("\nIOException: " + ioe.getMessage());
						}
					}
				}
				else {
					if (projectID == 0)
						printArea.append("\nChoose a project!");
				}
			}
			else
				notConnected();
		}

		// ------------------------------

		// Path button
		else if (e.getSource() == pathButton) {
			Object inputValue = JOptionPane.showInputDialog(this, "Select output path", "Output path", JOptionPane.INFORMATION_MESSAGE, null, null, outputPath);
			if (inputValue != null) {
				outputPath = inputValue.toString();
				pathLabel.setText(outputPath);
			}
		}


		// Create/ use project button
		else if (e.getSource() == useProjectButton || e.getSource() == projectField) {
			if (isConnected()) {
				if (!projectField.getText().isEmpty()) {
					setProject();
					refreshProjects();
					setProject();
					int index = pList.getNextMatch(projectName, 0, Position.Bias.Forward);
					if (index > -1) {
						if (projectName.equalsIgnoreCase((String)pListModel.get(index))) {
							projectID = dbConnect.getProjectID(projectName);
							pList.setSelectedIndex(index);
							pList.ensureIndexIsVisible(index);
							setStandardsInUse();
						}
					}
				}
			}
			else
				notConnected();
		}

		// Import project Button
		else if (e.getSource() == importPButton) {
			if (isConnected()) {
				initValues();
				if (projectName.isEmpty()) {
					printArea.append("\nChoose a project!");
				}
				else {
					if (transferTypeName.equals("XML file")){
						int inputValue = JOptionPane.showConfirmDialog(this, "Use current file path?", "Output path", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if (inputValue == JOptionPane.OK_OPTION) {
							String xmlStr = dbConnect.getProjectXMLAsString(projectName);
							File outputFile = new File(outputPath);
							outputFile = new File (outputPath  + "\\" + "Project_" + projectName + ".xml" );
							try {
								Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encodingName));
								w.write(xmlStr);
								w.close();
								printArea.append("\nFile created: " + outputFile.toString());
							}catch (IOException ioe) {
								printArea.append("\nIOException: " + ioe.getMessage());
							}
						}
						else
							printArea.append("\nImport command cancelled by user ");
					}
					else
						printArea.append("\nCannot import Project as a SOC object!\nChange transfer type setting");
				}
			}
			else
				notConnected();
		}

		// Export project Button
		else if (e.getSource() == exportPButton) {
			if (isConnected()) {
				initValues();
				if (transferTypeName.equals("XML file")) {
				    JFileChooser fc = new JFileChooser(dir);
				    fc.setMultiSelectionEnabled(true);
				    FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        "XML files", "xml");
				    fc.setFileFilter(filter);
				    int returnVal = fc.showOpenDialog(glassPanel);
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
						xmlFiles = fc.getSelectedFiles();
						if (xmlFiles.length > 0) {
							dir = xmlFiles[0];
						}
						for(int i=0; i<xmlFiles.length; i++) {
							String xmlStr = "";
							try {
								// Set Reader object to selected encoding
								Reader r = new InputStreamReader(new FileInputStream(xmlFiles[i]), encodingName);
								Scanner scanner = new Scanner(r);
								while (scanner.hasNextLine()){
									String tempStr = scanner.nextLine();
									xmlStr = xmlStr + tempStr.trim();
								}
								scanner.close();
							}catch (IOException ex){
								printArea.append("\nIOException: " + ex.getMessage());
							}
							dbConnect.setProjectXMLFromString(xmlStr);
							refreshProjects();
						}
					}
					else
						printArea.append("\nExport command cancelled by user");
				}
				else
					printArea.append("\nCannot export Project as a SOC object!\nChange transfer type setting");

			}
			else
				notConnected();
		}

		// Delete project Button
		else if (e.getSource() == deletePButton) {
			if (isConnected()) {
				initValues();
				projectID = dbConnect.getProjectID(projectName);
				if (projectName.isEmpty() || projectID == 0) {
					printArea.append("\nChoose a project!");
				}
				else {
					int okFlag = JOptionPane.showConfirmDialog(this,"Permanently delete project: " + projectName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
					if (okFlag == JOptionPane.YES_OPTION) {
						dbConnect.deleteProject(projectID);
						printArea.append("\nProject deleted..");
						projectID = 0;
						refreshProjects();
					}
				}
			}
			else
				notConnected();
		}

		// Refresh projects Button
		else if (e.getSource() == refreshPButton) {
			refreshProjects();
		}

		// ------------------------------

		// Standard List
		else if (e.getSource() == standardList) {
			standardsLabel.setText(standardsFull[standardList.getSelectedIndex()]);
			setProject();
			setStandardsInUse();
		}

		// Import selected Button
		else if (e.getSource() == importButton) {
			if (isConnected()) {
				initValues();
				if (projectID > 0 && !sList.isSelectionEmpty()) {
					int index = standardList.getSelectedIndex();
					ArrayList<String> standardNames = getStandardNames();
					if (transferTypeName.equals("SOC object")) {
						for (int i=0; i<standardNames.size(); i++){
							@SuppressWarnings("unused")
							Object o = dbConnect.getStandardXMLAsObject(projectID, index, standardNames.get(i));
							if(graphContainer != null){
								graphContainer.insertResource(o, null);
							}
							printArea.append("\nObject sent to SOC");
						}
					}
					else if (transferTypeName.equals("XML file")){
						int inputValue = JOptionPane.showConfirmDialog(this, "Use current file path?", "Output path", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if (inputValue == JOptionPane.OK_OPTION) {
							for (int i=0; i<standardNames.size(); i++){
								String xmlStr = dbConnect.getStandardXMLAsString(projectID, index, standardNames.get(i));
								File outputFile = new File(outputPath);
								outputFile.mkdirs();
								outputFile = new File (outputPath  + "\\" + standardsAbbr[index] + standardNames.get(i) + ".xml" );
								try {
									Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encodingName));
									w.write(xmlStr);
									w.close();
									printArea.append("\nFile created: " + outputFile.toString());
								}catch (IOException ioe) {
									printArea.append("\nIOException: " + ioe.getMessage());
								}
							}
						}
						else
							printArea.append("\nImport command cancelled by user ");
					}
				}
				else {
					if (projectID == 0)
						printArea.append("\nChoose a project!");
					if (standardName.isEmpty())
						printArea.append("\nSelect " + standardsAbbr[standardList.getSelectedIndex()] + " in list!");
				}
			}
			else
				notConnected();
		}

		// Export standard(s) to DB Button
		else if (e.getSource() == exportButton) {
			if (isConnected()) {
				initValues();
				if (projectID == 0) {
					printArea.append("\nChoose a project!");
				}
				else {
					if (transferTypeName.equals("XML file")) {
					    JFileChooser fc = new JFileChooser(dir);
					    fc.setMultiSelectionEnabled(true);
					    FileNameExtensionFilter filter = new FileNameExtensionFilter(
					        "XML files", "xml");
					    fc.setFileFilter(filter);
					    int returnVal = fc.showOpenDialog(glassPanel);
					    if(returnVal == JFileChooser.APPROVE_OPTION) {
							xmlFiles = fc.getSelectedFiles();
							if (xmlFiles.length > 0) {
								dir = xmlFiles[0];
							}
							for(int i=0; i<xmlFiles.length; i++) {
								String xmlStr = "";
								try {
									// Set Reader object to selected encoding
									Reader r = new InputStreamReader(new FileInputStream(xmlFiles[i]), encodingName);
									Scanner scanner = new Scanner(r);
									while (scanner.hasNextLine()){
										String tempStr = scanner.nextLine();
										xmlStr = xmlStr + tempStr.trim();
									}
									scanner.close();
								}catch (IOException ex){
									printArea.append("\nIOException: " + ex.getMessage());
								}
								int standardIndex = standardList.getSelectedIndex();
								dbConnect.setStandardXMLFromString(projectID, standardIndex, xmlStr);
							}
						}
						else
							printArea.append("\nExport command cancelled by user");
					}
					else if (transferTypeName.equals("SOC object")) {
						Object o = new Object();
						if(1 == graphContainer.getSelectedCount()){
							o = graphContainer.getSelectedResourceCell();
							if(o != null){
								o = ((ResourceCell)o).getFunction();
							}
						}
						int standardIndex = standardList.getSelectedIndex();
						dbConnect.setStandardXMLFromObject(projectID, standardIndex, o);
					}
					setProject();
				}
			}
			else
				notConnected();
		}

		// Delete standard Button
		else if (e.getSource() == deleteSButton) {
			if (isConnected()) {
				initValues();
				if (projectID > 0 && !sList.isSelectionEmpty()) {
					int index = standardList.getSelectedIndex();
					ArrayList<String> standardNames = getStandardNames();
					int okFlag = JOptionPane.showConfirmDialog(this,"Permanently delete selection?", "Confirm delete", JOptionPane.YES_NO_OPTION);
					if (okFlag == JOptionPane.YES_OPTION) {
						for (int i=0; i<standardNames.size(); i++){
							dbConnect.deleteStandard(projectID, index, standardNames.get(i));
						}
						setProject();
					}
				}
				else {
					if (projectID == 0)
						printArea.append("\nChoose a project!");
					if (standardName.isEmpty())
						printArea.append("\nSelect " + standardsAbbr[standardList.getSelectedIndex()] + " in list!");
				}
			}
			else
				notConnected();
		}

		// Refresh standards button
		else if (e.getSource() == refreshSButton) {
			if (isConnected()) {
				if (projectID == 0) {
					printArea.append("\nNo project selected!");
				}
				else {
					ArrayList<String> list = new ArrayList<String>();
					sListModel.removeAllElements();
					int index = standardList.getSelectedIndex();
					list = dbConnect.getAllStandards(projectID, index);
					Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
					for (int i=0; i<list.size(); i++) {
						sListModel.addElement(list.get(i));
					}
				}
			}
			else
				notConnected();
		}

		// Clear button
		else if (e.getSource() == clearButton) {
			printArea.setText("");
		}
	}

/*
	---------------------
	--< Value changed >--
	---------------------
*/
	public void valueChanged(ListSelectionEvent e) {
		JList lst = (JList) e.getSource();
		ListModel dlm = lst.getModel();
		if (lst.getValueIsAdjusting() == true) {
			if (e.getSource() == pList) {
				projectField.setText(dlm.getElementAt(lst.getSelectedIndex()).toString());
				setStandardsInUse();
			}

		}
	}

/*
	------------
	--< Main >--
	------------
*/
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run(){
					new DBInterface();
            }
        });
    }
}