/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import javax.help.*;
import org.supremica.log.*;
import org.supremica.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.help.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;

public class Supremica
	extends JFrame
	implements TableModelListener, Gui, VisualProjectContainerListener
{
	private final static InterfaceManager theInterfaceManager = InterfaceManager.getInstance();
	private static Logger logger = LoggerFactory.createLogger(Supremica.class);
	private LogDisplay theLogDisplay = LogDisplay.getInstance();
	private JPanel contentPane;

	private MainMenuBar menuBar = new MainMenuBar(this);
	private MainToolBar toolBar = new MainToolBar(this);
	private MainPopupMenu mainPopupMenu = new MainPopupMenu(this);

	private VisualProjectContainer theVisualProjectContainer;
	private TypeCellEditor typeEditor;
	private PreferencesDialog thePreferencesDialog = null;
	private BorderLayout layout;
	private JTable theAutomatonTable;
	private TableSorter theTableSorter;
	private TableModel fullTableModel;
	private JScrollPane theAutomatonTableScrollPane;
	private MenuHandler menuHandler;
	private JSplitPane splitPaneVertical;
	private Server xmlRpcServer = null;
	private ContentHelp help = null;
	private CSH.DisplayHelpFromSource helpDisplayer = null;
	private FileSecurity fileSecurity = new FileSecurity();

	// MF -- made publically available
	public static int TABLE_IDENTITY_COLUMN = 0;
	public static int TABLE_TYPE_COLUMN = 1;
	public static int TABLE_STATES_COLUMN = 2;
	public static int TABLE_EVENTS_COLUMN = 3;
	public static ImageIcon cornerIcon = (new ImageIcon(Supremica.class.getResource("/icons/cornerIcon.gif")));
	public static Image cornerImage = cornerIcon.getImage();

	// Construct the frame
	public Supremica()
	{
		theVisualProjectContainer = new VisualProjectContainer();

		theVisualProjectContainer.addListener(this);

		VisualProject theVisualProject = new VisualProject("");

		theVisualProjectContainer.addProject(theVisualProject);
		setActiveProject(theVisualProject);

		// theVisualProjectContainer = currProject.getVisualProjectContainer();
		// theVisualProjectContainer.addListener(this);
		logger.info("Supremica version: " + (new Version()).toString());

		if (SupremicaProperties.isXmlRpcActive())
		{
			boolean serverStarted = true;

			try
			{
				xmlRpcServer = new Server(theVisualProjectContainer, SupremicaProperties.getXmlRpcPort());
			}
			catch (Exception e)
			{
				serverStarted = false;

				logger.warn("Another server already running on port " + SupremicaProperties.getXmlRpcPort() + ". XML-RPC server not started!");
			}

			if (serverStarted)
			{
				logger.info("XML-RPC server running on port " + SupremicaProperties.getXmlRpcPort());
			}
		}

		layout = new BorderLayout();
		fullTableModel = getActiveProject().getFullTableModel();
		theTableSorter = new TableSorter(fullTableModel);
		theAutomatonTable = new JTable(theTableSorter);
		theAutomatonTable.getTableHeader().setReorderingAllowed(false);

		theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

		menuHandler = new MenuHandler(

		/**
		 *  theAutomatonTable
		 */
		);
		theAutomatonTableScrollPane = new JScrollPane(theAutomatonTable);

		JViewport vp = theAutomatonTableScrollPane.getViewport();

		vp.setBackground(Color.white);
		theAutomatonTable.setBackground(Color.white);

		splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, theAutomatonTableScrollPane, theLogDisplay.getComponent());

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		help = new ContentHelp();

		try
		{
			jbInit();
		}
		catch (Exception ex)
		{
			logger.debug(ex.getStackTrace());
		}

		// This code used to be in the popup menu -------------
		theAutomatonTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int col = theAutomatonTable.columnAtPoint(e.getPoint());
					int row = theAutomatonTable.rowAtPoint(e.getPoint());

					if (row < 0)
					{
						return;
					}

					if (SupremicaProperties.useDot())
					{
						if (col == TABLE_IDENTITY_COLUMN)
						{
							ActionMan.automatonView_actionPerformed(getGui());
							getGui().repaint();
						}

					}
				}
			}

			public void mousePressed(MouseEvent e)
			{

				// This is needed for the Linux platform
				// where isPopupTrigger is true only on mousePressed.
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{

				// This is for triggering the popup on Windows platforms
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					int currRow = theAutomatonTable.rowAtPoint(e.getPoint());

					if (currRow < 0)
					{
						return;
					}

					if (!theAutomatonTable.isRowSelected(currRow))
					{
						theAutomatonTable.clearSelection();
						theAutomatonTable.setRowSelectionInterval(currRow, currRow);
					}

					mainPopupMenu.show(theAutomatonTable.getSelectedRowCount(), e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// --------------------------------------//
	}

	public Supremica(String arg)
	{
		this();

		if (arg != null)
		{
			openProjectXMLFile(new File(arg));
		}
	}

	// local helper utility
	Gui getGui()
	{
		return this;
	}

	public void setActiveProject(VisualProject activeProject)
	{
		theVisualProjectContainer.setActiveProject(activeProject);
		updateTitle();
	}

	public VisualProject getActiveProject()
	{
		return theVisualProjectContainer.getActiveProject();
	}

	public void updateTitle()
	{
		Project currProject = getActiveProject();

		if (currProject != null)
		{
			String projectName = currProject.getName();

			setTitle("Supremica " + projectName);
		}
		else
		{
			setTitle("Supremica");
		}
	}

	private JFrame getCurrentFrame()
	{
		return this;
	}

	public FileSecurity getFileSecurity()
	{
		return fileSecurity;
	}

	// Component initialization
	private void jbInit()
		throws Exception
	{
		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(layout);
		contentPane.setOpaque(true);
		contentPane.setBackground(Color.white);
		setSize(new Dimension(800, 600));

		// theVisualProjectContainer.updateFrameTitles();
		// Enables stylish rollover buttions - JDK 1.4 required
//		toolBar.setRollover(true);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(splitPaneVertical, BorderLayout.CENTER);
		splitPaneVertical.setContinuousLayout(false);
		splitPaneVertical.setOneTouchExpandable(false);
		fullTableModel.addTableModelListener(this);
		theAutomatonTable.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					ActionMan.automataDelete_actionPerformed(getGui());
				}
			}

			public void keyReleased(KeyEvent e) {}

			public void keyTyped(KeyEvent e) {}
		});

		typeEditor = new TypeCellEditor(theAutomatonTable, theTableSorter, theVisualProjectContainer);
		helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

		// initMenubar();
		setJMenuBar(menuBar);

		// initToolbar();
	}

	public void initialize()
	{
		setIconImage(Supremica.cornerImage);
		setVisible(true);
		splitPaneVertical.setDividerLocation(0.7);
		// Set the preferred column width of the automaton table
		int tableWidth = theAutomatonTable.getWidth();
		int tableWidthEntity = tableWidth/12;
		TableColumnModel theTableColumnModel = theAutomatonTable.getColumnModel();
		for (int i = 0; i < theAutomatonTable.getColumnCount(); i++)
		{
			TableColumn currColumn = theTableColumnModel.getColumn(i);
			if (i == 0)
			{
				currColumn.setPreferredWidth(tableWidthEntity*5);
			}
			else if (i == 1)
			{
				currColumn.setPreferredWidth(tableWidthEntity*3);
			}
			else
			{
				currColumn.setPreferredWidth(tableWidthEntity*2);
			}
		}
		//setVisible(false);
	}



	// ** MF ** Implementation of Gui stuff
	public void error(String msg)
	{
		logger.error(msg);
	}

	public void error(String msg, Throwable t)
	{
		logger.error(msg, t);
	}

	public void info(String msg)
	{
		logger.info(msg);
	}

	public void debug(String msg)
	{
		logger.debug(msg);
	}

	public void clearSelection()
	{
		theAutomatonTable.clearSelection();
	}

	/**
	 * Selects the automata indicated by selectionIndices
	 */
 	public void selectAutomata(int[] selectionIndices)
	{
		// We must set the autoscrolls property false for esthetical reasons
		// but keep it unchanged after the operation for consistency
		boolean autoscrolls = theAutomatonTable.getAutoscrolls();
		theAutomatonTable.setAutoscrolls(false);
		for (int i=0; i<selectionIndices.length; i++)
		{
			theAutomatonTable.changeSelection(selectionIndices[i], 0, true, false);
		}
		theAutomatonTable.setAutoscrolls(autoscrolls);
	}

	/**
	 * Unselects automaton indicated by automaton index
	 *
	 * @param index The relative index of the automaton among the other
	 * selected automata. (Should be Automata.getAutomatonIndex)
	 */
	public void unselectAutomaton(int index)
	{
		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		theAutomatonTable.changeSelection(selectedRowIndices[index],0,true,false);
	}

	/**
	  Inverts the selection in theAutomatonTable.
	 */
	public void invertSelection()
	{
		// We must set the autoscrolls property false for esthetical reasons
		// but keep it unchanged after the operation for consistency
		boolean autoscrolls = theAutomatonTable.getAutoscrolls();
		theAutomatonTable.setAutoscrolls(false);
		for (int i=0; i<theAutomatonTable.getRowCount(); i++)
			theAutomatonTable.changeSelection(i,0,true,false);
		theAutomatonTable.setAutoscrolls(autoscrolls);
	}

	public void selectAll()
	{
		theAutomatonTable.selectAll();
	}



	public Component getComponent()
	{
		return this;
	}

	public JFrame getFrame()
	{
		return this;
	}

	/**
	 * This is a deprecated method, use getSelectedAutomata instead.
	 *
	 * THIS METHOD IS USED BY (AT LEAST) ActionMan AND AutomataVerificationWorker!!!
	 *
	 *@return  The selectedAutomataAsCollection value
	 */
	public Collection getSelectedAutomataAsCollection()
	{
		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		LinkedList selectedAutomata = new LinkedList();

		for (int i = 0; i < selectedRowIndices.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndices[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedAutomata.add(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
				logger.debug(ex.getStackTrace());
			}
		}

		return selectedAutomata;
	}

	public Automata getSelectedAutomata()
	{
		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		Automata selectedAutomata = new Automata();

		for (int i = 0; i < selectedRowIndices.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndices[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedAutomata.addAutomaton(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
				logger.debug(ex.getStackTrace());
			}
		}

		return selectedAutomata;
	}

	public Automata getUnselectedAutomata()
	{
		/* Simple... but flickery!
		   invertSelection();
		   Automata unSelectedAutomata = getSelectedAutomata();
		   invertSelection();
		   return unSelectedAutomata;
		*/

		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		Automata unselectedAutomata = new Automata();
		int j = 0;

		for (int i = 0; i < theAutomatonTable.getRowCount(); i++)
		{
			if ((j >= selectedRowIndices.length) || (i != selectedRowIndices[j]))
			{
				try
				{
					int currIndex = i;
					int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
					Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

					unselectedAutomata.addAutomaton(currAutomaton);
				}
				catch (Exception ex)
				{
					logger.error("Trying to get an automaton that does not exist. Index: " + i);
					logger.debug(ex.getStackTrace());
				}
			}
			else
			{
				j++;
			}
		}

		return unselectedAutomata;
	}

	// Same as getSelectedAutomata but include execution information
	public Project getSelectedProject()
	{
		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		Project selectedProject = new Project();

		for (int i = 0; i < selectedRowIndices.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndices[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedProject.addAutomaton(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
				logger.debug(ex.getStackTrace());
			}
		}
		Project activeProject = getActiveProject();
		if (activeProject != null)
		{
			selectedProject.addAttributes(activeProject);
			//selectedProject.addActions(activeProject.getActions());
			//selectedProject.addControls(activeProject.getControls());
			//selectedProject.setAnimationURL(activeProject.getAnimationURL());
		}
		return selectedProject;
	}

	// Tools.AutomataEditor
	public void toolsAutomataEditor()
	{
		getActiveProject().getAutomataEditor();
	}

	public void renameProject()
	{
		String newName = getNewProjectName();

		if (newName != null)
		{
			getActiveProject().setName(newName);
			getActiveProject().setProjectFile(null);
		}
	}

	public void commentProject()
	{
		String newComment = getNewProjectComment();

		if (newComment != null)
		{
			getActiveProject().setComment(newComment);
			//getActiveProject().setProjectFile(null);
		}
	}

	// Help.About action performed
	public void helpAbout()
	{
		AboutBox dlg = new AboutBox(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();

		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	// Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			ActionMan.fileExit(this);
		}
	}

	public String getNewProjectName()
	{
		String msg = "Enter new project name";
		boolean finished = false;
		String oldName = getActiveProject().getName();
		String newName = "";

		while (!finished)
		{
			newName = JOptionPane.showInputDialog(this, msg, oldName);

			if (newName == null)
			{
				return null;
			}
			else if (newName.equals(""))
			{
				JOptionPane.showMessageDialog(this, "An empty name is not allowed", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				finished = true;
			}
		}

		return newName;
	}

	public String getNewProjectComment()
	{
		// String msg = "Enter new project comment";
		boolean finished = false;
		String oldComment = getActiveProject().getComment();
		String newComment = "";

		while (!finished)
		{
			// newComment = JOptionPane.showInputDialog(this, msg, oldComment);
			EditCommentDialog dialog = new EditCommentDialog(this, oldComment);
			newComment = dialog.getComment();

			if (newComment == null)
			{
				return null;
			}
			else
			{
				finished = true;
			}
		}

		return newComment;
	}

	public String getNewAutomatonName(String msg, String nameSuggestion)
	{
		boolean finished = false;
		String newName = "";

		while (!finished)
		{
			newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name", JOptionPane.QUESTION_MESSAGE, null, null, nameSuggestion);

			if (newName == null)
			{
				return null;
			}
			else if (newName.equals(""))
			{
				JOptionPane.showMessageDialog(this, "An empty name is not allowed", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else if (getActiveProject().containsAutomaton(newName))
			{
				JOptionPane.showMessageDialog(this, "'" + newName + "' already exists", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				finished = true;
			}
		}

		return newName;
	}

	private int getIntegerInDialogWindow(String text)
	{
		boolean finished = false;
		String theInteger = "";
		int theIntValue = -1;

		while (!finished)
		{
			theInteger = JOptionPane.showInputDialog(this, text);

			try
			{
				theIntValue = Integer.parseInt(theInteger);
				finished = true;
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Not a valid integer", "Alert", JOptionPane.ERROR_MESSAGE);
			}
		}

		return theIntValue;
	}

	void openFile(File file)
	{
		openProjectXMLFile(file);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting()) {}
	}

	public void tableChanged(TableModelEvent e)
	{
		// logger.debug("Supremica.tableChanged");
		theAutomatonTable.revalidate();
	}

	public void openProjectXMLFile(File file)
	{
		Project currProject = null;

		logger.info("Opening " + file.getAbsolutePath() + " ...");

		try
		{
			ProjectBuildFromXml builder = new ProjectBuildFromXml(new VisualProjectFactory());

			currProject = builder.build(file);
		}
		catch (Exception ex)
		{

			// this exception is caught while opening
			logger.error("Error while opening " + file.getAbsolutePath() + " " + ex.getMessage());
			logger.debug(ex.getStackTrace());
			return;
		}

		int nbrOfProjectBeforeOpening = getActiveProject().getNbrOfAutomata();

		try
		{
			int nbrOfAddedProject = addAutomata(currProject);

			logger.info("Successfully opened and added " + nbrOfAddedProject + " automata.");
		}
		catch (Exception ex)
		{
			logger.error("Error adding automata " + file.getAbsolutePath() + " " + ex.getMessage());
			logger.debug(ex.getStackTrace());
			return;
		}

		if (nbrOfProjectBeforeOpening == 0)
		{
			String projectName = currProject.getName();

			if (projectName != null)
			{
				getActiveProject().setName(projectName);
				//logger.debug("Project name changed to \"" + projectName + "\"");
			}
		}

		if (nbrOfProjectBeforeOpening > 0)
		{
			File projectFile = getActiveProject().getProjectFile();

			if (projectFile != null)
			{
				getActiveProject().setProjectFile(null);
			}
		}
		else
		{
			getActiveProject().setProjectFile(file);
		}
	}

	public VisualProjectContainer getVisualProjectContainer()
	{
		return theVisualProjectContainer;
	}

	public MainPopupMenu getMainPopupMenu()
	{
		return mainPopupMenu;
	}

	public void addAttributes(Project otherProject)
	{
		Project currProject = getActiveProject();
		currProject.addAttributes(otherProject);
	}


/*
	public void addActions(Actions theActions)
	{
		Project currProject = getActiveProject();
		currProject.addActions(theActions);
	}

	public void addControls(Controls theControls)
	{
		Project currProject = getActiveProject();
		currProject.addControls(theControls);
	}

	public void setAnimationURL(URL animationURL)
	{
		Project currProject = getActiveProject();
		currProject.setAnimationURL(animationURL);
	}
*/

	public int addAutomata(Automata currAutomata)
	{
		//-- MF -- debug stuff, is there no way to remove the if under no-debug-build?
		if(currAutomata.size() == 0)
		{
			logger.debug("Supremica::addAutomata(): adding empty automata.");
			return 0; // "nothing to do, nowhere to go-o" [Ramones 1978]
		}
		//-- MF --

		int nbrOfAddedAutomata = 0;
		Iterator autIt = currAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (addAutomaton(currAutomaton))
			{
				nbrOfAddedAutomata++;
			}
			else
			{
				// Must have a way to say, "cancel all"?
			}
		}

		return nbrOfAddedAutomata;
	}

	public int addProject(Project theProject)
		throws Exception
	{
		int nbrOfAutomataBeforeOpening = getVisualProjectContainer().getActiveProject().getNbrOfAutomata();

		int nbrOfAddedAutomata = addAutomata(theProject);
		if (theProject != null)
		{
			addAttributes(theProject);
			//addActions(theProject.getActions());
			//addControls(theProject.getControls());
			//setAnimationURL(theProject.getAnimationURL());
			/*
			String animPath = theProject.getAnimationPath();
			if (animPath != null && !animPath.equals(""))
			{
				setAnimationPath(animPath);
			}*/
		}

		if (theProject.getComment() != "")
		{
			JOptionPane.showMessageDialog(this, EncodingHelper.linebreakAdjust(theProject.getComment()),
										  "Project information", JOptionPane.INFORMATION_MESSAGE);
		}

		if (theProject.hasAnimation())
		{
			JOptionPane.showMessageDialog(this, "This project includes an animation.",
										  "Project information", JOptionPane.INFORMATION_MESSAGE);
		}

		if (nbrOfAutomataBeforeOpening == 0)
		{
			String projectName = theProject.getName();
			String projectComment = theProject.getComment();

			if (projectName != null)
			{
				getVisualProjectContainer().getActiveProject().setName(projectName);
				getVisualProjectContainer().getActiveProject().setComment(projectComment);
				//gui.info("Project name changed to \"" + projectName + "\"");
				getVisualProjectContainer().getActiveProject().updateFrameTitles();
			}
		}

		return nbrOfAddedAutomata;
	}

	// We need a single entry to add automata to the gui
	// Here we manage all necessary user interaction
	public boolean addAutomaton(Automaton currAutomaton)
	{
		logger.debug("Supremica::addAutomaton(" + currAutomaton.getName() + ")");
		// Force the user to enter a new name if it has no name
		//if (currAutomaton.getName() == null || currAutomaton.getName().equals(""))
		if (!currAutomaton.hasName())
		{
			String autName = getNewAutomatonName("Enter a new name", currAutomaton.getComment());

			if (autName == null)
			{
				return false;

				// not added
			}
			else
			{
				currAutomaton.setName(autName);
			}
		}

		if (getActiveProject().containsAutomaton(currAutomaton.getName()))
		{
			String autName = currAutomaton.getName();
			String newName = getActiveProject().getUniqueAutomatonName(autName);

			currAutomaton.setName(newName);
			logger.info("Name conflict - '" + autName + "' does already exist. Changed name of new '" + autName + "' to '" + newName + "'.");
		}

		try
		{   // throws Exception if the automaton already exists
			// logger.debug("Supremica.addAutomaton");
			getActiveProject().addAutomaton(currAutomaton);
		}
		catch (Exception ex)
		{
			// should never occur, we test for this condition already
			logger.error("Error while adding: " + ex.getMessage());
			logger.debug(ex.getStackTrace());
		}

		return true;
	}

	public void close()
	{
		setVisible(false);
		dispose();
	}

	public void destroy()
	{
		close();
	}

	public void projectAdded(VisualProjectContainer container, Project theProject)
	{
		logger.info("Project added: " + theProject.getName());
	}

	public void projectRemoved(VisualProjectContainer container, Project theProject)
	{
		logger.info("Project removed: " + theProject.getName());
	}

	public void projectRenamed(VisualProjectContainer container, Project theProject)
	{
		logger.info("Project renamed: " + theProject.getName());
	}

	public void updated(Object theObject) {}
}
