
/*
 *  Copyright © Northwoods Software Corporation, 2000-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.HashMap;
import java.net.URL;
import com.nwoods.jgo.*;
import org.supremica.gui.*;

//import com.nwoods.jgo.examples.*;

/**
 * This example app is a simple process flow editor
 */
public class RecipeEditor
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	protected VisualProject theProject = null;

	public RecipeEditor(VisualProject theProject)
	{
		this.theProject = theProject;

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			//final RecipeEditor app = new RecipeEditor(theProject);
			//final JFrame mainFrame = new JFrame();
			// close the application when the main window closes
			addWindowListener(new WindowAdapter()
			{
				public void windowActivated(WindowEvent evt)
				{
					if (getCurrentView() != null)
					{
						getCurrentView().getDoc().updateLocationModifiable();
					}
				}

				public void windowClosing(WindowEvent event)
				{
					Object object = event.getSource();

					if (object == this)
					{
						exit();
					}
				}
			});
			setTitle("Recipe Editor");

			//Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
			Utility.setupFrame(this, 200, 500);

			//setBounds(0, 0, 800, 600);
			Container contentPane = getContentPane();

			contentPane.setLayout(new BorderLayout());

			/*contentPane.add("Center", app);
			contentPane.validate();
			mainFrame.setVisible(true);
			app.start();
			*/

			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			mainMenuBar = new JMenuBar();
			toolBar = new JToolBar();
			filemenu = new JMenu();
			editmenu = new JMenu();
			viewmenu = new JMenu();
			insertmenu = new JMenu();
			helpmenu = new JMenu();

			initMenus();
			initToolbar();
			addExitCommand();

			myDesktop = new JDesktopPane();
			myPalette = new Palette();

			myPalette.setPreferredSize(new Dimension(100, 300));
			myPalette.setMinimumSize(new Dimension(100, 100));

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

			splitPane.setContinuousLayout(true);
			splitPane.setLeftComponent(getPalette());
			splitPane.setRightComponent(getDesktop());
			splitPane.setDividerLocation(100);

			// Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(toolBar, "North");
			contentPane.add(splitPane, "Center");
			contentPane.validate();
		}
		catch (Throwable t)
		{
			System.err.println(t);
			t.printStackTrace();
		}
	}

/*
		public void setVisible(boolean doVisible)
		{
				frame.setVisible(doVisible);
		}
*/

