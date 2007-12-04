package org.supremica.external.processeditor.tools.db;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;

public class DBInterface extends JFrame implements ActionListener, ListSelectionListener {

/*
	------------------
	--< Initialize >--
	------------------
*/
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktop;
	private JPanel glassPanel;
	private JMenuBar menuBar;
	private JMenu sessionMenu;
	private JMenu optionsMenu;
	private JMenu encodingMenu;
	private JMenu dataTypeMenu;
	private JMenuItem connectItem, disconnectItem, exitItem,
							encUTF8Item, encUTF16Item, SOCItem, fileItem;
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
	private String dataTypeName = "XML file";

	public int projectID = 0;
	private String projectName = "";
	private String standardName = "";
	private File xmlFile = null;
	private String outputPath = "c:\\tmp";
		
/*
	-------------------
	--< Constructor >--
	-------------------
*/
	public DBInterface() {
		setDefaultLookAndFeelDecorated(true);
		setTitle("Database Connection Interface");
		setResizable(false);
		desktop = new JDesktopPane();
		setContentPane(desktop);
		desktop.setBackground(Color.WHITE);
		setSize(570,750);
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
		encodingMenu = new JMenu("File encoding ");
		ButtonGroup dataTypeGroup = new ButtonGroup();
		SOCItem = new JRadioButtonMenuItem("To/from SOC");
		fileItem = new JRadioButtonMenuItem("To/from file", true);
		dataTypeGroup.add(SOCItem);
		dataTypeGroup.add(fileItem);
		ButtonGroup encodingGroup = new ButtonGroup();
		encUTF8Item = new JRadioButtonMenuItem("Unicode UTF-8", true);
		encUTF16Item = new JRadioButtonMenuItem("Unicode UTF-16");
		encodingGroup.add(encUTF8Item);
		encodingGroup.add(encUTF16Item);
		dataTypeMenu.add(SOCItem);
		dataTypeMenu.add(fileItem);
		encodingMenu.add(encUTF8Item);
		encodingMenu.add(encUTF16Item);
		SOCItem.addActionListener(this);
		fileItem.addActionListener(this);
		encUTF8Item.addActionListener(this);
		encUTF16Item.addActionListener(this);
		optionsMenu.add(dataTypeMenu);
		optionsMenu.add(encodingMenu);
		menuBar.add(sessionMenu);
		menuBar.add(optionsMenu);

		// ****** Project components ******

		// Project label
		projectLabel = new JLabel("Project");
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(20,0,10,0);
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
		con.gridy = 3;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,7,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(useProjectButton,con);
		add(useProjectButton);
		
		// Import project Button
		importPButton = new JButton("Import from DB");
		importPButton.setMinimumSize(new Dimension(130,26));
		importPButton.setEnabled(false);
		importPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 4;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,7,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(importPButton,con);
		add(importPButton);
		
		// Export project Button
		exportPButton = new JButton("Export to DB");
		exportPButton.setMinimumSize(new Dimension(130,26));
		exportPButton.setEnabled(false);
		exportPButton.addActionListener(this);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 5;
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
		con.gridy = 6;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,0,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(deletePButton,con);
		add(deletePButton);
		
		// Projects label
		projectsLabel = new JLabel("Projects");
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 1;
		con.gridwidth = 1;
		con.insets = new Insets(20,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(projectsLabel,con);
		add(projectsLabel);
		
		// Project list
		pListModel = new DefaultListModel();
		pListModel.addElement("No projects");
		pList = new JList(pListModel);
		pList.setPreferredSize(new Dimension(300,200));
		pList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pList.setSelectedIndex(0);
		pList.addMouseListener(new ActionJList(pList));
		pList.addListSelectionListener(this);
		pList.setVisibleRowCount(5);
		pListScrollPane = new JScrollPane(pList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 2;
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
		con.gridy = 6;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,30,0);
		con.anchor = GridBagConstraints.WEST;
		m.setConstraints(refreshPButton,con);
		add(refreshPButton);
		
		// Check standards label
		chkStdLabel = new JLabel("");
		con = new GridBagConstraints();
		con.gridx = 3;
		con.gridy = 6;
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
		con.gridy = 8;
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
		con.gridy = 9;
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
		con.gridy = 10;
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
		con.gridy = 11;
		con.gridwidth = 1;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.NORTHWEST;
		m.setConstraints(deleteSButton,con);
		add(deleteSButton);

		// Standards label
		standardsLabel = new JLabel(standardString);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 8;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.insets = new Insets(0,0,10,0);
		con.anchor = GridBagConstraints.WEST;		
		m.setConstraints(standardsLabel,con);
		add(standardsLabel);
		
		// Standards list
		sListModel = new DefaultListModel();
		sListModel.addElement("Select a project");
		sList = new JList(sListModel);
		sList.setPreferredSize(new Dimension(300,200));
		sList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sList.setSelectedIndex(0);
		sList.addListSelectionListener(this);
		sList.setVisibleRowCount(5);
		sListScrollPane = new JScrollPane(sList);
		con = new GridBagConstraints();
		con.gridx = 2;
		con.gridy = 9;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.gridheight = 3;
		con.fill = GridBagConstraints.BOTH;
		con.ipadx = 300; // or whatever the height is;
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
		con.gridy = 12;
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
		con.gridy = 14;
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
		con.gridy = 14;
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
		con.gridy = 15;
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.fill = GridBagConstraints.BOTH;
		con.ipadx = 380;
		con.ipady = 200;
		con.insets = new Insets(0,0,30,0);
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
	public static JTextArea getPrintArea() {
		return printArea;
	}
	
	// Clear print area
	public static void clearPrintArea() {
		printArea.setText("");
	}
	
	// Set DB connection
	public static void setDBConnection(Connect dbCon) {
		dbConnect = dbCon;
	}
	
    public void setGraphContainer(SOCGraphContainer graphContainer) {
		this.graphContainer = graphContainer;
	}
	
	// Is connected
	private boolean isConnected() {
		if (dbConnect != null) {
			if (dbConnect.isConnected()) {
				return true;
			}
		}
		return false;		
	}
	
	// Not connected
	private void notConnected() {
		printArea.append("\nNot connected!");
		pListModel.removeAllElements();
		sListModel.removeAllElements();
		pListModel.addElement("No projects");
		sListModel.addElement("Select a project");
		chkStdLabel.setText("");
	}
	
	// Init values
	private void initValues() {
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

	// Set standards in use
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
				count = count + 2;
			}
			if (stdInUse.get(2) == 1) {
				stdToDisplay.add(" ROP");
				count = count + 4;
			}
			if (stdInUse.get(3) == 1) {
				stdToDisplay.add(" EOP");
				count = count + 8;
			}
			if (stdInUse.get(4) == 1) {
				stdToDisplay.add(" IL");
				count = count + 16;
			}
			switch(count){
			case 0: stdToDisplay.removeAll(stdToDisplay); stdToDisplay.add("Empty"); break;
			case 31: stdToDisplay.removeAll(stdToDisplay); stdToDisplay.add("All"); break;
			}
			
			chkStdLabel.setText(stdToDisplay.toString());
		}
	}
	
