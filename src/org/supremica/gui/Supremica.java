/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;
import java.io.*;
// import toolbarButtonGraphics.*;

import org.apache.log4j.*;
import javax.help.*;

import org.supremica.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;

public class Supremica
	extends JFrame
	implements TableModelListener
{
	private static final InterfaceManager theInterfaceManager = InterfaceManager.getInstance();

	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();
	private JPopupMenu regionPopup;

	private AutomatonContainer theAutomatonContainer;
	private TypeCellEditor typeEditor;
	private PreferencesDialog thePreferencesDialog = null;

	private BorderLayout layout;

	private JTable theAutomatonTable;
	private TableSorter theTableSorter;
	private TableModel fullTableModel;

	private JScrollPane theAutomatonTableScrollPane;
	private MenuHandler menuHandler;

	private static Category thisCategory = LogDisplay.createCategory(Supremica.class.getName());

	private LogDisplay theLogDisplay = LogDisplay.getInstance();

	private JSplitPane splitPaneVertical;

	private Server xmlRpcServer = null;

	private ContentHelp help = null;
	private CSH.DisplayHelpFromSource helpDisplayer = null;

	public static int TABLE_IDENTITY_COLUMN = 0;
	public static int TABLE_TYPE_COLUMN = 1;
	public static int TABLE_STATES_COLUMN = 2;
	public static int TABLE_EVENTS_COLUMN = 3;

	public static ImageIcon cornerIcon = (new ImageIcon(Supremica.class.getResource("/icons/cornerIcon.gif")));
	public static Image cornerImage = cornerIcon.getImage();

	// Construct the frame
	public Supremica()
	{
		theAutomatonContainer = new AutomatonContainer(this);

		thisCategory.info("Supremica version: " + (new Version()).toString());
		if (WorkbenchProperties.isXmlRpcActive())
		{
			boolean serverStarted = true;
			try
			{
				xmlRpcServer = new Server(theAutomatonContainer, WorkbenchProperties.getXmlRpcPort());
			}
			catch (Exception e)
			{
				serverStarted = false;
				thisCategory.warn("Another server already running on port " + WorkbenchProperties.getXmlRpcPort() + ". XML-RPC server not started!");
			}
			if (serverStarted)
			{
				thisCategory.info("XML-RPC server running on port " + WorkbenchProperties.getXmlRpcPort());
			}
		}

		layout = new BorderLayout();

		fullTableModel = theAutomatonContainer.getFullTableModel();
		theTableSorter = new TableSorter(fullTableModel);
		theAutomatonTable = new JTable(theTableSorter);
		theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

		menuHandler = new MenuHandler(theAutomatonTable);

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
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private JFrame getCurrentFrame()
	{
		return this;
	}

	// Component initialization
	private void jbInit() throws Exception
	{
		contentPane = (JPanel)getContentPane();
		contentPane.setLayout(layout);
		contentPane.setOpaque(true);
		contentPane.setBackground(Color.white);

		setSize(new Dimension(800, 600));

		theAutomatonContainer.updateFrameTitles();

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
						automataDelete_actionPerformed();
					}
				}

				public void keyReleased(KeyEvent e)
				{
				}

				public void keyTyped(KeyEvent e)
				{
				}
			}


										 );

 		typeEditor = new TypeCellEditor(theAutomatonTable, theTableSorter, theAutomatonContainer);

		helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

		initMenubar();
		initToolbar();
		initPopups();
	}

	public void initialize()
	{
		setIconImage(Supremica.cornerImage);
		setVisible(true);
		splitPaneVertical.setDividerLocation(0.7);
		setVisible(false);
	}

	public void initMenubar()
	{
		setJMenuBar(menuBar);

		// File
		JMenu menuFile = new JMenu();
		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		// File.Open
		JMenuItem menuFileOpen = new JMenuItem();
		menuFileOpen.setText("Open...");
		menuFile.add(menuFileOpen);
		menuFileOpen.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileOpen(getCurrentFrame());
				}
			});

		// File.Save
		JMenuItem menuFileSave = new JMenuItem();
		menuFileSave.setText("Save");
		menuFileSave.setMnemonic(KeyEvent.VK_S);
		menuFile.add(menuFileSave);
		menuFileSave.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileSave();
				}
			});

		// File.SaveAs
		JMenuItem menuFileSaveAs = new JMenuItem();
		menuFileSaveAs.setText("Save As...");
		menuFile.add(menuFileSaveAs);
		menuFileSaveAs.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileSaveAs();
				}
			});

		menuFile.addSeparator();

		// File.Import
		JMenu menuFileImport = new JMenu();
		menuFileImport.setText("Import");
		menuFile.add(menuFileImport);

		// File.Import.Desco
		JMenuItem menuFileImportDesco = new JMenuItem();
		menuFileImportDesco.setText("From Desco...");
		menuFileImport.add(menuFileImportDesco);
		menuFileImportDesco.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileImportDesco();
				}
			});

		// File.Import.TCT
		JMenuItem menuFileImportTCT = new JMenuItem();
		menuFileImportTCT.setText("From TCT...");
		menuFileImport.add(menuFileImportTCT);
		menuFileImportTCT.setEnabled(false);
		menuFileImportTCT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//fileImportTCT();
				}
			});

		// File.Import.UMDES
		JMenuItem menuFileImportUMDES = new JMenuItem();
		menuFileImportUMDES.setText("From UMDES...");
		menuFileImport.add(menuFileImportUMDES);
		menuFileImportUMDES.setEnabled(false);
		menuFileImportUMDES.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//fileImportUMDES();
				}
			});

		// File.Import.Valid
		JMenuItem menuFileImportValid = new JMenuItem();
		menuFileImportValid.setText("From Valid...");
		menuFileImport.add(menuFileImportValid);
		menuFileImportValid.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileImportValid();
				}
			});

		// File.Export
		JMenu menuFileExport = new JMenu();
		menuFileExport.setText("Export");
		menuFile.add(menuFileExport);

		// File.Export.Desco
		JMenuItem menuFileExportDesco = new JMenuItem();
		menuFileExportDesco.setText("To Desco...");
		menuFileExport.add(menuFileExportDesco);
		menuFileExportDesco.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileExportDesco();
				}
			});

		// File.Export.TCT
		JMenuItem menuFileExportTCT = new JMenuItem();
		menuFileExportTCT.setText("To TCT...");
		menuFileExport.add(menuFileExportTCT);
		menuFileExportTCT.setEnabled(false);
		menuFileExportTCT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//fileExportTCT();
				}
			});

		// File.Export.UMDES
		JMenuItem menuFileExportUMDES = new JMenuItem();
		menuFileExportUMDES.setText("To UMDES...");
		menuFileExport.add(menuFileExportUMDES);
		menuFileExportUMDES.setEnabled(false);
		menuFileExportUMDES.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//fileExportUMDES();
				}
			});

		// File.Export.Valid
		JMenuItem menuFileExportValid = new JMenuItem();
		menuFileExportValid.setText("To Valid...");
		menuFileExport.add(menuFileExportValid);
		menuFileExportUMDES.setEnabled(false);
		menuFileExportValid.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileExportValid();
				}
			});


		menuFile.addSeparator();

		// File.Exit
		JMenuItem menuFileExit = new JMenuItem();
		menuFileExit.setText("Exit");
		menuFile.add(menuFileExit);
		menuFileExit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileExit();
				}
			});

		// Project
		JMenu menuProject = new JMenu();
		menuProject.setText("Project");
		menuProject.setMnemonic(KeyEvent.VK_P);
		menuBar.add(menuProject);

		// Project.Rename
		JMenuItem menuProjectRename = new JMenuItem();
		menuProjectRename.setText("Rename...");
		menuProject.add(menuProjectRename);
		menuProjectRename.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					renameProject();
				}
			});

		// Tools.AutomataEditor
		if (WorkbenchProperties.includeEditor())
		{
			// Tools
			JMenu menuTools = new JMenu();
			menuTools.setText("Tools");
			menuTools.setMnemonic(KeyEvent.VK_T);
			menuBar.add(menuTools);

			JMenuItem menuToolsAutomataEditor = new JMenuItem();
			menuToolsAutomataEditor.setText("Editor");
			menuTools.add(menuToolsAutomataEditor);

			menuToolsAutomataEditor.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						toolsAutomataEditor();
					}
				});

			JMenu menuToolsCodeGeneration = new JMenu();
			menuToolsCodeGeneration.setText("Code Generation");
			menuTools.add(menuToolsCodeGeneration);

			JMenuItem menuToolsCodeGenerationIL = new JMenuItem();
			menuToolsCodeGenerationIL.setText("IEC-1131 Instruction List...");
			menuToolsCodeGenerationIL.setEnabled(false);

			menuToolsCodeGeneration.add(menuToolsCodeGenerationIL);

			menuToolsCodeGenerationIL.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						//toolsAutomataEditor();
					}
				});

			JMenuItem menuToolsCodeGenerationBC = new JMenuItem();
			menuToolsCodeGenerationBC.setText("Java Bytecode...");
			menuToolsCodeGenerationBC.setEnabled(false);

			menuToolsCodeGeneration.add(menuToolsCodeGenerationBC);

			menuToolsCodeGenerationBC.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						//toolsAutomataEditor();
					}
				});
		}

		// Configure
		JMenu menuConfigure = new JMenu();
		menuConfigure.setText("Configure");
		menuConfigure.setMnemonic(KeyEvent.VK_C);
		menuBar.add(menuConfigure);

		// Configure.Preferences
		JMenuItem menuConfigurePreferences = new JMenuItem();
		menuConfigurePreferences.setText("Preferences...");
		menuConfigure.add(menuConfigurePreferences);
		menuConfigurePreferences.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					configurePreferences_actionPerformed(e);
				}
			});

		// Help
		JMenu menuHelp = new JMenu();
		menuHelp.setText("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);


		// Help.Help Topics
		JMenuItem menuHelpTopics = new JMenuItem("Help Topics");
		//menuHelpTopics.setMnemonic(KeyEvent.VK_H);
		menuHelpTopics.addActionListener(helpDisplayer);
		menuHelp.add(menuHelpTopics);

		menuHelp.addSeparator();

		// Help.About
		JMenuItem menuHelpAbout = new JMenuItem();
		menuHelpAbout.setText("About...");
		menuHelp.add(menuHelpAbout);
		menuHelpAbout.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					helpAbout();
				}
			});

	}

	public void initToolbar()
	{
		// toolBar.setLayout(new BorderLayout());

		// Create buttons
		JButton openButton = new JButton();
		openButton.setToolTipText("Open");
		ImageIcon open16Img = new ImageIcon(Supremica.class.getResource(
			"/toolbarButtonGraphics/general/Open16.gif"));
		openButton.setIcon(open16Img);
		openButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileOpen(getCurrentFrame());
				}
			});

		JButton saveButton = new JButton();
		saveButton.setToolTipText("Save");
		ImageIcon save16Img = new ImageIcon(Supremica.class.getResource(
			"/toolbarButtonGraphics/general/Save16.gif"));
		saveButton.setIcon(save16Img);
		saveButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileSave();
				}
			});

		JButton saveAsButton = new JButton();
		saveAsButton.setToolTipText("Save As");
		ImageIcon saveAs16Img = new ImageIcon(Supremica.class.getResource(
			"/toolbarButtonGraphics/general/SaveAs16.gif"));
		saveAsButton.setIcon(saveAs16Img);
		saveAsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileSaveAs();
				}
			});

		JButton editButton = new JButton();
		editButton.setToolTipText("Edit");
		ImageIcon edit16Img = new ImageIcon(Supremica.class.getResource(
			"/toolbarButtonGraphics/general/Edit16.gif"));
		editButton.setIcon(edit16Img);
		editButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					toolsAutomataEditor();
				}
			});

		JButton helpButton = new JButton();
		helpButton.setToolTipText("Help");
		ImageIcon help16Img = new ImageIcon(Supremica.class.getResource(
			"/toolbarButtonGraphics/general/Help16.gif"));
		helpButton.setIcon(help16Img);
		helpButton.addActionListener(helpDisplayer);

		// Set margins
		Insets tmpInsets = new Insets(0, 0, 0, 0);
		openButton.setMargin(tmpInsets);
		saveButton.setMargin(tmpInsets);
		saveAsButton.setMargin(tmpInsets);
		editButton.setMargin(tmpInsets);
		helpButton.setMargin(tmpInsets);

		// Add buttons to toolbar
		toolBar.add(openButton, "WEST");
		toolBar.add(saveButton, "WEST");
		toolBar.add(saveAsButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(editButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(helpButton, "EAST");
	}

	public void initPopups()
	{
		JMenuItem selectAllItem = new JMenuItem("Select all");
		menuHandler.add(selectAllItem, 0);

		menuHandler.addSeparator();

		JMenuItem statusItem = new JMenuItem("Status");
		menuHandler.add(statusItem, 0);

		JMenuItem exploreItem = new JMenuItem("Explore states");
		menuHandler.add(exploreItem, 1);

		JMenuItem alphabetItem = new JMenuItem("View alphabet");
		menuHandler.add(alphabetItem, 1);

		if (WorkbenchProperties.useDot())
		{
			JMenuItem viewItem = new JMenuItem("View graph");
			menuHandler.add(viewItem, 1);

			viewItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						automatonView_actionPerformed(e);
						repaint();
					}
				});
		}

		menuHandler.addSeparator();

		JMenuItem synchronizeItem = new JMenuItem("Synchronize");
		menuHandler.add(synchronizeItem, 2);

		JMenuItem verifyItem = new JMenuItem("Verify");
		menuHandler.add(verifyItem, 1);

		/*
		JMenuItem controllabilityCheckItem = new JMenuItem("Controllability check");
		menuHandler.add(controllabilityCheckItem, 2);
		*/

		/*
		JMenuItem fastControllabilityCheckItem = new JMenuItem("Fast controllability check");
		menuHandler.add(fastControllabilityCheckItem, 2);
		*/

		/*
		JMenuItem pairwiseCheckItem = new JMenuItem("Pairwise controllability check");
		menuHandler.add(pairwiseCheckItem, 2);
		*/

		/*
		JMenuItem languageInclusionCheckItem = new JMenuItem("Language inclusion check");
		menuHandler.add(languageInclusionCheckItem, 1);
		*/

		JMenuItem synthesizeItem = new JMenuItem("Synthesize");
		menuHandler.add(synthesizeItem, 1);

		menuHandler.addSeparator();

		JMenuItem purgeItem = new JMenuItem("Purge");
		menuHandler.add(purgeItem, 1);

		JMenuItem minimizeItem = new JMenuItem("Minimize");
		menuHandler.add(minimizeItem, 1);

		JMenuItem allAcceptingItem = new JMenuItem("Set all states as accepting");
		menuHandler.add(allAcceptingItem, 1);

		JMenuItem complementItem = new JMenuItem("Automaton complement");
		menuHandler.add(complementItem, 1);

		menuHandler.addSeparator();

		if (WorkbenchProperties.includeBoundedUnconTools())
		{
			JMenuItem extendItem = new JMenuItem("Extend");
			menuHandler.add(extendItem, 1);

			JMenuItem removePassItem = new JMenuItem("Remove pass events");
			menuHandler.add(removePassItem, 1);

			JMenuItem addSelfLoopArcsItem = new JMenuItem("Add self-loop arcs");
			menuHandler.add(addSelfLoopArcsItem, 1);

			JMenuItem removeSelfLoopArcsItem = new JMenuItem("Remove self-loop arcs");
			menuHandler.add(removeSelfLoopArcsItem, 1);

			JMenuItem normalizeAlphabetItem = new JMenuItem("Normalize alphabet");
			menuHandler.add(normalizeAlphabetItem, 1);

			menuHandler.addSeparator();

			extendItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						automataExtend_actionPerformed(e);
						repaint();
					}
				});

			removePassItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						automataRemovePass_actionPerformed(e);
						repaint();
					}
				});

			addSelfLoopArcsItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						automataAddSelfLoopArcs_actionPerformed(e);
						repaint();
					}
				});

			removeSelfLoopArcsItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						automataRemoveSelfLoopArcs_actionPerformed(e);
						repaint();
					}
				});

			normalizeAlphabetItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						normalizeAlphabet_actionPerformed(e);
						repaint();
					}
				});
		}

		JMenuItem alphabetAnalyzerItem = new JMenuItem("Analyze alphabets");
		menuHandler.add(alphabetAnalyzerItem, 2);

		alphabetAnalyzerItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					alphabetAnalyzer_actionPerformed(e);
					repaint();
				}
			});

	//** MF Find States **
		JMenuItem findStatesItem = new JMenuItem("Find States...");
		menuHandler.add(findStatesItem, 1);
		findStatesItem.addActionListener(new ActionListener()
		{	// anonymous class (is this a good thing?)
			public void actionPerformed(ActionEvent e)
			{
				findStates_action(e);
				repaint();
			}
		});
		
		menuHandler.addSeparator(); //----------------------------------------------


		JMenuItem copyItem = new JMenuItem("Copy");
		menuHandler.add(copyItem, 1);

		JMenuItem deleteItem = new JMenuItem("Delete");
		menuHandler.add(deleteItem, 1);

		JMenuItem deleteAllItem = new JMenuItem("Delete all");
		menuHandler.add(deleteAllItem, 0);

		JMenuItem renameItem = new JMenuItem("Rename");
		menuHandler.add(renameItem, 1);

		menuHandler.addSeparator();


		// JMenuItem saveAsItem = new JMenuItem("Save As...");
		// menuHandler.add(saveAsItem, 1);

		JMenuItem exportItem = new JMenuItem("Export...");
		menuHandler.add(exportItem, 1);

		selectAllItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					selectAll_actionPerformed(e);
				}
			});

		statusItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automatonStatus_actionPerformed(e);
				}
			});

		exploreItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automatonExplore_actionPerformed(e);
					repaint();
				}
			});

		alphabetItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automatonAlphabet_actionPerformed(e);
					repaint();
				}
			});


		synchronizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataSynchronize_actionPerformed(e);
					repaint();
				}
			});

		verifyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataVerify_actionPerformed(e);
					repaint();
				}
			});

		/*
		controllabilityCheckItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataControllabilityCheck_actionPerformed(e);
					repaint();
				}
			});
		*/

		/*
		fastControllabilityCheckItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataFastControllabilityCheck_actionPerformed(e);
					repaint();
				}
			});
		*/

		/*
		pairwiseCheckItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataPairwiseCheck_actionPerformed(e);
					repaint();
				}
			});
		*/

		/*
		languageInclusionCheckItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					languageInclusionCheck_actionPerformed(e);
					repaint();
				}
			});
		*/

		synthesizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataSynthesize_actionPerformed(e);
					repaint();
				}
			});

		purgeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataPurge_actionPerformed(e);
					repaint();
				}
			});

		allAcceptingItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataAllAccepting_actionPerformed(e);
					repaint();
				}
			});

		complementItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataComplement_actionPerformed(e);
					repaint();
				}
			});

		minimizeItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automatonMinimize_actionPerformed(e);
					repaint();
				}
			});

		copyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataCopy_actionPerformed(e);
					repaint();
				}
			});

		deleteItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataDelete_actionPerformed();
					repaint();
				}
			});

		deleteAllItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataDeleteAll_actionPerformed(e);
					repaint();
				}
			});

		renameItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataRename_actionPerformed();
					repaint();
				}
			});

		//saveAsItem.addActionListener(new ActionListener()
		//	{
		//		public void actionPerformed(ActionEvent e)
		//		{
		//			automataSaveAs_actionPerformed(e);
		//			repaint();
		//		}
		//	});


		exportItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					automataExport();
					repaint();
				}
			});

		theAutomatonTable.addMouseListener(new MouseAdapter()
			{
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
						int currRow = theAutomatonTable.rowAtPoint(new Point(e.getX(), e.getY()));
						if (currRow < 0)
						{
							return;
						}
						if (!theAutomatonTable.isRowSelected(currRow))
						{
							theAutomatonTable.clearSelection();
							theAutomatonTable.setRowSelectionInterval(currRow, currRow);
						}
						regionPopup = menuHandler.getDisabledPopupMenu();
						regionPopup.show(e.getComponent(),
										 e.getX(), e.getY());
					}
				}
			});
	}

	/**
	 * This is a deprecated method, use getSelectedAutomata instead.
	 */
	public Collection getSelectedAutomataAsCollection()
	{
		int[] selectedRowIndicies = theAutomatonTable.getSelectedRows();
		LinkedList selectedAutomata = new LinkedList();

		for (int i = 0; i < selectedRowIndicies.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndicies[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = theAutomatonContainer.getAutomatonAt(orgIndex);
				selectedAutomata.add(currAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Trying to get an automaton that does not exist. Index: " + i);
			}
		}

		return selectedAutomata;
	}

	public Automata getSelectedAutomata()
	{
		int[] selectedRowIndicies = theAutomatonTable.getSelectedRows();
		Automata selectedAutomata = new Automata();

		for (int i = 0; i < selectedRowIndicies.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndicies[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = theAutomatonContainer.getAutomatonAt(orgIndex);
				selectedAutomata.addAutomaton(currAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Trying to get an automaton that does not exist. Index: " + i);
			}
		}

		return selectedAutomata;
	}

	// File.Open action performed
	public void fileOpen(JFrame parent)
	{
		JFileChooser fileOpener = FileDialogs.getXMLFileImporter();
		if (fileOpener.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						openFile(currFiles[i]);
					}
				}
			}
			repaint();
			theAutomatonTable.repaint();
       	}
    }

	public void fileImportDesco()
	{
		/*
		JFileChooser fileOpener = FileDialogs.getDescoFileImporter();
		if (fileOpener.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						importDescoFile(currFiles[i]);
					}
				}
			}
			repaint();
			theAutomatonTable.repaint();
       	}
       	*/
    }

	public void fileImportValid()
	{
		JFileChooser fileOpener = FileDialogs.getVALIDFileImporter();
		if (fileOpener.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						importValidFile(currFiles[i]);
					}
				}
			}
			repaint();
			theAutomatonTable.repaint();
       	}
    }

	public void configurePreferences_actionPerformed(ActionEvent e)
	{
		if (thePreferencesDialog == null)
		{
			thePreferencesDialog = new PreferencesDialog(this);
		}
		thePreferencesDialog.setVisible(true);
    }

    // File.Exit action performed
    public void fileExit()
    {
        System.exit(0);
    }

    // Tools.AutomataEditor
    public void toolsAutomataEditor()
    {
        theAutomatonContainer.getAutomataEditor();
    }

    // selectAll action performed
    public void selectAll_actionPerformed(ActionEvent e)
    {
		theAutomatonTable.selectAll();
    }


    // Automaton.Status action performed
    public void automatonStatus_actionPerformed(ActionEvent e)
    {
		thisCategory.info("Number of automata: " + theAutomatonContainer.getSize());
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			StringBuffer statusStr = new StringBuffer();
			statusStr.append("Status for automaton: " + currAutomaton.getName());
			statusStr.append("\n\tNumber of states: " + currAutomaton.nbrOfStates());
			statusStr.append("\n\tNumber of events: " + currAutomaton.nbrOfEvents());
			statusStr.append("\n\tNumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
			statusStr.append("\n\tNumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());
			int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();
			if (acceptingAndForbiddenStates > 0)
			{
				statusStr.append("\n\tNumber of accepting and forbidden states: " + acceptingAndForbiddenStates);
			}
			thisCategory.info(statusStr.toString());
		}
    }

    // Automaton.View action performed
    public void automatonView_actionPerformed(ActionEvent e)
    {
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			int maxNbrOfStates = WorkbenchProperties.getDotMaxNbrOfStatesWithoutWarning();
			if  (maxNbrOfStates < currAutomaton.nbrOfStates())
			{
				StringBuffer msg = new StringBuffer();
				msg.append(currAutomatonName + " has " + currAutomaton.nbrOfStates() + " states. ");
				msg.append("It is not recommended to display an automaton with more than " + maxNbrOfStates + " states.\n");
				msg.append("Do you want to abort viewing?");
				int res = JOptionPane.showOptionDialog(
													   this, msg, "Warning",
													   JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,
													   null, null, null
													   );
				if (res == 0)
				{ // Abort - YES
					return;
				}
			}
			if (!currAutomaton.hasInitialState())
			{
				JOptionPane.showMessageDialog(this, "The automaton does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try
			{
				AutomatonViewer viewer = theAutomatonContainer.getAutomatonViewer(currAutomatonName);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonViewer. Automaton: " + currAutomaton.getName());
				return;
			}

		}
    }

	// Automaton.Explore action performed
    public void automatonExplore_actionPerformed(ActionEvent e)
    {
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		/*
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				try
				{
					AutomatonExplorer explorer = theAutomatonContainer.getAutomatonExplorer(currAutomatonName);
				}
				catch (Exception ex)
				{
					thisCategory.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
				}
			}
		}
		*/

		if (selectedAutomata.size() == 1)
		{   // One automata selected
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			try
			{
				AutomatonExplorer explorer = theAutomatonContainer.getAutomatonExplorer(currAutomatonName);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
			}
		}
		else
		{   // Many automata selected
			Automata currAutomata = new Automata();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				String currAutomatonName = currAutomaton.getName();
				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(this,
												  "The automaton " + currAutomatonName + " does not have an initial state!",
												  "Alert",
												  JOptionPane.ERROR_MESSAGE);
					return;
				}
				currAutomata.addAutomaton(currAutomaton);
			}

			try
			{
			    AutomataExplorer explorer = new AutomataExplorer(currAutomata);
				explorer.setVisible(true);
				explorer.initialize();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataExplorer.");
			}
		}
	}

	// Automaton.Alphabet action performed
	public void automatonAlphabet_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				AlphabetViewer viewer = theAutomatonContainer.getAlphabetViewer(currAutomatonName);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AlphabetViewer. Automaton: " + currAutomaton.getName());
			}
		}
    }

	// Automaton.Synchronize action performed
	// Threaded version
	public void automataSynchronize_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();

 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(this, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String newAutomatonName = getNewAutomatonName("Please enter a new name", "");

		if (newAutomatonName == null)
		{
			return;
		}

		Automata currAutomata = new Automata();

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this,
					"The automaton " + currAutomatonName + " does not have an initial state!",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		SynchronizationOptions syncOptions;
		try
		{
			syncOptions = new SynchronizationOptions(
				WorkbenchProperties.syncNbrOfExecuters(),
				SynchronizationType.Prioritized,
				WorkbenchProperties.syncInitialHashtableSize(),
				WorkbenchProperties.syncExpandHashtable(),
				WorkbenchProperties.syncForbidUncontrollableStates(),
				WorkbenchProperties.syncExpandForbiddenStates(),
				false,
				false,
				true,
				WorkbenchProperties.verboseMode()
			);
		}
		catch (Exception ex)
		{
				JOptionPane.showMessageDialog(this,
					"Invalid synchronizationOptions",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
		}

		AutomataSynchronizerWorker worker = new AutomataSynchronizerWorker(this, currAutomata, newAutomatonName, syncOptions);
	}

	// Automaton.Verify action performed
	// Threaded version
	public void automataVerify_actionPerformed(ActionEvent e)
	{
		VerificationOptions verificationOptions = new VerificationOptions();
		VerificationDialog verificationDialog = new VerificationDialog(this, verificationOptions);
		verificationDialog.show();

		if (!verificationOptions.getDialogOK())
			return;

		Collection selectedAutomata = getSelectedAutomataAsCollection();

 		if (selectedAutomata.size() < 1)
 		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata currAutomata = new Automata();

		// The Automata must have initial states.
		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this,
					"The automaton " + currAutomatonName + " does not have an initial state!",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		SynchronizationOptions syncOptions;
		try
		{
			syncOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this,
										  "Invalid synchronizationOptions",
										  "Alert",
										  JOptionPane.ERROR_MESSAGE);
			return;
		}

		AutomataVerificationWorker worker = new AutomataVerificationWorker(this, currAutomata, syncOptions, verificationOptions);
	}