/*
		public void show(boolean doShow)
		{
				frame.show(doShow);
		}

		public void show()
		{
				show(true);
		}
*/

	// ==============================================================
	// Define all the command actions
	// ==============================================================
	static private Icon iconImage(String resourceName)
	{
		URL url = Supremica.class.getResource(resourceName);

		if (url != null)
		{
			return new ImageIcon(url);
		}

		return null;
	}

	AppAction FileNewAction = new AppAction("New", iconImage("/toolbarButtonGraphics/general/New16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			newRecipe();
		}

		public boolean canAct()
		{
			return true;
		}
	};    // doesn't depend on a view
	AppAction FileOpenAction = new AppAction("Open", iconImage("/toolbarButtonGraphics/general/Open16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			openRecipe();
		}

		public boolean canAct()
		{
			return true;
		}
	};    // doesn't depend on a view
	AppAction FileCloseAction = new AppAction("Close", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			closeRecipe();
		}
	};
	AppAction FileSaveAction = new AppAction("Save", iconImage("/toolbarButtonGraphics/general/Save16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			saveRecipe();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isLocationModifiable();
		}
	};    // doesn't depend on a view
	AppAction FileSaveAsAction = new AppAction("Save As", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			saveAsRecipe();
		}
	};
	AppAction FilePropertiesAction = new AppAction("Properties", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			editRecipeProperties();
		}
	};
	AppAction PrintAction = new AppAction("Print", iconImage("/toolbarButtonGraphics/general/Print16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().print();
		}
	};
	AppAction CutAction = new AppAction("Cut", iconImage("/toolbarButtonGraphics/general/Cut16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().cut();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty() && getView().getDoc().isModifiable();
		}
	};    // doesn't depend on a view
	AppAction CopyAction = new AppAction("Copy", iconImage("/toolbarButtonGraphics/general/Copy16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().copy();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty();
		}
	};    // doesn't depend on a view
	AppAction PasteAction = new AppAction("Paste", iconImage("/toolbarButtonGraphics/general/Paste16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().paste();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};    // doesn't depend on a view
	AppAction DeleteAction = new AppAction("Delete", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().deleteSelection();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty() && getView().getDoc().isModifiable();
		}
	};
	AppAction SelectAllAction = new AppAction("Select All", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().selectAll();
		}
	};
	JMenuItem UndoMenuItem = null;
	AppAction UndoAction = new AppAction("Undo", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().getDocument().undo();
			AppAction.updateAllActions();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getDocument().canUndo());
		}

		public void updateEnabled()
		{
			super.updateEnabled();

			if ((UndoMenuItem != null) && (getView() != null))
			{
				UndoMenuItem.setText(getView().getDocument().getUndoManager().getUndoPresentationName());
			}
		}
	};
	JMenuItem RedoMenuItem = null;
	AppAction RedoAction = new AppAction("Redo", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().getDocument().redo();
			AppAction.updateAllActions();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getDocument().canRedo());
		}

		public void updateEnabled()
		{
			super.updateEnabled();

			if ((RedoMenuItem != null) && (getView() != null))
			{
				RedoMenuItem.setText(getView().getDocument().getUndoManager().getRedoPresentationName());
			}
		}
	};
	AppAction DrawRoutedLinkAction = new AppAction("Draw Routed Link", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().editDrawRoutedLink();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction MovePortAction = new AppAction("Move Port", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().editMovePort();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction ObjectPropertiesAction = new AppAction("Properties", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().editObjectProperties();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty();
		}
	};
	JCheckBoxMenuItem LinksJumpOverMenuItem = null;
	AppAction LinksJumpOverAction = new AppAction("Links Jump Over", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().getDoc().setLinksJumpOver(!getView().getDoc().getLinksJumpOver());
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}

		public void updateEnabled()
		{
			super.updateEnabled();

			if ((LinksJumpOverMenuItem != null) && (getView() != null))
			{
				LinksJumpOverMenuItem.setSelected(getView().getDoc().getLinksJumpOver());
			}
		}
	};
	AppAction ZoomNormalAction = new AppAction("Normal Zoom", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().zoomNormal();
		}
	};
	AppAction ZoomInAction = new AppAction("Zoom In", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().zoomIn();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getScale() < 8.0f);
		}
	};
	AppAction ZoomOutAction = new AppAction("Zoom Out", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().zoomOut();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getScale() > 0.13f);
		}
	};
	AppAction ZoomToFitAction = new AppAction("Zoom To Fit", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().zoomToFit();
		}
	};
	AppAction InsertCommentAction = new AppAction("Comment", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().insertComment();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction InsertInputAction = new AppAction("Start", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().insertInput();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction InsertOutputAction = new AppAction("Finish", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().insertOutput();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction InsertOperationAction = new AppAction("Operation", this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			getView().insertOperation();
		}

		public boolean canAct()
		{
			return super.canAct() && getView().getDoc().isModifiable();
		}
	};
	AppAction AboutAction = new AppAction("About", iconImage("/toolbarButtonGraphics/general/About16.gif"), this)
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e)
		{
			showAbout();
		}

		public boolean canAct()
		{
			return true;
		}
	};    // doesn't depend on a view

	void initMenus()
	{

		// ==============================================================
		// Define all the command actions and setup the menus
		// ==============================================================
		JMenuItem item = null;

		filemenu.setText("File");
		filemenu.setMnemonic('F');

		item = filemenu.add(FileNewAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		item.setMnemonic('N');
		item.setIcon(null);    // choose not to use icon in menu

		item = filemenu.add(FileOpenAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
		item.setMnemonic('O');
		item.setIcon(null);    // choose not to use icon in menu

		item = filemenu.add(FileCloseAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, Event.CTRL_MASK));
		item.setMnemonic('C');

		item = filemenu.add(FileSaveAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		item.setMnemonic('S');
		item.setIcon(null);    // choose not to use icon in menu

		item = filemenu.add(FileSaveAsAction);

		item.setMnemonic('A');
		filemenu.addSeparator();

		item = filemenu.add(FilePropertiesAction);

		item.setMnemonic('r');
		filemenu.addSeparator();

		item = filemenu.add(PrintAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
		item.setMnemonic('P');
		item.setIcon(null);    // choose not to use icon in menu
		mainMenuBar.add(filemenu);
		editmenu.setText("Edit");
		editmenu.setMnemonic('E');

		item = editmenu.add(CutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		item.setMnemonic('t');
		item.setIcon(null);    // choose not to use icon in menu

		item = editmenu.add(CopyAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
		item.setMnemonic('C');
		item.setIcon(null);    // choose not to use icon in menu

		item = editmenu.add(PasteAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
		item.setMnemonic('P');
		item.setIcon(null);    // choose not to use icon in menu

		item = editmenu.add(DeleteAction);

		item.setMnemonic('D');

		item = editmenu.add(SelectAllAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
		item.setMnemonic('l');
		editmenu.addSeparator();

		UndoMenuItem = editmenu.add(UndoAction);

		UndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));
		UndoMenuItem.setMnemonic('U');

		RedoMenuItem = editmenu.add(RedoAction);

		RedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
		RedoMenuItem.setMnemonic('R');
		editmenu.addSeparator();

		item = editmenu.add(ObjectPropertiesAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK));
		item.setMnemonic('o');
		editmenu.addSeparator();

		item = editmenu.add(DrawRoutedLinkAction);

		item.setMnemonic('w');

		item = editmenu.add(MovePortAction);

		item.setMnemonic('v');
		mainMenuBar.add(editmenu);
		viewmenu.setText("View");
		viewmenu.setMnemonic('V');

		item = viewmenu.add(ZoomNormalAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, Event.CTRL_MASK | Event.SHIFT_MASK));
		item.setMnemonic('N');

		item = viewmenu.add(ZoomInAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, Event.CTRL_MASK));
		item.setMnemonic('I');

		item = viewmenu.add(ZoomOutAction);

		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, Event.SHIFT_MASK));
		item.setMnemonic('O');

		item = viewmenu.add(ZoomToFitAction);

		// item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		item.setMnemonic('Z');
		viewmenu.addSeparator();

		LinksJumpOverMenuItem = new JCheckBoxMenuItem(LinksJumpOverAction);

		viewmenu.add(LinksJumpOverMenuItem);
		LinksJumpOverMenuItem.setMnemonic('j');
		mainMenuBar.add(viewmenu);
		insertmenu.setText("Insert");
		insertmenu.setMnemonic('I');

		item = insertmenu.add(InsertCommentAction);

		item.setMnemonic('C');

		item = insertmenu.add(InsertInputAction);

		item.setMnemonic('S');

		item = insertmenu.add(InsertOutputAction);

		item.setMnemonic('F');

		item = insertmenu.add(InsertOperationAction);

		item.setMnemonic('A');
		mainMenuBar.add(insertmenu);
		helpmenu.setText("Help");
		helpmenu.setMnemonic('H');

		item = helpmenu.add(AboutAction);

		item.setMnemonic('A');
		item.setIcon(null);    // choose not to use icon in menu
		mainMenuBar.add(helpmenu);
		setJMenuBar(mainMenuBar);
	}

	protected JToolBar initToolbar()
	{
		JButton button = null;

		button = toolBar.add(FileNewAction);

		button.setToolTipText("Create a new work-flow window");

		button = toolBar.add(FileOpenAction);

		button.setToolTipText("Open a previously saved work-flow window");

		button = toolBar.add(FileSaveAction);

		button.setToolTipText("Save a work-flow window");
		toolBar.addSeparator();

		button = toolBar.add(CutAction);

		button.setToolTipText("Cut to clipboard");

		button = toolBar.add(CopyAction);

		button.setToolTipText("Copy to clipboard");

		button = toolBar.add(PasteAction);

		button.setToolTipText("Paste from clipboard");
		toolBar.addSeparator();

		button = toolBar.add(PrintAction);

		button.setToolTipText("Print selected work-flow window");
		toolBar.addSeparator();

		button = toolBar.add(AboutAction);

		button.setToolTipText("Display help about this application");

		return toolBar;
	}

	public void addExitCommand()
	{
		filemenu.addSeparator();

		AppAction ExitAction = new AppAction("Exit", this)
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				exit();
			}

			public boolean canAct()
			{
				return true;
			}
		};    // doesn't depend on a view
		JMenuItem item = filemenu.add(ExitAction);

		item.setMnemonic('x');
	}

	void initPalette()
	{
		getPalette().setBorder(new TitledBorder("Activities"));

		JGoDocument doc = getPalette().getDocument();
		Comment cmnt = new Comment("a comment");

		cmnt.setEditable(true);
		doc.addObjectAtTail(cmnt);

		OperationNode snode;

		for (int i = 0; i < 8; i++)
		{
			OperationNode n = makePaletteNode(i, -1);

			doc.addObjectAtTail(n);
		}
	}

	OperationNode makePaletteNode(int acttype, int id)
	{
		OperationNode snode = new OperationNode();
		JGoImage nodeicon = new JGoImage(new Rectangle(0, 0, 40, 40));

		nodeicon.loadImage(RecipeEditor.class.getResource("doc.gif"), true);
		snode.initialize(acttype, id);
		snode.addScatteredPorts((int) (Math.random() * 5) + 1);
		snode.getLabel().setSelectable(false);

		return snode;
	}