	// Refresh projects
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
	public void setProject() {
		if (isConnected()) {
			initValues();
			if (projectName.isEmpty()) {
				printArea.append("\nType or select a project name!");
			}
			else {
				projectID = dbConnect.getProjectID(projectName);
				Vector<String> standardV = new Vector<String>();
				sListModel.removeAllElements();
				int index = standardList.getSelectedIndex();
				standardV = dbConnect.getAllStandards(projectID, index);
				Collections.sort(standardV, String.CASE_INSENSITIVE_ORDER);
				for (int i=0; i<standardV.size(); i++) {
					sListModel.addElement(standardV.elementAt(i));
				}
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
			encodingName = "UTF8";
		}
		
		// UTF-16 Item
		else if (e.getSource() == encUTF16Item) {
			encodingName = "UTF16";
		}
		
		// SOC type Item
		else if (e.getSource() == SOCItem) {
			dataTypeName = "SOC object";
		}
		
		// File type Item
		else if (e.getSource() == fileItem) {
			dataTypeName = "XML file";
		}
		
		// ------------------------------
		
		// Create/ use project button
		else if (e.getSource() == useProjectButton || e.getSource() == projectField) {
			if (isConnected() && !projectField.getText().isEmpty()) {
				setProject();
				refreshProjects();
				setProject();
				int index = pList.getNextMatch(projectName, 0, Position.Bias.Forward);
				if (index > -1) { 
					if (projectName.equals((String)pListModel.get(index))) {
						projectID = dbConnect.getProjectID(projectName);
						pList.setSelectedIndex(index);
						pList.ensureIndexIsVisible(index);
						setStandardsInUse();
					}
				}
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
					int okFlag = JOptionPane.showConfirmDialog(null,"Permanently delete project: " + projectName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
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
		}
		
		// Import selected Button
		else if (e.getSource() == importButton) {
			if (isConnected()) {
				initValues();
				if (projectID > 0 && !standardName.isEmpty()) {
					int index = standardList.getSelectedIndex();
					if (dataTypeName.equals("SOC object")) {
						@SuppressWarnings("unused")
						Object o = dbConnect.getStandardXMLAsObject(projectID, index, standardName);
						if(graphContainer != null){
							graphContainer.insertResource(o, null);
						}
					}
					else if (dataTypeName.equals("XML file")){
						String xmlStr = dbConnect.getStandardXMLAsString(projectID, index, standardName);
						Object inputValue = JOptionPane.showInputDialog(this, "Select output path", "Output path", JOptionPane.INFORMATION_MESSAGE, null, null, outputPath); 
						if (inputValue != null) {
							outputPath = inputValue.toString();
							File outputFile = new File(inputValue.toString());
							outputFile.mkdirs();
							outputFile = new File (inputValue  + "\\" + standardsAbbr[standardList.getSelectedIndex()] + standardName + ".xml" );
							try {
							   FileWriter fstream = new FileWriter(outputFile);
							   BufferedWriter out = new BufferedWriter(fstream);
							   out.write(xmlStr);
							   out.close();
							   printArea.append("\nFile created: " + outputFile.toString());
							}catch (IOException ioe) {
								printArea.append("\nIOException: " + ioe.getMessage());
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

		// Export to DB Button
		else if (e.getSource() == exportButton) {
			if (isConnected()) {
				initValues();
				if (projectID == 0) {
					printArea.append("\nChoose a project!");
				}
				else {
					if (dataTypeName.equals("XML file")) {
						JFileChooser fc = new JFileChooser();
						if (xmlFile != null) {
							fc.setCurrentDirectory(xmlFile);
						}
						int returnVal = fc.showOpenDialog(glassPanel); 
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							xmlFile = fc.getSelectedFile();
							String xmlStr = "";
							try {
								// Set Reader object to selected encoding
								Reader r = new InputStreamReader(new FileInputStream(xmlFile), encodingName);
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
						else
							printArea.append("\nExport command cancelled by user");
					}
					else if (dataTypeName.equals("SOC object")) {
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
				if (projectID > 0 && !standardName.isEmpty()) {
					int okFlag = JOptionPane.showConfirmDialog(null,"Permanently delete " + standardsAbbr[standardList.getSelectedIndex()] + ": " + standardName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
					if (okFlag == JOptionPane.YES_OPTION) {
						int index = standardList.getSelectedIndex();
						dbConnect.deleteStandard(projectID, index, standardName);
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
					Vector<String> vector = new Vector<String>();
					sListModel.removeAllElements();
					int index = standardList.getSelectedIndex();
					vector = dbConnect.getAllStandards(projectID, index);
					Collections.sort(vector, String.CASE_INSENSITIVE_ORDER);
					for (int i=0; i<vector.size(); i++) {
						sListModel.addElement(vector.elementAt(i));
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
				pList.setSelectedIndex(lst.getSelectedIndex());
				projectField.setText(dlm.getElementAt(lst.getSelectedIndex()).toString());
				setStandardsInUse();
			}
			else if (e.getSource() == sList) {
				sList.setSelectedIndex(lst.getSelectedIndex());
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

/*
	-------------------------
	--< Class ActionJList >--
	-------------------------
*/
class ActionJList extends MouseAdapter{
	protected JList list;
    
	public ActionJList(JList l){
	  list = l;
	}
    
	public void mouseClicked(MouseEvent e){
	}
}