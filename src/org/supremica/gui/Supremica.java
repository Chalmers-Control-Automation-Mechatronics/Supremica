
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
import org.supremica.log.*;
import javax.help.*;
import org.supremica.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.templates.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.*;
import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.gui.animators.scenebeans.*;

public class Supremica
	extends JFrame
	implements TableModelListener, Gui, VisualProjectContainerListener
{
	private final static InterfaceManager theInterfaceManager = InterfaceManager.getInstance();
	private static Logger logger = LoggerFactory.createLogger(Supremica.class);
	private LogDisplay theLogDisplay = LogDisplay.getInstance();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();
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

		VisualProject theVisualProject = new VisualProject("Single Visual Project");

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

		theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

		menuHandler = new MenuHandler(

		/*
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
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// This code used to be in the popup menu -------------
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

			setTitle("Supremica - " + projectName);
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
		toolBar.setRollover(true);
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

		initMenubar();
		initToolbar();
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
		class NewFromTemplateHandler
			implements ActionListener
		{
			private TemplateItem item = null;

			public NewFromTemplateHandler(TemplateItem item)
			{
				this.item = item;
			}

			public void actionPerformed(ActionEvent e)
			{
				ActionMan.fileNewFromTemplate(getGui(), item);
			}
		}

		class ToolsAnimationHandler
			implements ActionListener
		{
			private AnimationItem item = null;

			public ToolsAnimationHandler(AnimationItem item)
			{
				this.item = item;
			}

			public void actionPerformed(ActionEvent e)
			{
				ActionMan.animator(getGui(), item);
			}
		}

		boolean separatorNeeded = false;

		setJMenuBar(menuBar);

		// File
		JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		// File.New
		JMenuItem menuFileNew = new JMenuItem();

		menuFileNew.setText("New...");
		menuFile.add(menuFileNew);
		menuFileNew.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.fileNew(getGui());
			}
		});

		// File.NewFromTemplate
		JMenu menuFileNewFromTemplate = new JMenu();

		menuFileNewFromTemplate.setText("New From Template");
		menuFile.add(menuFileNewFromTemplate);

		ExampleTemplates exTempl = ExampleTemplates.getInstance();

		for (Iterator groupIt = exTempl.iterator(); groupIt.hasNext(); )
		{
			TemplateGroup currGroup = (TemplateGroup) groupIt.next();
			JMenu menuFileNewFromTemplateGroup = new JMenu();

			menuFileNewFromTemplateGroup.setText(currGroup.getDescription());
			menuFileNewFromTemplate.add(menuFileNewFromTemplateGroup);

			for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
			{
				TemplateItem currItem = (TemplateItem) itemIt.next();
				JMenuItem menuItem = new JMenuItem();

				menuItem.setText(currItem.getDescription());
				menuFileNewFromTemplateGroup.add(menuItem);
				menuItem.addActionListener(new NewFromTemplateHandler(currItem));
			}
		}

		if (SupremicaProperties.fileAllowOpen())
		{

			// File.Open
			JMenuItem menuFileOpen = new JMenuItem();

			menuFileOpen.setText("Open...");
			menuFile.add(menuFileOpen);
			menuFileOpen.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileOpen(getGui());
				}
			});

			separatorNeeded = true;
		}

		if (SupremicaProperties.fileAllowSave())
		{

			// File.Save
			JMenuItem menuFileSave = new JMenuItem();

			menuFileSave.setText("Save");
			menuFileSave.setMnemonic(KeyEvent.VK_S);
			menuFile.add(menuFileSave);
			menuFileSave.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSave(getGui());
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
					ActionMan.fileSaveAs(getGui());
				}
			});

			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();

			separatorNeeded = false;
		}

		if (SupremicaProperties.fileAllowImport())
		{

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
					ActionMan.fileImportDesco(getGui());
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

					// ActionMan.fileImportTCT(getGui());
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

					// ActionMan.fileImportUMDES(this);
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
					ActionMan.fileImportValid(getGui());
				}
			});

			separatorNeeded = true;
		}

		if (SupremicaProperties.fileAllowExport())
		{

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
					ActionMan.fileExportDesco(getGui());
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

					// ActionMan.fileExportTCT(getGui());
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

					// ActionMan.fileExportUMDES(getGui());
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
					ActionMan.fileExportValid(getGui());
				}
			});

			// ++ ARASH:
			// File.Export.RCP
			JMenuItem menuFileExportRCP = new JMenuItem();

			menuFileExportRCP.setText("To RCP...");
			menuFileExport.add(menuFileExportRCP);
			menuFileExportRCP.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExportRCP(getGui());
				}
			});

			// -- ARASH
			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();

			separatorNeeded = false;
		}

		if (SupremicaProperties.generalUseSecurity())
		{

			// File.Login
			JMenuItem menuFileLogin = new JMenuItem();

			menuFileLogin.setText("Login");
			menuFile.add(menuFileLogin);
			menuFileLogin.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileLogin(getGui());
				}
			});

			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();

			separatorNeeded = false;
		}

		if (SupremicaProperties.fileAllowQuit())
		{

			// File.Exit
			JMenuItem menuFileExit = new JMenuItem();

			menuFileExit.setText("Exit");
			menuFile.add(menuFileExit);
			menuFileExit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExit(getGui());
				}
			});
		}
		else
		{

			// File.Close
			JMenuItem menuFileExit = new JMenuItem();

			menuFileExit.setText("Close");
			menuFile.add(menuFileExit);
			menuFileExit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileClose(getGui());
				}
			});
		}

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

		// Tools
		JMenu menuTools = new JMenu();

		menuTools.setText("Tools");
		menuTools.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menuTools);

		// Tools.TestCases
		JMenuItem test_cases = new JMenuItem();

		test_cases.setText("Test Cases...");
		menuTools.add(test_cases);
		test_cases.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ActionMan.testCases(getGui());
				}
				catch (Exception excp)
				{

					// what the f*** do we do?
				}
			}
		});

		// Tools.Animations
		JMenu menuToolsAnimations = new JMenu();

		menuToolsAnimations.setText("Animations");
		menuTools.add(menuToolsAnimations);

		ExampleAnimations exAnim = ExampleAnimations.getInstance();

		for (Iterator groupIt = exAnim.iterator(); groupIt.hasNext(); )
		{
			AnimationGroup currGroup = (AnimationGroup) groupIt.next();
			JMenu menuToolsAnimationGroup = new JMenu();

			menuToolsAnimationGroup.setText(currGroup.getDescription());
			menuToolsAnimations.add(menuToolsAnimationGroup);

			for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
			{
				AnimationItem currItem = (AnimationItem) itemIt.next();
				JMenuItem menuItem = new JMenuItem();

				menuItem.setText(currItem.getDescription());
				menuToolsAnimationGroup.add(menuItem);
				menuItem.addActionListener(new ToolsAnimationHandler(currItem));
			}
		}

		// Tools.AutomataEditor
		if (SupremicaProperties.includeEditor())
		{
			menuTools.add(new JSeparator());

			JMenuItem menuToolsAutomataEditor = new JMenuItem();

			menuToolsAutomataEditor.setText("Editor...");
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

					// toolsAutomataEditor();
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

					// toolsAutomataEditor();
				}
			});

			JMenuItem menuToolsCodeGenerationSattLineSFC = new JMenuItem();

			menuToolsCodeGenerationSattLineSFC.setText("SattLine SFC...");
			menuToolsCodeGeneration.add(menuToolsCodeGenerationSattLineSFC);
			menuToolsCodeGenerationSattLineSFC.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.AutomataToSattLineSFC(getGui());
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
				ActionMan.configurePreferences_actionPerformed(getGui());
			}
		});

		// Help
		JMenu menuHelp = new JMenu();

		menuHelp.setText("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);

		// Help.Help Topics
		JMenuItem menuHelpTopics = new JMenuItem("Help Topics");

		// menuHelpTopics.setMnemonic(KeyEvent.VK_H);
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
		Insets tmpInsets = new Insets(0, 0, 0, 0);

		if (SupremicaProperties.fileAllowOpen())
		{

			// Create buttons
			JButton openButton = new JButton();

			openButton.setToolTipText("Open");

			ImageIcon open16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif"));

			openButton.setIcon(open16Img);
			openButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileOpen(getGui());
				}
			});
			openButton.setMargin(tmpInsets);
			toolBar.add(openButton, "WEST");
		}

		if (SupremicaProperties.fileAllowSave())
		{
			JButton saveButton = new JButton();

			saveButton.setToolTipText("Save");

			ImageIcon save16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Save16.gif"));

			saveButton.setIcon(save16Img);
			saveButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSave(getGui());
				}
			});

			JButton saveAsButton = new JButton();

			saveAsButton.setToolTipText("Save As");

			ImageIcon saveAs16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));

			saveAsButton.setIcon(saveAs16Img);
			saveAsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSaveAs(getGui());
				}
			});
			saveButton.setMargin(tmpInsets);
			saveAsButton.setMargin(tmpInsets);
			toolBar.add(saveButton, "WEST");
			toolBar.add(saveAsButton, "WEST");
			toolBar.addSeparator();
		}

		JButton editButton = new JButton();

		editButton.setToolTipText("Edit");

		ImageIcon edit16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Edit16.gif"));

		editButton.setIcon(edit16Img);
		editButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				toolsAutomataEditor();
			}
		});
		editButton.setMargin(tmpInsets);
		toolBar.add(editButton, "WEST");
		toolBar.addSeparator();

		JButton helpButton = new JButton();

		helpButton.setToolTipText("Help");

		ImageIcon help16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Help16.gif"));

		helpButton.setIcon(help16Img);
		helpButton.addActionListener(helpDisplayer);
		helpButton.setMargin(tmpInsets);
		toolBar.add(helpButton, "EAST");
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
	 *@return  The selectedAutomataAsCollection value
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
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedAutomata.add(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
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
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedAutomata.addAutomaton(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
			}
		}

		return selectedAutomata;
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
		catch (Exception e)
		{

			// this exception is caught while opening
			logger.error("Error while opening " + file.getAbsolutePath() + " " + e.getMessage());

			return;
		}

		int nbrOfProjectBeforeOpening = getActiveProject().getNbrOfAutomata();

		try
		{
			int nbrOfAddedProject = addAutomata(currProject);

			logger.info("Successfully opened and added " + nbrOfAddedProject + " automata.");
		}
		catch (Exception excp)
		{
			logger.error("Error adding automata " + file.getAbsolutePath() + " " + excp.getMessage());

			return;
		}

		if (nbrOfProjectBeforeOpening == 0)
		{
			String projectName = currProject.getName();

			if (projectName != null)
			{
				getActiveProject().setName(projectName);
				logger.info("Project name changed to \"" + projectName + "\"");
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

	public int addAutomata(Automata currAutomata)
	{
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

	// We need a single entry to add automata to the gui
	// Here we manage all necessary user interaction
	public boolean addAutomaton(Automaton currAutomaton)
	{

		// Force the user to enter a new name if the name is ""
		if (currAutomaton.getName().equals(""))
		{
			String autName = getNewAutomatonName("Enter a new name", "");

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
			logger.info("Name conflict - " + autName + " does already exist. Changed name of new " + autName + " to " + newName + ".");
		}

		try
		{    // throws Exception if the automaton already exists

			// logger.debug("Supremica.addAutomaton");
			getActiveProject().addAutomaton(currAutomaton);
		}
		catch (Exception excp)
		{

			// should never occur, we test for this condition already
			logger.error("Error while adding: " + excp.getMessage());
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