/*
		public void init()    // Applet initialization
		{
				JGoImage.setDefaultBase(getCodeBase());
		}


		public void start()
		{

				// enable drag-and-drop from separate thread
				new Thread(this).start();
				initPalette();

				MultiPortNodePort.FULL = new JGoBrush(JGoBrush.SOLID, Color.green);
		}

		public void run()
		{
				getPalette().initializeDragDropHandling();

				if (getDesktop().getAllFrames().length == 0)
				{
						newRecipe();
				}

				AppAction.updateAllActions();
		}

*/
	public void destroy()
	{
		JInternalFrame[] frames = getDesktop().getAllFrames();

		for (int i = 0; i < frames.length; i++)
		{
			JInternalFrame f = frames[i];

			try
			{
				f.setClosed(true);
			}
			catch (Exception x) {}
		}
	}

/*
		static public void main(String args[])
		{

		}
*/
	void exit()
	{
		destroy();
		System.exit(0);
	}

	void showAbout()
	{
		HelpDlg helpDlg = new HelpDlg(null, "About", true);

		helpDlg.setVisible(true);
	}

	void editRecipeProperties()
	{
		RecipeView v = getCurrentView();

		if (v != null)
		{
			v.getDoc().startTransaction();
			new RecipeDialog(v.getFrame(), v.getDoc()).setVisible(true);
			v.getDoc().endTransaction("Recipe Properties");
		}
	}

	public void createFrame(RecipeDocument doc)
	{
		final RecipeView view = new RecipeView(doc);
		final JInternalFrame frame = new JInternalFrame(doc.getName(), true, true, true);

		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		view.initialize(this, frame);

		// keep track of the "current" view, even if it doesn't have focus
		// try to give focus to a view when it becomes activated
		// enable/disable all the command actions appropriately for the view
		frame.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameActivated(InternalFrameEvent e)
			{
				myCurrentView = view;

				view.requestFocus();
				view.getDoc().updateLocationModifiable();
				AppAction.updateAllActions();
			}

			public void internalFrameDeactivated(InternalFrameEvent e) {}

			public void internalFrameOpened(InternalFrameEvent e) {}

			public void internalFrameClosing(InternalFrameEvent e) {}

			public void internalFrameClosed(InternalFrameEvent e)
			{
				myCurrentView = null;

				AppAction.updateAllActions();
			}

			public void internalFrameIconified(InternalFrameEvent e) {}

			public void internalFrameDeiconified(InternalFrameEvent e) {}
		});
		frame.addVetoableChangeListener(new java.beans.VetoableChangeListener()
		{
			public void vetoableChange(java.beans.PropertyChangeEvent evt)
				throws java.beans.PropertyVetoException
			{
				if (evt.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY) && (evt.getOldValue() == Boolean.FALSE) && (evt.getNewValue() == Boolean.TRUE))
				{
					if (view.getDoc().isModified())
					{
						String msg = "Save changes to ";

						if (view.getDoc().getName().equals(""))
						{
							msg += "modified document?";
						}
						else
						{
							msg += view.getDoc().getName();
						}

						msg += "\n  (";

						if (view.getDoc().getLocation().equals(""))
						{
							msg += "<no location>";
						}
						else
						{
							msg += view.getDoc().getLocation();
						}

						msg += ")";

						int answer = JOptionPane.showConfirmDialog(view.getFrame(), msg, "Closing modified document", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

						if ((answer == JOptionPane.NO_OPTION) || (answer == JOptionPane.YES_OPTION))
						{
							if (answer == JOptionPane.YES_OPTION)
							{
								if (view.getDoc().isLocationModifiable())
								{
									view.getDoc().save();
								}
								else
								{
									view.getDoc().saveAs(".wfl");
								}
							}

							// then allow the internal frame to close
							getDesktop().remove(frame);
							getDesktop().repaint();
						}
						else
						{

							// CANCEL_OPTION--don't close
							throw new java.beans.PropertyVetoException("", evt);
						}
					}
				}
			}
		});

		Container contentPane = frame.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(view);
		frame.setSize(500, 400);
		getDesktop().add(frame);
		frame.show();
		view.initializeDragDropHandling();
	}

	void newRecipe()
	{
		RecipeDocument doc = new RecipeDocument();
		String t = "Untitled" + Integer.toString(myDocCount++);

		doc.setName(t);
		createFrame(doc);
		doc.setModified(false);
		doc.discardAllEdits();
	}

	void openRecipe()
	{
		String defaultLoc = null;
		RecipeView view = getCurrentView();

		if (view != null)
		{
			RecipeDocument doc = view.getDoc();

			defaultLoc = doc.getLocation();
		}

		RecipeDocument doc = RecipeDocument.open(this, defaultLoc);

		if (doc != null)
		{
			createFrame(doc);
		}
	}

	void closeRecipe()
	{
		if (getCurrentView() != null)
		{
			JInternalFrame frame = getCurrentView().getInternalFrame();

			if (frame != null)
			{
				try
				{
					frame.setClosed(true);
				}
				catch (Exception x) {}
			}
		}
	}

	void saveRecipe()
	{
		if (getCurrentView() != null)
		{
			RecipeDocument doc = getCurrentView().getDoc();

			doc.save();
		}
	}

	void saveAsRecipe()
	{
		if (getCurrentView() != null)
		{
			RecipeDocument doc = getCurrentView().getDoc();

			doc.saveAs(".wfl");
		}
	}

	RecipeDocument findRecipeDocument(String path)
	{
		Object val = myMap.get(path);

		if ((val != null) && (val instanceof RecipeDocument))
		{
			return (RecipeDocument) val;
		}
		else
		{
			return null;
		}
	}

	RecipeView getCurrentView()
	{
		return myCurrentView;
	}

	JDesktopPane getDesktop()
	{
		return myDesktop;
	}

	Palette getPalette()
	{
		return myPalette;
	}

	public static RecipeEditor createEditor(VisualProject theProject)
	{
		return new RecipeEditor(theProject);

/*
				try
				{
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

						final RecipeEditor app = new RecipeEditor(theProject);
						final JFrame mainFrame = new JFrame();

						// close the application when the main window closes
						mainFrame.addWindowListener(new WindowAdapter()
						{
								public void windowActivated(WindowEvent evt)
								{
										if (app.getCurrentView() != null)
										{
												app.getCurrentView().getDoc().updateLocationModifiable();
										}
								}

								public void windowClosing(WindowEvent event)
								{
										Object object = event.getSource();

										if (object == mainFrame)
										{
												app.exit();
										}
								}
						});
						app.addExitCommand();
						mainFrame.setTitle("Recipe Editor");

						//Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();

						mainFrame.setBounds(0, 0, 800, 600);

						Container contentPane = mainFrame.getContentPane();

						contentPane.setLayout(new BorderLayout());
						contentPane.add("Center", app);
						contentPane.validate();
						mainFrame.setVisible(true);
						app.start();
				}
				catch (Throwable t)
				{
						System.err.println(t);
						t.printStackTrace();
						System.exit(1);
				}

*/
	}

	// State
	protected HashMap myMap = new HashMap();
	protected RecipeView myCurrentView;
	protected JDesktopPane myDesktop;
	protected Palette myPalette;
	protected JMenuBar mainMenuBar;
	protected JToolBar toolBar;
	protected JMenu filemenu;
	protected JMenu editmenu;
	protected JMenu viewmenu;
	protected JMenu insertmenu;
	protected JMenu helpmenu;
	private int myDocCount = 1;
}
