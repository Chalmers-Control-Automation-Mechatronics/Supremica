
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
package org.supremica.gui.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.HashMap;
import com.nwoods.jgo.*;
import com.nwoods.jgo.layout.JGoNetwork;
import java.beans.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.log.*;

public class AutomataEditor
	extends JFrame
	implements TableModelListener
{
	private static Logger logger = LoggerFactory.createLogger(AutomataEditor.class);

	private VisualProject theVisualProject = null;
	private JPanel contentPane;
	private JToolBar toolBar = new JToolBar();
	private JTable theAutomatonTable;
	private JScrollPane theAutomatonTableScrollPane;
	private TableModel lightTableModel;
	private JSplitPane splitPaneHorizontal;
	private EditorActions theActions;

	public AutomataEditor(VisualProject theVisualProject)
	{
		this.theVisualProject = theVisualProject;
		theActions = new EditorActions(this);

		initMenus();
		initToolbar();

		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(toolBar, BorderLayout.NORTH);

		lightTableModel = theVisualProject.getLightTableModel();

		lightTableModel.addTableModelListener(this);

		theAutomatonTable = new JTable(lightTableModel);
		theAutomatonTableScrollPane = new JScrollPane(theAutomatonTable);

		JViewport vp = theAutomatonTableScrollPane.getViewport();

		vp.setBackground(Color.white);
		theAutomatonTable.setBackground(Color.white);

		splitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, theAutomatonTableScrollPane, getDesktop());

		splitPaneHorizontal.setDividerLocation(0.2);
		contentPane.add(splitPaneHorizontal, "Center");
		contentPane.add(getStatusArea(), BorderLayout.SOUTH);
		initStatusArea();
		contentPane.validate();
		init();
	}

	void initMenus()
	{
		JMenuItem item = null;

		filemenu.setText("File");
		filemenu.setMnemonic('F');

		// Add
		item = filemenu.add(theActions.getFileAddAction());

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		item.setMnemonic('A');

		/*
		 *   AppAction FileOpenAction = new AppAction("Open", this) {
		 *     public void actionPerformed(ActionEvent e) { openDemo(); }
		 *     public boolean canAct() { return true; } };  // doesn't depend on a view
		 *
		 *   item = filemenu.add(FileOpenAction);
		 *   item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Event.CTRL_MASK));
		 *   item.setMnemonic('O');
		 *
		 *   AppAction FileCloseAction = new AppAction("Close", this) {
		 *     public void actionPerformed(ActionEvent e) { closeDemo(); } };
		 *
		 *   item = filemenu.add(FileCloseAction);
		 *   item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,Event.CTRL_MASK));
		 *   item.setMnemonic('C');
		 *
		 *   AppAction FileSaveAction = new AppAction("Save", this) {
		 *     public void actionPerformed(ActionEvent e) { saveDemo(); } };
		 *
		 *   item = filemenu.add(FileSaveAction);
		 *   item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK));
		 *   item.setMnemonic('S');
		 *
		 *   AppAction FileSaveAsAction = new AppAction("Save As", this) {
		 *     public void actionPerformed(ActionEvent e) { saveAsDemo(); } };
		 *
		 *   item = filemenu.add(FileSaveAsAction);
		 *   item.setMnemonic('A');
		 *
		 *   filemenu.addSeparator();
		 */

		// Print
		item = filemenu.add(theActions.getFilePrintAction());

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
		item.setMnemonic('P');
		filemenu.addSeparator();

		// Close
		item = filemenu.add(theActions.getFileCloseAction());

		item.setMnemonic('x');
		mainMenuBar.add(filemenu);
		editmenu.setText("Edit");
		editmenu.setMnemonic('E');

		AppAction CutAction = new AppAction("Cut", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().cut();
			}

			public boolean canAct()
			{
				return super.canAct() &&!getView().getSelection().isEmpty();
			}
		};

		item = editmenu.add(CutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		item.setMnemonic('t');

		AppAction CopyAction = new AppAction("Copy", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().copy();
			}

			public boolean canAct()
			{
				return super.canAct() &&!getView().getSelection().isEmpty();
			}
		};

		item = editmenu.add(CopyAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
		item.setMnemonic('C');

		AppAction PasteAction = new AppAction("Paste", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().paste();
			}
		};

		item = editmenu.add(PasteAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
		item.setMnemonic('P');

		AppAction DeleteAction = new AppAction("Delete", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().deleteSelection();
			}

			public boolean canAct()
			{
				return super.canAct() &&!getView().getSelection().isEmpty();
			}
		};

		item = editmenu.add(DeleteAction);

		item.setMnemonic('D');

		AppAction SelectAllAction = new AppAction("Select All", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().selectAll();
			}
		};

		item = editmenu.add(SelectAllAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
		item.setMnemonic('l');
		mainMenuBar.add(editmenu);
		viewmenu.setText("View");
		viewmenu.setMnemonic('V');

		AppAction ZoomNormalAction = new AppAction("Normal Zoom", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().zoomNormal();
			}
		};

		item = viewmenu.add(ZoomNormalAction);

		item.setMnemonic('N');

		AppAction ZoomInAction = new AppAction("Zoom In", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().zoomIn();
			}

			public boolean canAct()
			{
				return super.canAct() && (getView().getScale() < 8.0f);
			}
		};

		item = viewmenu.add(ZoomInAction);

		item.setMnemonic('I');

		AppAction ZoomOutAction = new AppAction("Zoom Out", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().zoomOut();
			}

			public boolean canAct()
			{
				return super.canAct() && (getView().getScale() > 0.13f);
			}
		};

		item = viewmenu.add(ZoomOutAction);

		item.setMnemonic('O');

		AppAction ZoomToFitAction = new AppAction("Zoom To Fit", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().zoomToFit();
			}
		};

		item = viewmenu.add(ZoomToFitAction);

		item.setMnemonic('Z');
		viewmenu.addSeparator();

		AppAction GridAction = new AppAction("Toggle Grid", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				getView().showGrid();
			}
		};

		item = viewmenu.add(GridAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
		item.setMnemonic('G');
		mainMenuBar.add(viewmenu);
		insertmenu.setText("Insert");
		insertmenu.setMnemonic('I');

		AppAction InsertNodeAction = new AppAction("Basic Node", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				nodeAction();
			}
		};

		item = insertmenu.add(InsertNodeAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK));
		item.setMnemonic('B');
		mainMenuBar.add(insertmenu);
		layoutmenu.setText("Layout");
		layoutmenu.setMnemonic('L');

		AppAction RandomLayoutAction = new AppAction("Random Layout", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				randomAction();
			}
		};

		item = layoutmenu.add(RandomLayoutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));
		item.setMnemonic('R');

		AppAction ForceLayoutAction = new AppAction("Force-Directed Layout", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				forceAction();
			}
		};

		item = layoutmenu.add(ForceLayoutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
		item.setMnemonic('F');

		AppAction LayerLayoutAction = new AppAction("Layered Digraph Layout", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				layerAction();
			}
		};

		item = layoutmenu.add(LayerLayoutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
		item.setMnemonic('L');
		mainMenuBar.add(layoutmenu);
		helpmenu.setText("Help");
		helpmenu.setMnemonic('H');

		AppAction AboutAction = new AppAction("About", this)
		{
			public void actionPerformed(ActionEvent e)
			{
				showAbout();
			}

			public boolean canAct()
			{
				return true;
			}
		};    // doesn't depend on a view

		item = helpmenu.add(AboutAction);

		item.setMnemonic('A');
		mainMenuBar.add(helpmenu);
		setJMenuBar(mainMenuBar);
	}

	public JFrame getCurrentFrame()
	{
		return this;
	}

	public void initToolbar()
	{
		// Enables stylish rollover buttons - JDK 1.4 required
		toolBar.setRollover(true);

		Insets tmpInsets = new Insets(0, 0, 0, 0);
		boolean separatorNeeded = false;
		JButton addButton = new JButton();

		addButton.setToolTipText("Add");

		ImageIcon add16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Add16.gif"));

		addButton.setIcon(add16Img);
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileAdd();
			}
		});
		addButton.setMargin(tmpInsets);
		toolBar.add(addButton, "WEST");

		separatorNeeded = true;

		if (SupremicaProperties.fileAllowOpen())
		{
			JButton openButton = new JButton();

			openButton.setToolTipText("Open");

			ImageIcon open16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif"));

			openButton.setIcon(open16Img);
			openButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fileOpen();
				}
			});
			openButton.setMargin(tmpInsets);
			toolBar.add(openButton, "WEST");

			separatorNeeded = true;
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
					fileSave();
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
					fileSaveAs();
				}
			});
			saveButton.setMargin(tmpInsets);
			saveAsButton.setMargin(tmpInsets);
			toolBar.add(saveButton, "WEST");
			toolBar.add(saveAsButton, "WEST");

			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			toolBar.addSeparator();

			separatorNeeded = true;
		}

		JButton printButton = new JButton();

		printButton.setToolTipText("Print");

		ImageIcon print16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Print16.gif"));

		printButton.setIcon(print16Img);
		printButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				filePrint();
			}
		});

		JButton cutButton = new JButton();

		cutButton.setToolTipText("Cut");

		ImageIcon cut16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Cut16.gif"));

		cutButton.setIcon(cut16Img);
		cutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton copyButton = new JButton();

		copyButton.setToolTipText("Copy");

		ImageIcon copy16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Copy16.gif"));

		copyButton.setIcon(copy16Img);
		copyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton pasteButton = new JButton();

		pasteButton.setToolTipText("Paste");

		ImageIcon paste16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Paste16.gif"));

		pasteButton.setIcon(paste16Img);
		pasteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton deleteButton = new JButton();

		deleteButton.setToolTipText("Delete");

		ImageIcon delete16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Delete16.gif"));

		deleteButton.setIcon(delete16Img);
		deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton undoButton = new JButton();

		undoButton.setToolTipText("Undo");

		ImageIcon undo16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Undo16.gif"));

		undoButton.setIcon(undo16Img);
		undoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton redoButton = new JButton();

		redoButton.setToolTipText("Redo");

		ImageIcon redo16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Redo16.gif"));

		redoButton.setIcon(redo16Img);
		redoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton zoomButton = new JButton();

		zoomButton.setToolTipText("Zoom");

		ImageIcon zoom16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif"));

		zoomButton.setIcon(zoom16Img);
		zoomButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton zoomInButton = new JButton();

		zoomInButton.setToolTipText("Zoom In");

		ImageIcon zoomIn16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomIn16.gif"));

		zoomInButton.setIcon(zoomIn16Img);
		zoomInButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton zoomOutButton = new JButton();

		zoomOutButton.setToolTipText("Zoom Out");

		ImageIcon zoomOut16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomOut16.gif"));

		zoomOutButton.setIcon(zoomOut16Img);
		zoomOutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				// fileOpen();
			}
		});

		JButton helpButton = new JButton();

		helpButton.setToolTipText("Help");

		ImageIcon help16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Help16.gif"));

		helpButton.setIcon(help16Img);

		// helpButton.addActionListener(helpDisplayer);
		printButton.setMargin(tmpInsets);
		cutButton.setMargin(tmpInsets);
		copyButton.setMargin(tmpInsets);
		pasteButton.setMargin(tmpInsets);
		deleteButton.setMargin(tmpInsets);
		undoButton.setMargin(tmpInsets);
		redoButton.setMargin(tmpInsets);
		zoomButton.setMargin(tmpInsets);
		zoomInButton.setMargin(tmpInsets);
		zoomOutButton.setMargin(tmpInsets);
		helpButton.setMargin(tmpInsets);

		// Add buttons to toolbar
		toolBar.add(printButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(cutButton, "WEST");
		toolBar.add(copyButton, "WEST");
		toolBar.add(pasteButton, "WEST");
		toolBar.add(deleteButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(undoButton, "WEST");
		toolBar.add(redoButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(zoomButton, "WEST");
		toolBar.add(zoomInButton, "WEST");
		toolBar.add(zoomOutButton, "WEST");
		toolBar.addSeparator();
		toolBar.add(helpButton, "EAST");
	}

	public void init()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Throwable t)
		{
			logger.error(t);
			t.printStackTrace();
			return;
		}

		// close the application when the main window closes
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent event)
			{

				/*
				 * Object object = event.getSource();
				 * if (object == this)
				 * {
				 *       exit();
				 * }
				 */
			}
		});
		setSize(new Dimension(800, 600));
		setIconImage(Supremica.cornerImage);

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}

		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		myDesktop.setBackground(new Color(132, 130, 130));
		setVisible(true);
		splitPaneHorizontal.setDividerLocation(0.2);
		AppAction.updateAllActions();
		theAutomatonTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int currRow = theAutomatonTable.rowAtPoint(new Point(e.getX(), e.getY()));

					if (currRow < 0)
					{
						return;
					}

					String automatonName = (String) theAutomatonTable.getValueAt(currRow, 0);

					try
					{
						AutomatonDocument theDocument = theVisualProject.getAutomatonDocument(automatonName);
						Automaton theAutomaton = theDocument.getAutomaton();
						JInternalFrame theFrame = theVisualProject.getAutomatonFrame(automatonName);

						theFrame.setVisible(true);
						theFrame.setTitle(theAutomaton.getName());

						if (theDocument.isLayoutNeeded())
						{
							randomAction();
							theDocument.setLayoutNeeded(false);
						}
					}
					catch (Exception ex)
					{
						System.err.println("Error while displaying the automaton");

						return;
					}
				}
			}

			public void mousePressed(MouseEvent e)
			{

				// This is needed for the Unix platform
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

				/*
				 *                               if (e.isPopupTrigger())
				 *                               {
				 *                                       int currRow = theAutomatonTable.rowAtPoint(new Point(e.getX(), e.getY()));
				 *                                       if (currRow < 0)
				 *                                       {
				 *                                               return;
				 *                                       }
				 *                                       if (!theAutomatonTable.isRowSelected(currRow))
				 *                                       {
				 *                                               theAutomatonTable.clearSelection();
				 *                                               theAutomatonTable.setRowSelectionInterval(currRow, currRow);
				 *                                       }
				 *                                       regionPopup = menuHandler.getDisabledPopupMenu();
				 *                                       regionPopup.show(e.getComponent(),
				 *                                                                        e.getX(), e.getY());
				 *                               }
				 */
			}
		});
	}

	/*
	 * public void start()
	 * {
	 *   // enable drag-and-drop from separate thread
	 *   new Thread(this).start();
	 * }
	 *
	 * public void run() {
	 *   newDemo();
	 * }
	 */

	/*
	 * static public void main(String args[])
	 * {
	 *   try {
	 *     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	 *
	 *     final JFrame mainFrame = new JFrame();
	 *     final AutomataEditor app = new AutomataEditor();
	 *
	 *     // close the application when the main window closes
	 *     mainFrame.addWindowListener(new WindowAdapter() {
	 *       public void windowClosing(java.awt.event.WindowEvent event) {
	 *         Object object = event.getSource();
	 *         if (object == mainFrame)
	 *           app.exit();
	 *       }
	 *     });
	 *
	 *     //mainFrame.setTitle("Supremica");
	 *     Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	 *     mainFrame.setBounds(0, 0, screensize.width, screensize.height);
	 *
	 *     Container contentPane = mainFrame.getContentPane();
	 *     contentPane.setLayout(new BorderLayout());
	 *     contentPane.add("Center", app);
	 *     contentPane.validate();
	 *
	 *     mainFrame.setVisible(true);
	 *
	 *     app.init();
	 *     app.start();
	 *   } catch (Throwable t) {
	 *     System.err.println(t);
	 *     t.printStackTrace();
	 *     System.exit(1);
	 *   }
	 *
	 *
	 * }
	 */
	void showAbout()
	{
		AboutBox dlg = new AboutBox(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();

		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	public JInternalFrame createFrame(AutomatonDocument doc)
	{
		final AutomatonView view = new AutomatonView(doc);
		final JInternalFrame frame = new JInternalFrame(doc.getName(), true, true, true);

		frame.setFrameIcon(Supremica.cornerIcon);
		view.initialize(this, frame);

		// keep track of the "current" view, even if it doesn't have focus
		// try to give focus to a view when it becomes activated
		// enable/disable all the command actions appropriately for the view
		frame.addVetoableChangeListener(new CloseListener(this, frame));
		frame.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameActivated(InternalFrameEvent e)
			{
				myCurrentView = view;

				view.requestFocus();
				AppAction.updateAllActions();
			}

			public void internalFrameDeactivated(InternalFrameEvent e) {}

			public void internalFrameOpened(InternalFrameEvent e)
			{
				view.zoomToFit();
				view.showGrid();
				view.setSnapMove(JGoGridView.NoSnap);
			}

			public void internalFrameClosing(InternalFrameEvent e) {}

			public void internalFrameClosed(InternalFrameEvent e) {}

			public void internalFrameIconified(InternalFrameEvent e) {}

			public void internalFrameDeiconified(InternalFrameEvent e) {}
		});

		Container contentPane = frame.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(view);
		frame.setSize(500, 300);
		getDesktop().add(frame);
		frame.show();
		view.initializeDragDropHandling();

		return frame;
	}

	/*
	 * void openDemo()
	 * {
	 *   AutomatonDocument doc = AutomatonDocument.open();
	 *   if (doc != null)
	 *     createFrame(doc);
	 * }
	 *
	 * void closeDemo()
	 * {
	 *   if (getCurrentView() != null) {
	 *     JInternalFrame frame = getCurrentView().getInternalFrame();
	 *     if (frame != null) {
	 *       if (getCurrentView().isChanged()) {
	 *         String msg = "Save Changes to " + getCurrentView().getDoc().getName();
	 *         int choice = javax.swing.JOptionPane.showConfirmDialog(frame, msg, "AutomataEditor",
	 *                     javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
	 *         if (choice == javax.swing.JOptionPane.YES_OPTION)
	 *           saveDemo();
	 *         if((myCurrentView.isChanged() && choice == javax.swing.JOptionPane.YES_OPTION) ||
	 *                     choice == javax.swing.JOptionPane.CANCEL_OPTION)
	 *           //save was cancelled, or cancel was chosen.  Don't close.
	 *           return;
	 *       }
	 *       getDesktop().getDesktopManager().closeFrame(frame);
	 *       myCurrentView = null;
	 *       AppAction.updateAllActions();
	 *     }
	 *   }
	 * }
	 *
	 * void saveDemo()
	 * {
	 *   if (getCurrentView() != null) {
	 *     AutomatonDocument doc = getCurrentView().getDoc();
	 *     doc.save();
	 *   }
	 * }
	 *
	 * void saveAsDemo()
	 * {
	 *   if (getCurrentView() != null) {
	 *     AutomatonDocument doc = getCurrentView().getDoc();
	 *     doc.saveAs();
	 *   }
	 * }
	 */
	AutomatonDocument findAutomatonDocument(String path)
	{
		Object val = myMap.get(path);

		if ((val != null) && (val instanceof AutomatonDocument))
		{
			return (AutomatonDocument) val;
		}
		else
		{
			return null;
		}
	}

	AutomatonView getCurrentView()
	{
		return myCurrentView;
	}

	void randomAction()
	{
		setStatus("");

		AutomatonDocument doc = getCurrentView().getDoc();
		Rectangle r = getCurrentView().getViewRect();
		SimpleRAL s = new SimpleRAL(doc, (r.x + 100), ((r.width - r.x) - 100), (r.y + 100), ((r.height - r.y) - 100));

		s.performLayout();
		setStatus("Random Layout done");
	}

	void forceAction()
	{
		setStatus("");

		ForceDialog f = new ForceDialog(getCurrentView().getFrame(), "Force-Directed Settings", true, getCurrentView(), this);

		f.setVisible(true);
	}

	void layerAction()
	{
		setStatus("");

		LayerDialog l = new LayerDialog(getCurrentView().getFrame(), "Layered-Digraph Settings", true, getCurrentView(), this);

		l.setVisible(true);
	}

	void nodeAction()
	{
		NodeDialog n = new NodeDialog(getCurrentView().getFrame(), "Node Settings", true, getCurrentView());

		n.setVisible(true);
	}

	JDesktopPane getDesktop()
	{
		return myDesktop;
	}

	public JPanel getStatusArea()
	{
		return myStatusArea;
	}

	public void setStatus(String s)
	{
		if (s.equals(""))
		{
			s = " ";
		}

		myStatusLabel.setText(s);
		myStatusLabel.paintImmediately(0, 0, myStatusLabel.getWidth(), myStatusLabel.getHeight());
	}

	protected void initStatusArea()
	{
		getStatusArea().setMinimumSize(new Dimension(10, 10));
		getStatusArea().setBorder(BorderFactory.createEtchedBorder());
		getStatusArea().setLayout(new BorderLayout());
		getStatusArea().add(myStatusLabel, "Center");
		setStatus("Ready");
	}

	public void tableChanged(TableModelEvent e)
	{
		theAutomatonTable.revalidate();
	}

	public void fileAdd()
	{
		String title = theVisualProject.getUniqueAutomatonName();
		Automaton newAutomaton = new Automaton(title);

		try
		{
			theVisualProject.addAutomaton(newAutomaton);
		}
		catch (Exception e)
		{
			System.err.println("Error while adding the automaton to the container");

			return;
		}

		AutomatonDocument doc = new AutomatonDocument(theVisualProject, newAutomaton);

		createFrame(doc);
	}

	public void fileOpen()
	{

		// ActionMan.fileOpen(workbench);
	}

	public void fileSave()
	{

		// ActionMan.fileSave(workbench);
	}

	public void fileSaveAs()
	{

		// ActionMan.fileSaveAs(workbench);
	}

	public void filePrint()
	{
		AutomatonView currView = getCurrentView();

		if (currView != null)
		{
			currView.print();
		}
	}

	public void fileClose()
	{
		this.show(false);
	}

	// State
	protected HashMap myMap = new HashMap();
	protected AutomatonView myCurrentView = null;
	protected JDesktopPane myDesktop = new JDesktopPane();
	protected JMenuBar mainMenuBar = new JMenuBar();
	protected JMenu filemenu = new JMenu();
	protected JMenu editmenu = new JMenu();
	protected JMenu viewmenu = new JMenu();
	protected JMenu insertmenu = new JMenu();
	protected JMenu layoutmenu = new JMenu();
	protected JMenu helpmenu = new JMenu();
	protected JPanel myStatusArea = new JPanel();
	protected JLabel myStatusLabel = new JLabel();
	private int myDocCount = 1;

	class CloseListener
		implements VetoableChangeListener
	{
		CloseListener(AutomataEditor app, JInternalFrame frame)
		{
			myApp = app;
			myFrame = frame;
		}

		public void vetoableChange(PropertyChangeEvent e)
			throws PropertyVetoException
		{

			/*
			 *   String name = e.getPropertyName();
			 *   if(name.equals(JInternalFrame.IS_CLOSED_PROPERTY)) {
			 *     Component internalFrame = (Component)e.getSource();
			 *     Boolean oldvalue = (Boolean)e.getOldValue(),
			 *             newvalue = (Boolean)e.getNewValue();
			 *     if(oldvalue == Boolean.FALSE && newvalue == Boolean.TRUE) {
			 *       msg = "Save Changes to " + myApp.getCurrentView().getDoc().getName();
			 *       if (getCurrentView().isChanged()) {
			 *         int choice = javax.swing.JOptionPane.showConfirmDialog(myFrame, msg, "Automata Editor",
			 *             javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
			 *         if (choice == javax.swing.JOptionPane.YES_OPTION)
			 *           saveDemo();
			 *         if((myCurrentView.isChanged() && choice == javax.swing.JOptionPane.YES_OPTION) || choice == javax.swing.JOptionPane.CANCEL_OPTION)
			 *         //save was cancelled, or cancel was chosen.  Don't close.
			 *           throw new PropertyVetoException("close cancelled", e);
			 *         else {  //user either saved or chose "no".  either way, it's ok to close window.
			 *           myApp.myCurrentView = null;
			 *           AppAction.updateAllActions();
			 *         }
			 *       }
			 *       else { //no changes.  just close it.
			 *         myApp.myCurrentView = null;
			 *         AppAction.updateAllActions();
			 *       }
			 *     }
			 *   }
			 */
		}

		private String msg = "";
		private AutomataEditor myApp = null;
		private JInternalFrame myFrame = null;
	}
}
