
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
import java.beans.*;

//import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.util.ToolBarButton;

public class AutomataEditor
	extends JFrame
	implements TableModelListener, EditorView
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

	// State
	protected HashMap myMap = new HashMap();
	protected AutomatonView myCurrentView = null;
	protected JDesktopPane myDesktop = new JDesktopPane();
	protected JMenuBar mainMenuBar = new JMenuBar();
	protected JMenu filemenu = new JMenu();
	protected JMenu editmenu = new JMenu();

//      protected JMenu viewmenu = new JMenu();
//  protected JMenu insertmenu = new JMenu();
//      protected JMenu layoutmenu = new JMenu();
	protected JMenu helpmenu = new JMenu();
	protected JPanel myStatusArea = new JPanel();
	protected JLabel myStatusLabel = new JLabel();
	private int myDocCount = 1;

	public AutomataEditor(VisualProject theVisualProject)
	{
		this.theVisualProject = theVisualProject;
		theActions = new EditorActions(this);

		setTitle("Supremica Editor");
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
		JMenuItem item;

		// Create File Menu
		filemenu.setText("File");
		filemenu.setMnemonic('F');

		// Add
		item = filemenu.add(theActions.getFileAddAction());

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		item.setMnemonic('A');

		// Close
		item = filemenu.add(theActions.getFileCloseAction());

		item.setMnemonic('x');
		mainMenuBar.add(filemenu);
		editmenu.setText("Edit");
		editmenu.setMnemonic('E');

		/*
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

		item = editmenu.add(theActions.CutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		item.setMnemonic('t');

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
*/
		setJMenuBar(mainMenuBar);
	}

	public JFrame getCurrentFrame()
	{
		return this;
	}

	public JDesktopPane getDesktop()
	{
		return myDesktop;
	}

	public void initToolbar()
	{

		// Enables stylish rollover buttons - JDK 1.4 required
		toolBar.setRollover(true);

		ToolBarButton addButton = new ToolBarButton(theActions.getFileAddAction());

		addButton.setText("");    // Quick and dirty fix, Change to its own class instead
		toolBar.add(addButton);
		toolBar.addSeparator();

		ToolBarButton printButton = new ToolBarButton(theActions.getFilePrintAction());

		printButton.setText("");    // Fix
		toolBar.add(printButton);
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
			logger.debug(t.getStackTrace());

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
		Utility.setupFrame(this, 800, 600);
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

/*
												if (theDocument.isLayoutNeeded())
												{
														randomAction();
														theDocument.setLayoutNeeded(false);
												}
*/
					}
					catch (Exception ex)
					{
						logger.error("Error while displaying the automaton", ex);
						logger.debug(ex.getStackTrace());

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

	public VisualProject getVisualProject()
	{
		return theVisualProject;
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

	public AutomatonView getCurrentAutomatonView()
	{
		return myCurrentView;
	}

	public AutomataEditor getAutomataEditor()
	{
		return this;
	}

	public JPanel getStatusArea()
	{
		return myStatusArea;
	}

	public void setStatus(String s)
	{
		if ((s == null) || s.equals(""))
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

/*
<<<<<<< AutomataEditor.java
=======
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
				this.setVisible(false);
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
>>>>>>> 1.17

*/
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