/*
	// Automaton.Synchronize action performed
	public void automataSynchronize_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();

 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(null, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String newAutomatonName = getNewAutomatonName("Please enter a new name");

		if (newAutomatonName == null)
		{
			return;
		}

		Automata currAutomata = new Automata();

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(null, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}
		  AutomataSynchronizer theSynchronizer;
		  try
		  {
		  theSynchronizer =
		  new AutomataSynchronizer(currAutomata);

		  theSynchronizer.execute();
		  }
		  catch (Exception ex)
		  {
		  thisCategory.error("Exception in AutomatonSynchronizer");
		  return;
		  }
		  Automaton theAutomaton;
		  try
		  {
		  theAutomaton = theSynchronizer.getAutomaton();
		  }
		  catch (Exception ex)
		  {
		  thisCategory.error("Exception in AutomatonSynchronizer while getting the automaton");
		  return;
		  }
		  theAutomaton.setName(newAutomatonName);
		  try
		  {
		  theAutomatonContainer.add(theAutomaton);
		  }
		  catch (Exception ex)
		  {
		  thisCategory.error("Could not add the new automaton after synchronization");
		  return;
		  }

	}
*/

	// Automaton.ControllabilityCheck action performed
	/**
	 * @deprecated use AutomataVerifier instead.
	 */
	public void automataControllabilityCheck_actionPerformed(ActionEvent e)
	{
		Date startDate = new Date();

		Collection selectedAutomata = getSelectedAutomataAsCollection();
		boolean isControllable = true;

 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(this, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata currAutomata = new Automata();

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		int nbrOfExecuters = WorkbenchProperties.syncNbrOfExecuters();

		AutomataControllabilityCheck theControllabilityCheck;
		try
		{
			SynchronizationOptions syncOptions = new SynchronizationOptions();
			theControllabilityCheck =
		    	new AutomataControllabilityCheck(currAutomata, syncOptions);
			isControllable = theControllabilityCheck.execute();
		}
		catch (Exception ex)
		{
			thisCategory.error("Exception in AutomatonControllabilityCheck");
			return;
		}

		Date endDate = new Date();
		thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");

		if (isControllable)
		{
			JOptionPane.showMessageDialog(this, "The automata is controllable!", "Good news", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "The automata is not controllable!", "Bad news", JOptionPane.INFORMATION_MESSAGE);
		}
	 }


	/**
	 * @deprecated use AutomataVerifier instead.
	 */
	public void automataFastControllabilityCheck_actionPerformed(ActionEvent e)
	{
		Date startDate = new Date();
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		boolean isControllable = true;

 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(this, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata currAutomata = new Automata();

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		int nbrOfExecuters = WorkbenchProperties.syncNbrOfExecuters();

		AutomataFastControllabilityCheck theFastControllabilityCheck;
		try
		{
			SynchronizationOptions syncOptions = new SynchronizationOptions();
			theFastControllabilityCheck =
		    	new AutomataFastControllabilityCheck(currAutomata, syncOptions);
			isControllable = theFastControllabilityCheck.execute();
		}
		catch (Exception ex)
		{
			thisCategory.error("Exception in AutomataFastControllabilityCheck." + ex);
			return;
		}

		Date endDate = new Date();
		thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");

		if (isControllable)
		{
			JOptionPane.showMessageDialog(this, "The automata is controllable!", "Good news", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "The automata is NOT controllable!", "Bad news", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/*
	// Automaton.PairwiseCheck action performed
	public void automataPairwiseCheck_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		boolean existsPlant = false;
		boolean existsSupervisor = false;

		// Select at least two automata
 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(this, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata currAutomata = new Automata();

		// The automata must have an initial state
		// At least one plant and one supervisor among the automata
		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getType() == AutomatonType.Plant)
				existsPlant = true;
			if ((currAutomaton.getType() == AutomatonType.Supervisor) ||
				(currAutomaton.getType() == AutomatonType.Specification))
				existsSupervisor = true;
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		if (!existsPlant || !existsSupervisor)
		{
			JOptionPane.showMessageDialog(this, "At least one plant and one specification/supervisor must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try
		{
			SynchronizationOptions syncOptions = new SynchronizationOptions();
			AutomataPairwiseCheck thePairwiseCheck =
		    new AutomataPairwiseCheck(currAutomata, syncOptions);
			thePairwiseCheck.execute();
		}
		catch (Exception ex)
		{
			thisCategory.error("Exception in AutomatonPairwiseCheck");
			return;
		}

   	}
	*/

	// Automaton.LanguageInclusionCheck action performed
	/**
	 * @deprecated use AutomataVerifier instead.
	 */
	public void languageInclusionCheck_actionPerformed(ActionEvent e)
	{
		Date startDate = new Date();
		// LinkedList selectedAutomata = (LinkedList) getSelectedAutomataAsCollection();
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		boolean isIncluded = false;

		// Select at least one automaton
 		if (selectedAutomata.size() < 1)
 		{
			JOptionPane.showMessageDialog(this, "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

 		if (selectedAutomata.size() == theAutomatonContainer.getSize())
 		{
			JOptionPane.showMessageDialog(this, "You have selected all automata and that is bad.", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata AutomataA = new Automata();
		Automata AutomataB = new Automata();
		Automaton currAutomaton;
		String currAutomatonName;

		// The automata must have an initial state
		// Put selected automata in automataA and unselected in automataB
		for (int i = 0; i < theAutomatonContainer.getSize(); i++)
		{
			try
			{
				currAutomaton = theAutomatonContainer.getAutomatonAt(i);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonContainer.");
				return;
			}
		    currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (selectedAutomata.contains(currAutomaton))
				AutomataB.addAutomaton(currAutomaton);
			else
				AutomataA.addAutomaton(currAutomaton);
		}

		try
		{
			SynchronizationOptions syncOptions = new SynchronizationOptions();
			LanguageInclusionCheck languageInclusionCheck = new LanguageInclusionCheck(AutomataA, AutomataB, syncOptions);
			isIncluded = languageInclusionCheck.execute();
		}
		catch (Exception ex)
		{
			thisCategory.error("Exception in LanguageInclusionCheck. " + ex);
			return;
		}

		Date endDate = new Date();
		thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");

		if (isIncluded)
		{
			JOptionPane.showMessageDialog(this, "The language of the unselected automata is included in the language of the selected automata.", "Good news", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "The language of the unselected automata is NOT included in the language of the selected automata.", "Bad news", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// Automaton.Synthesize action performed
	public void automataSynthesize_actionPerformed(ActionEvent e)
	{
		SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
		SynthesizerDialog synthesizerDialog = new SynthesizerDialog(this, synthesizerOptions);
		synthesizerDialog.show();

		if (!synthesizerOptions.getDialogOK())
			return;

		Date startDate = new Date();
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (selectedAutomata.size() > 1)
		{
			SynchronizationOptions syncOptions;
			try
			{
				syncOptions = new SynchronizationOptions(
					WorkbenchProperties.syncNbrOfExecuters(),
					SynchronizationType.Prioritized,
					WorkbenchProperties.syncInitialHashtableSize(),
					WorkbenchProperties.syncExpandHashtable(),
					true, // WorkbenchProperties.syncForbidUncontrollableStates(),
					WorkbenchProperties.syncExpandForbiddenStates(),
					false,
					false,
					true,
					WorkbenchProperties.verboseMode()
					);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this,
											  "Invalid synchronizationOptions",
											  "Alert",
											  JOptionPane.ERROR_MESSAGE);
				return;
			}

			Automata currAutomata = new Automata();
			Iterator autIt = selectedAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				String currAutomatonName = currAutomaton.getName();
				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
					return;
				}
				currAutomata.addAutomaton(currAutomaton);
			}

			AutomataSynthesizer synthesizer = new AutomataSynthesizer(this, currAutomata, syncOptions, synthesizerOptions);
			try
			{
				synthesizer.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataSynthesizer: " + e);
			}
		}
		else
		{
			Iterator autIt = selectedAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				try
				{
					AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(currAutomaton);
					if (synthesizerOptions.getSynthesisType() == 0)      // Controllable
						synthesizer.synthesizeControllable();
					else if (synthesizerOptions.getSynthesisType() == 1) // Non-blocking
						thisCategory.error("Option not implemented...");
					else if (synthesizerOptions.getSynthesisType() == 2) // Both
						synthesizer.synthesize();
					else
						thisCategory.error("Unavailable option chosen.");
					if (synthesizerOptions.getPurge())
					{
						AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);
						automatonPurge.execute();
					}
				}
				catch (Exception ex)
				{
					thisCategory.error("Exception in AutomatonSynthesizer. Automaton: " + currAutomaton.getName());
				}
			}
		}
		Date endDate = new Date();
		thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
	}

	// Automaton.Purge action performed
	public void automataPurge_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);
			try
			{
				automatonPurge.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automaton.AllAccepting action performed
	public void automataAllAccepting_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			AutomatonAllAccepting allAccepting = new AutomatonAllAccepting(currAutomaton);
			try
			{
				allAccepting.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataAllAccepting. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automaton.Complement action performed
	public void automataComplement_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "_c");

			if (newAutomatonName == null)
			{
				return;
			}

			try
			{
				AutomatonComplement automataComplement = new AutomatonComplement(currAutomaton);
				Automaton newAutomaton = automataComplement.execute();
				newAutomaton.setName(newAutomatonName);
				theAutomatonContainer.add(newAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automaton.Extend action performed
	public void automataExtend_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = getNewAutomatonName("Please enter a new name", "");

			if (newAutomatonName == null)
			{
				return;
			}

			int k = getIntegerInDialogWindow("Select k");
			AutomataExtender extender = new AutomataExtender(currAutomaton);
			extender.setK(k);
			try
			{
				extender.execute();
				Automaton newAutomaton = extender.getNewAutomaton();
				newAutomaton.setName(newAutomatonName);
				theAutomatonContainer.add(newAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automaton.RemovePass action performed
	public void automataRemovePass_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				RemovePassEvent.execute(currAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataRemovePass. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automata.AddSelfLoopArcs action performed
	public void automataAddSelfLoopArcs_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				AddSelfArcs.execute(currAutomaton, true);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomataAddSelfLoopArcs. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automata.RemoveSelfLoopArcs action performed
	public void automataRemoveSelfLoopArcs_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				RemoveSelfArcs.execute(currAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in RemoveSelfArcs. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automata.AlphabetNormalize action performed
	public void normalizeAlphabet_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				AlphabetNormalize alphabetNormalize = new AlphabetNormalize(currAutomaton);
				alphabetNormalize.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName());
				thisCategory.error(ex);
				ex.printStackTrace();
			}
		}
	}

	// Automaton.Minimization action performed
	public void automatonMinimize_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = getNewAutomatonName("Please enter a new name", "");

			if (newAutomatonName == null)
			{
				return;
			}

			try
			{
				AutomatonMinimizer autMinimizer = new AutomatonMinimizer(currAutomaton);
				Automaton newAutomaton = autMinimizer.getMinimizedAutomaton(true);
				newAutomaton.setName(newAutomatonName);
				theAutomatonContainer.add(newAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
			}
		}
	}

     // Automata.AlphabetAnalyzer action performed
     public void alphabetAnalyzer_actionPerformed(ActionEvent e)
     {
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() >= 2)
		{
			Iterator autIt = selectedAutomata.iterator();
			Automata currAutomata = new Automata();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				currAutomata.addAutomaton(currAutomaton);
			}
			AlphabetAnalyzer theAnalyzer =
				new AlphabetAnalyzer(currAutomata);

			try
			{
				theAnalyzer.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AlphabetAnalyzer");
			}
		}
 		else
 		{
			JOptionPane.showMessageDialog(this, "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
		}
	}


	//** MF ** why should it be public?
	private void findStates_action(ActionEvent e)
	{
		FindStates find_states = new FindStates(getSelectedAutomata());
		try
		{
			find_states.execute();
		}
		catch(Exception excp)
		{
			thisCategory.error(excp.toString());
		}
			
	}
	
	// Automaton.Copy action performed
	public void automataCopy_actionPerformed(ActionEvent e)
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "(2)");
			if (newAutomatonName == null)
				return;
			try
			{
				Automaton newAutomaton = new Automaton(currAutomaton);
				newAutomaton.setName(newAutomatonName);
				theAutomatonContainer.add(newAutomaton);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception while copying the automaton");
			}
		}
	}

	// Automaton.Delete action performed
	public void automataDelete_actionPerformed()
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				theAutomatonContainer.remove(currAutomatonName);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception while removing " + currAutomatonName);
			}
		}
		if (theAutomatonContainer.getSize() == 0)
		{
			theAutomatonContainer.setProjectFile(null);
		}
		theAutomatonTable.clearSelection();
	}


	// Automaton.Delete action performed
	public void automataDeleteAll_actionPerformed(ActionEvent e)
	{
		theAutomatonContainer.clear();
		theAutomatonTable.clearSelection();
		theAutomatonContainer.setProjectFile(null);
	}

	// Automaton.Rename action performed
	public void automataRename_actionPerformed()
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				String newName = getNewAutomatonName("Enter a new name for " + currAutomatonName, currAutomatonName);
				if (newName != null)
				{
					theAutomatonContainer.rename(currAutomaton, newName);
				}
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception while renaming the automaton " + currAutomatonName, ex);
			}
		}
	}

	// File.Save action performed
	public void fileSave()
	{
		File currFile = theAutomatonContainer.getProjectFile();
		if (currFile == null)
		{
			fileSaveAs();
			return;
		}

		Automata currAutomata = theAutomatonContainer.getAutomata();

		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					AutomataToXml exporter = new AutomataToXml(currAutomata);
					exporter.serialize(currFile.getAbsolutePath());
				}
				catch (Exception ex)
				{
					thisCategory.error("Exception while saveAs " + currFile.getAbsolutePath());
				}
			}
		}
	}


	// File.SaveAs action performed
	public void fileSaveAs()
	{
		JFileChooser fileSaveAs = FileDialogs.getXMLFileSaveAs();

		String projectName = theAutomatonContainer.getProjectName();
		if (projectName != null)
		{
			File currDirectory = fileSaveAs.getCurrentDirectory();
			fileSaveAs.setSelectedFile(new File(currDirectory, projectName + ".xml"));
		}

		if (fileSaveAs.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileSaveAs.getSelectedFile();
			if (currFile != null)
			{
				theAutomatonContainer.setProjectFile(currFile);
				fileSave();
			}
		}
	}

	public void fileExportDesco()
	{
		automataExport();
	}

	public void fileExportTCT()
	{
		automataExport();
	}

	public void fileExportUMDES()
	{
		automataExport();
	}

	public void fileExportValid()
	{
		automataExport();
	}

	public void fileExportDot()
	{
		automataExport();
	}

	public void fileExportSupremica()
	{
		automataExport();
	}

	// Automaton.Export action performed
	public void automataExport()
	{
		Collection selectedAutomata = getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(this, "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String xmlString = "xml";
		String dotString = "dot";
		String dsxString = "dsx";

		Object[] possibleValues = { xmlString, dotString, dsxString };
		Object selectedValue = JOptionPane.showInputDialog(
			this, "Export as", "Input", JOptionPane.INFORMATION_MESSAGE,
			null, possibleValues, possibleValues[0]);

		if (selectedValue == null)
			return;

		int exportMode = -1;
		if (selectedValue == xmlString)
		{
			exportMode = 1;
		}
		else if (selectedValue == dotString)
		{
			exportMode = 2;
		}
		else if (selectedValue == dsxString)
		{
			exportMode = 3;
		}
		else
		{
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			JFileChooser fileExporter = null;
			if (exportMode == 1)
			{
				fileExporter = FileDialogs.getXMLFileExporter();
			}
			else if (exportMode == 2)
			{
				fileExporter = FileDialogs.getDOTFileExporter();
			}
			else if (exportMode == 3)
			{
				fileExporter = FileDialogs.getDSXFileExporter();
			}
			else
			{
				return;
			}
			if (fileExporter.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				File currFile = fileExporter.getSelectedFile();
				if (currFile != null)
				{
					if (!currFile.isDirectory())
					{
						try
						{
							if (exportMode == 1)
							{
								Automata currAutomata = new Automata();
								currAutomata.addAutomaton(currAutomaton);
								AutomataToXml exporter = new AutomataToXml(currAutomata);
								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == 2)
							{
								AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == 3)
							{
								AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
								exporter.serialize(currFile.getAbsolutePath());
							}
						}
						catch (Exception ex)
						{
							thisCategory.error("Exception while exporting " + currFile.getAbsolutePath());
						}
					}
				}
			}
		}
	}

	public void renameProject()
	{
		String newName = getNewProjectName();
		if (newName != null)
		{
			theAutomatonContainer.setProjectName(newName);
			theAutomatonContainer.setProjectFile(null);
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


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            fileExit();
        }
    }

	public String getNewProjectName()
	{
		String msg = "Enter new project name";

		boolean finished = false;
		String newName = "";

		while (!finished)
		{
			newName = JOptionPane.showInputDialog(this, msg);
			if (newName == null)
			{
				return null;
			}
			else if (newName.equals(""))
			{
				JOptionPane.showMessageDialog(this, "An empty name is not allowed", "alert", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				finished = true;
			}
		}
		return newName;
	}

	public String getNewAutomatonName(String msg, String nameSuggestion)
	{
		boolean finished = false;
		String newName = "";

		while (!finished)
		{
			newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name",
														   JOptionPane.QUESTION_MESSAGE,
														   null, null, nameSuggestion);
			if (newName == null)
			{
				return null;
			}
			else if (newName.equals(""))
			{
				JOptionPane.showMessageDialog(this, "An empty name is not allowed", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else if (theAutomatonContainer.containsAutomaton(newName))
			{
				JOptionPane.showMessageDialog(this, newName + " already exists", "Alert", JOptionPane.ERROR_MESSAGE);
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
		openAutomataXMLFile(file);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting())
		{
		}
	}

	public void tableChanged(TableModelEvent e)
	{
		theAutomatonTable.revalidate();
	}

	public void openAutomataXMLFile(File file)
	{
		int nbrOfAutomataBeforeOpening = theAutomatonContainer.getSize();

		thisCategory.info("Opening " + file.getAbsolutePath() + " ...");
		int nbrOfAddedAutomata = 0;
		try
		{
			Automata currAutomata = AutomataBuildFromXml.build(file);

			if (nbrOfAutomataBeforeOpening == 0)
			{
				String projectName = currAutomata.getName();
				if (projectName != null)
				{
					theAutomatonContainer.setProjectName(projectName);
					thisCategory.info("Project name changed to \"" + projectName + "\"");
					theAutomatonContainer.updateFrameTitles();
				}
			}

			Iterator autIt = currAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				boolean add = true;

				// Force the user to enter a new name if the name is ""
				// Note that a null name is not allowed by AutomataBuildFromXml
				if (currAutomaton.getName().equals(""))
				{
					String autName = getNewAutomatonName("Enter a new name", "");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}

				if (theAutomatonContainer.containsAutomaton(currAutomaton.getName()))
				{
					String autName = currAutomaton.getName();

					JOptionPane.showMessageDialog(this, autName + " already exists", "Alert", JOptionPane.ERROR_MESSAGE);

					autName = getNewAutomatonName("Enter a new name", autName + "(2)");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}
				if (add)
				{
					nbrOfAddedAutomata++;
					theAutomatonContainer.add(currAutomaton);
				}
			}
		}
		catch (Exception e)
		{
			thisCategory.error("Error while opening " + file.getAbsolutePath() + " " + e.getMessage());
			return;
		}
		thisCategory.info("Successfully opened " + nbrOfAddedAutomata + " automata.");

		if (nbrOfAutomataBeforeOpening > 0)
		{
			File projectFile = theAutomatonContainer.getProjectFile();
			if (projectFile != null)
			{
				theAutomatonContainer.setProjectFile(null);
			}
		}
		else
		{
			theAutomatonContainer.setProjectFile(file);
		}
	}

	public void importValidFile(File file)
	{
		thisCategory.info("Importing " + file.getAbsolutePath() + " ...");
		int nbrOfAddedAutomata = 0;

		try
		{
  			Automata currAutomata = AutomataBuildFromVALID.build(file);
			Iterator autIt = currAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				boolean add = true;
				if (currAutomaton.getName().equals(""))
				{
					String autName = getNewAutomatonName("Enter a new name", "");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}

				if (theAutomatonContainer.containsAutomaton(currAutomaton.getName()))
				{
					String autName = currAutomaton.getName();

					JOptionPane.showMessageDialog(this, autName + " already exists", "Alert",
												  JOptionPane.ERROR_MESSAGE);

					autName = getNewAutomatonName("Enter a new name", autName + "(2)");
					if (autName == null)
					{
						add = false; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}
				if (add)
				{
					nbrOfAddedAutomata++;
					theAutomatonContainer.add(currAutomaton);
				}
			}
		}
		catch (Exception e)
		{
			thisCategory.error("Error while importing " + file.getAbsolutePath() + " " + e.getMessage());
			return;
		}
		thisCategory.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
	}

	public AutomatonContainer getAutomatonContainer()
	{
		return theAutomatonContainer;
	}
}

class TypeCellEditor
	implements CellEditorListener
{
	private JTable theTable;
	private TableSorter theTableSorter;
	private JComboBox automatonTypeCombo;
	private AutomatonContainer theAutomatonContainer;
	private static Category thisCategory = LogDisplay.createCategory(TypeCellEditor.class.getName());

	public TypeCellEditor(JTable theTable, TableSorter theTableSorter, AutomatonContainer theAutomatonContainer)
	{
		this.theTable = theTable;
		this.theAutomatonContainer = theAutomatonContainer;
		this.theTableSorter = theTableSorter;

		automatonTypeCombo = new JComboBox();
		Iterator typeIt = AutomatonType.iterator();
		while (typeIt.hasNext())
		{
			automatonTypeCombo.addItem(typeIt.next());
		}
		TableColumnModel columnModel = theTable.getColumnModel();
		TableColumn typeColumn = columnModel.getColumn(Supremica.TABLE_TYPE_COLUMN);
		DefaultCellEditor cellEditor = new DefaultCellEditor(automatonTypeCombo);
		cellEditor.setClickCountToStart(2);
		typeColumn.setCellEditor(cellEditor);
		cellEditor.addCellEditorListener(this);
	}

	public void editingCanceled(ChangeEvent e)
	{
	}

	public void editingStopped(ChangeEvent e)
	{
		if (automatonTypeCombo.getSelectedIndex() >= 0)
		{
			AutomatonType selectedValue = (AutomatonType)automatonTypeCombo.getSelectedItem();

			if (selectedValue != null)
			{
				int selectedRow = theTable.getSelectedRow();
				int orgRow = theTableSorter.getOriginalRowIndex(selectedRow);
				if (selectedRow >= 0)
				{
					Automaton currAutomaton = null;
					try
					{
						currAutomaton = theAutomatonContainer.getAutomatonAt(orgRow);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						System.exit(0);
					}
					currAutomaton.setType(selectedValue);
				}
			}
		}
	}
}


