
/*
 *  Test of JGo for Grafchart.
 */
package org.jgrafchart;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import com.nwoods.jgo.*;
import org.jgrafchart.GCStep;
import org.jgrafchart.GCTransition;
import org.jgrafchart.DigitalIn;
import org.jgrafchart.DigitalOut;
import org.jgrafchart.DigitalOut0;
import org.jgrafchart.DigitalOut1;
import org.jgrafchart.AppAction;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.jgrafchart.Transitions.*;
import org.jgrafchart.Actions.*;
import java.util.*;

/**
 * Two views.
 */
public class Basic2GC
	extends JFrame
{    // Constructor
	public Basic2GC()
	{

		// final JFrame mainFrame = new JFrame();
		// close the application when the main window closes
		// mainFrame.addWindowListener(new WindowAdapter() {
		// public void windowClosing(java.awt.event.WindowEvent event) {
		// Object object = event.getSource();
		// if (object == mainFrame)
		// System.exit(0);
		// }
		// });
		parser = new TransitionParser(new StringReader(" "));
		actionParser = new ActionParser(new StringReader(" "));

		Container contentPane = getContentPane();

		contentPane.setBackground(new Color(0xFF, 0xCC, 0xCC));
		setTitle("JGrafchart");
		setSize(1000, 1000);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent event)
			{
				Object object = event.getSource();

				if (object == this)
				{
					System.exit(0);
				}
			}
		});

		// ==============================================================
		// Create the first window
		// ==============================================================
		// viewDoc = new JGoDocument();
		// myView = new GCView(viewDoc);
		// viewDoc.setPaperColor(new Color(0xFF, 0xFF, 0xDD));
		// myView.addKeyListener(new KeyAdapter() {
		// public void keyPressed(KeyEvent evt) {
		// int t = evt.getKeyCode();
		// if (t == KeyEvent.VK_DELETE) {
		// JGoSelection sel = myView.getSelection();
		// myView.removeDeletedPointers(sel);
		// myView.deleteSelection();
		// } else if (evt.isControlDown() && t == KeyEvent.VK_Q) {
		// System.exit(0);
		// }
		// }
		// });
		// myView.getDocument().addDocumentListener(new JGoDocumentListener() {
		// public void documentChanged(JGoDocumentEvent e) {
		// processDocChange(e); }
		// });
		// myView.addViewListener(new JGoViewListener() {
		// public void viewChanged(JGoViewEvent e) {
		// processViewChange(e);
		// }
		// });
		initMenus();
		initPopupMenus();

		// ==============================================================
		// Create the second window
		// ==============================================================
		paletteDoc = new JGoDocument();
		myPalette = new GCPalette(paletteDoc);

		myPalette.setMinimumSize(new Dimension(150, 1000));

		// ==============================================================
		// Add the windows to the frame
		// ==============================================================
		// JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,myPalette    ,myView);
		// splitPane.setOneTouchExpandable(true);
		// splitPane.setDividerLocation(150);
		// Dimension minimumSize = new Dimension(100, 50);
		// myPalette.setMinimumSize(minimumSize);
		// myView.setMinimumSize(minimumSize);
		myDesktop = new JDesktopPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(myDesktop, "Center");
		contentPane.add(myPalette, "West");

		// setContentPane(splitPane);
		// splitPane.validate();
		contentPane.validate();

		// setLocation(50,50);
		setVisible(true);

		// myView.initializeDragDropHandling();
		myPalette.initializeDragDropHandling();

		// initView();
		initPalette();
	}

	AppAction CompileAction = new AppAction("Compile", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			compileAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView &&!getView().executing;
		}
	};

	// AppAction InitializeAction = new AppAction("Initialize", this) {
	// public void actionPerformed(ActionEvent e) {initializeAction();}
	// public boolean canAct() {return super.canAct() && getApp().topLevelView
	// &&  !getView().executing && getView().compiledOnce;}};
	AppAction ExecuteAction = new AppAction("Execute", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			executeAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView &&!getView().executing && getView().compiledOnce;
		}
	};
	AppAction StopAction = new AppAction("Stop", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			stopAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView && getView().executing;
		}
	};
	AppAction DebugAction = new AppAction("Debug", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			debugAction();
		}
	};
	AppAction NewAction = new AppAction("New", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			newAction();
		}

		public boolean canAct()
		{
			return true;
		}
	};
	AppAction OpenAction = new AppAction("Open", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			openAction();
		}

		public boolean canAct()
		{
			return true;
		}
	};
	AppAction PrintAction = new AppAction("Print", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			getView().print();
		}
	};
	AppAction SaveAction = new AppAction("Save", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			saveAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView &&!getView().executing;
		}
	};
	AppAction SaveAsAction = new AppAction("Save As", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			saveAsAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView &&!getView().executing;
		}
	};
	AppAction PropertiesAction = new AppAction("Properties", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			propertiesAction();
		}

		public boolean canAct()
		{
			return super.canAct() && getApp().topLevelView;
		}
	};
	AppAction ExitAction = new AppAction("Exit", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			exitAction();
		}

		public boolean canAct()
		{
			return true;
		}
	};
	AppAction ZoomOutAction = new AppAction("Zoom out", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			zoomOutAction();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getScale() > 0.13f);
		}
	};
	AppAction ZoomInAction = new AppAction("Zoom in", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			zoomInAction();
		}

		public boolean canAct()
		{
			return super.canAct() && (getView().getScale() < 8.0f);
		}
	};
	AppAction ZoomNormalAction = new AppAction("Zoom normal size", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			zoomNormalAction();
		}
	};
	AppAction ZoomToFitAction = new AppAction("Zoom to fit", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			zoomToFitAction();
		}
	};
	AppAction ZoomCutAction = new AppAction("Cut", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			cutAction();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty();
		}
	};
	AppAction ZoomCopyAction = new AppAction("Copy", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			copyAction();
		}

		public boolean canAct()
		{
			return super.canAct() &&!getView().getSelection().isEmpty();
		}
	};
	AppAction ZoomPasteAction = new AppAction("Paste", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			pasteAction();
		}
	};
	AppAction ShowActionBlockAction = new AppAction("Show Action Block", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			showActionBlockAction(e);
		}

		public boolean canAct()
		{
			return super.canAct() && (getApp().selectedObject != null) && (getApp().selectedObject instanceof GCStep) &&!((GCStep) getApp().selectedObject).actionBlockVisible;
		}
	};
	AppAction HideActionBlockAction = new AppAction("Hide Action Block", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			hideActionBlockAction(e);
		}

		public boolean canAct()
		{
			return super.canAct() && (getApp().selectedObject != null) && (getApp().selectedObject instanceof GCStep) && ((GCStep) getApp().selectedObject).actionBlockVisible;
		}
	};
	AppAction TransitionEditAction = new AppAction("Edit", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			transitionEditAction(e);
		}
	};
	AppAction StepEditAction = new AppAction("Edit", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			stepEditAction(e);
		}
	};
	AppAction ProcedureStepEditAction = new AppAction("Edit", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			procedureStepEditAction(e);
		}
	};
	AppAction ProcedureStepShowAction = new AppAction("Call", this)
	{
		public void actionPerformed(ActionEvent e)
		{
			procedureStepShowAction(e);
		}
	};

	void initMenus()
	{
		filemenu.setText("File");
		filemenu.add(NewAction);
		filemenu.add(OpenAction);
		filemenu.add(SaveAction);
		filemenu.add(SaveAsAction);
		filemenu.addSeparator();
		filemenu.add(PrintAction);
		filemenu.addSeparator();
		filemenu.add(PropertiesAction);
		filemenu.addSeparator();
		filemenu.add(ExitAction);
		mainMenuBar.add(filemenu);
		viewmenu.setText("Edit");
		viewmenu.add(ZoomCutAction);
		viewmenu.add(ZoomCopyAction);
		viewmenu.add(ZoomPasteAction);
		viewmenu.addSeparator();
		viewmenu.add(ZoomInAction);
		viewmenu.add(ZoomOutAction);
		viewmenu.add(ZoomNormalAction);
		viewmenu.add(ZoomToFitAction);
		mainMenuBar.add(viewmenu);
		grafcetmenu.setText("Execute");
		grafcetmenu.add(CompileAction);

		// grafcetmenu.add(InitializeAction);
		grafcetmenu.add(ExecuteAction);
		grafcetmenu.add(StopAction);

		// grafcetmenu.add(DebugAction);
		mainMenuBar.add(grafcetmenu);
		setJMenuBar(mainMenuBar);
	}

	void initPopupMenus()
	{
		stepmenu.setLabel("Step Menu");
		stepmenu.add(ShowActionBlockAction);
		stepmenu.add(HideActionBlockAction);
		stepmenu.add(StepEditAction);
		transitionmenu.setLabel("Transition Menu");
		transitionmenu.add(TransitionEditAction);
		procedurestepmenu.setLabel("Procedure Step Menu");
		procedurestepmenu.add(ProcedureStepEditAction);
		procedurestepmenu.add(ProcedureStepShowAction);
	}

	void zoomInAction()
	{
		double newscale = Math.rint(myCurrentView.getScale() / 0.9f * 100f) / 100f;

		myCurrentView.setScale(newscale);
		updateActions();
	}

	void zoomOutAction()
	{
		double newscale = Math.rint(myCurrentView.getScale() * 0.9f * 100f) / 100f;

		myCurrentView.setScale(newscale);
		updateActions();
	}

	void zoomNormalAction()
	{
		double newscale = 1;

		myCurrentView.setScale(newscale);
		updateActions();
	}

	void zoomToFitAction()
	{
		double newscale = 1;

		if (!myCurrentView.getDocument().isEmpty())
		{
			double extentWidth = myCurrentView.getExtentSize().width;
			double printWidth = myCurrentView.getPrintDocumentSize().width;
			double extentHeight = myCurrentView.getExtentSize().height;
			double printHeight = myCurrentView.getPrintDocumentSize().height;

			newscale = Math.min((extentWidth / printWidth), (extentHeight / printHeight));
		}

		if (newscale > 2)
		{
			newscale = 1;
		}

		newscale *= myCurrentView.getScale();

		myCurrentView.setScale(newscale);
		myCurrentView.setViewPosition(0, 0);
		updateActions();
	}

	void cutAction()
	{
		GCView view = myCurrentView;
		JGoSelection sel = view.getSelection();

		view.deleteHierarchies(sel);
		view.cut();
	}

	void pasteAction()
	{
		myCurrentView.paste();
	}

	void copyAction()
	{
		myCurrentView.copy();
	}

	void newAction()
	{
		GCDocument doc = new GCDocument();
		String t = "JGrafchart" + Integer.toString(myDocCount++);

		doc.setName(t);
		topGrafcharts.add(doc);

		final GCView view = new GCView(doc);
		final JInternalFrame frame = new JInternalFrame(doc.getName(), true, true, true, true);

		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		doc.setPaperColor(new Color(0xFF, 0xFF, 0xDD));
		view.initialize(this, frame);
		frame.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameActivated(InternalFrameEvent e)
			{
				myCurrentView = view;
				topLevelView = true;

				view.requestFocus();

				// view.getDoc().updateLocationModifiable();
				AppAction.updateAllActions();
			}

			public void internalFrameDeactivated(InternalFrameEvent e) {}

			public void internalFrameOpened(InternalFrameEvent e) {}

			public void internalFrameClosing(InternalFrameEvent e) {}

			public void internalFrameClosed(InternalFrameEvent e)
			{
				closeMacros(view.getDoc());
				topGrafcharts.remove(view.getDoc());

				myCurrentView = null;

				AppAction.updateAllActions();
			}

			public void internalFrameIconified(InternalFrameEvent e) {}

			public void internalFrameDeiconified(InternalFrameEvent e) {}
		});

		// initMenus();
		Container contentPane = frame.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(view);
		frame.setSize(400, 600);
		getDesktop().add(frame);
		frame.show();
		view.initializeDragDropHandling();
	}

	public void closeMacros(GCDocument doc)
	{
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) obj;

				if (ms.frame != null)
				{
					closeMacros(ms.myContentDocument);

					try
					{
						ms.frame.setClosed(true);
					}
					catch (Exception x) {}
				}
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	void openAction()
	{

		// ProcessDocument doc = ProcessDocument.open();
		// if (doc != null)
		// createFrame(doc);
		GCDocument doc = null;
		JFileChooser chooser;

		if (myCurrentView != null)
		{
			chooser = new JFileChooser(myCurrentView.getDoc().getReadFileLocation());
		}
		else
		{
			chooser = new JFileChooser("");
		}

		// chooser.setCurrentDirectory(null);
		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String loc = chooser.getSelectedFile().getAbsolutePath();
			FileInputStream fstream = null;

			try
			{
				fstream = new FileInputStream(loc);
				doc = loadObjects(fstream);

				topGrafcharts.add(doc);
			}
			catch (IOException x)
			{
				JOptionPane.showMessageDialog(null, x, "Open Document Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception x)
			{
				JOptionPane.showMessageDialog(null, x, "Loading Document Exception", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try
				{
					if (fstream != null)
					{
						fstream.close();
					}
				}
				catch (Exception x) {}
			}

			doc.setWriteFileLocation(loc);
			doc.setReadFileLocation(loc);

			final GCView view = new GCView(doc);
			final JInternalFrame frame = new JInternalFrame(doc.getName(), true, true, true, true);

			frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			doc.setPaperColor(new Color(0xFF, 0xFF, 0xDD));
			view.initialize(this, frame);
			view.setScale(doc.currentScale);
			frame.setBounds(doc.bounds);
			frame.addInternalFrameListener(new InternalFrameListener()
			{
				public void internalFrameActivated(InternalFrameEvent e)
				{
					myCurrentView = view;
					topLevelView = true;

					view.requestFocus();

					// view.getDoc().updateLocationModifiable();
					AppAction.updateAllActions();
				}

				public void internalFrameDeactivated(InternalFrameEvent e) {}

				public void internalFrameOpened(InternalFrameEvent e) {}

				public void internalFrameClosing(InternalFrameEvent e) {}

				public void internalFrameClosed(InternalFrameEvent e)
				{
					topGrafcharts.remove(view.getDoc());

					myCurrentView = null;

					AppAction.updateAllActions();
				}

				public void internalFrameIconified(InternalFrameEvent e) {}

				public void internalFrameDeiconified(InternalFrameEvent e) {}
			});

			// initMenus();
			Container contentPane = frame.getContentPane();

			contentPane.setLayout(new BorderLayout());
			contentPane.add(view);

			// frame.setSize(400, 600);
			getDesktop().add(frame);
			frame.show();
			view.initializeDragDropHandling();
		}
	}

	static public GCDocument loadObjects(InputStream ins)
		throws IOException, ClassNotFoundException
	{
		ObjectInputStream istream = new ObjectInputStream(ins);
		Object newObj = istream.readObject();

		if (newObj instanceof GCDocument)
		{
			GCDocument doc = (GCDocument) newObj;

			return doc;
		}
		else
		{
			return null;
		}
	}

	void saveAction()
	{
		if (myCurrentView.getDoc().getWriteFileLocation().equals(""))
		{
			saveAsAction();
		}
		else
		{
			store();
		}
	}

	void cleanUp(GCDocument doc)
	{
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GCTransition)
			{
				GCTransition t = (GCTransition) obj;

				t.node = null;
			}

			if (obj instanceof GCStep)
			{
				GCStep s = (GCStep) obj;

				s.node = null;
			}

			if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
			{
				MacroStep ms = (MacroStep) obj;

				cleanUp(ms.myContentDocument);

				ms.frame = null;
				ms.parentView = null;
				ms.view = null;
			}

			if (obj instanceof ProcedureStep)
			{
				ProcedureStep ps = (ProcedureStep) obj;

				ps.viewOwner = null;
				ps.frame = null;
				ps.parentView = null;
				ps.view = null;
			}

			if (obj instanceof GrafcetProcedure)
			{
				GrafcetProcedure gp = (GrafcetProcedure) obj;

				cleanUp(gp.myContentDocument);

				gp.frame = null;
				gp.parentView = null;
				gp.view = null;
			}

			if (obj instanceof DigitalIn)
			{
				DigitalIn in = (DigitalIn) obj;

				in.digIn = null;
			}

			if (obj instanceof DigitalOut)
			{
				DigitalOut out = (DigitalOut) obj;

				out.digOut = null;
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	void saveAsAction()
	{
		GCDocument doc = myCurrentView.getDoc();
		JFileChooser chooser = new JFileChooser(doc.getWriteFileLocation());

		// chooser.setCurrentDirectory(null);
		int returnVal = chooser.showSaveDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			stopAction();
			cleanUp(doc);

			String loc = chooser.getSelectedFile().getAbsolutePath();

			doc.setReadFileLocation(loc);
			doc.setWriteFileLocation(loc);

			doc.currentScale = myCurrentView.getScale();
			doc.bounds = myCurrentView.myInternalFrame.getBounds();

			FileOutputStream fstream = null;

			try
			{
				fstream = new FileOutputStream(doc.getWriteFileLocation());

				storeObjects(fstream, doc);
			}
			catch (Exception x)
			{
				JOptionPane.showMessageDialog(null, x, "Save Document Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try
				{
					if (fstream != null)
					{
						fstream.close();
					}
				}
				catch (Exception x) {}
			}
		}
	}

	public void store()
	{
		GCDocument doc = myCurrentView.getDoc();

		if (!doc.getWriteFileLocation().equals(""))
		{
			cleanUp(doc);

			doc.currentScale = myCurrentView.getScale();
			doc.bounds = myCurrentView.myInternalFrame.getBounds();

			FileOutputStream fstream = null;

			try
			{
				fstream = new FileOutputStream(doc.getWriteFileLocation());

				storeObjects(fstream, doc);
			}
			catch (Exception x)
			{
				JOptionPane.showMessageDialog(null, x, "Save Document Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try
				{
					if (fstream != null)
					{
						fstream.close();
					}
				}
				catch (Exception x) {}
			}
		}
	}

	public void storeObjects(OutputStream outs, JGoDocument doc)
		throws IOException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(outs);

		ostream.writeObject(doc);
		ostream.flush();
	}

	public void propertiesAction()
	{
		GCView v = myCurrentView;

		if (v != null)
		{
			new GrafcetDialog(v.getFrame(), v.getDoc(), this).setVisible(true);
		}
	}

	void initializeAction()
	{
		myCurrentView.initializeDocument(myCurrentView.getDoc());
		updateActions();
	}

	void debugAction()
	{
		topGrafcharts.printOutNames();
	}

	void executeAction()
	{
		myCurrentView.initializeDocument(myCurrentView.getDoc());
		myCurrentView.setDragDropEnabled(false);
		myCurrentView.start();
		updateActions();
	}

	void compileJGoObject(JGoObject obj, ArrayList symbolList)
	{
		org.jgrafchart.Transitions.SimpleNode n;
		org.jgrafchart.Actions.SimpleNode n1;

		// System.out.println("Compiling " + obj);
		if (obj instanceof GenericTransition)
		{
			GenericTransition t = (GenericTransition) obj;

			// System.out.println("t " + t);
			t.compileStructure();

			if (t.node != null)
			{

				// System.out.println("t.node " + t.node);
				boolean compilationOK = t.node.compile(symbolList);

				if (!compilationOK)
				{
					t.setTextColor(red);
				}
				else
				{
					t.setTextColor(black);
				}
			}
			else
			{

				// System.out.println("t.myLabel " + t.myLabel);
				String value = t.getLabelText();

				// System.out.println("value " + value);
				t.setTextColor(black);
				parser.ReInit(new StringReader(value));

				try
				{
					n = parser.Start();
					t.node = n;
				}
				catch (Throwable ex)
				{
					t.setTextColor(red);
					System.out.println("Oops. Nisse");

					// System.out.println(ex.getMessage());
					// ex.printStackTrace();
				}

				boolean compilationOK = t.node.compile(symbolList);

				if (!compilationOK)
				{
					t.setTextColor(red);
				}
				else
				{
					t.setTextColor(black);
				}
			}
		}

		if (obj instanceof GCStep)
		{
			GCStep s = (GCStep) obj;

			if (s.node != null)
			{

				// System.out.println("Calling s.node.compile (s.node != null)");
				// s.node.dump(" ");
				String value;

				if (s.actionBlockVisible)
				{
					value = s.myActionLabel.getText();

					s.myActionLabel.setTextColor(black);
				}
				else
				{
					value = s.actionText;
				}

				actionParser.ReInit(new StringReader(value));

				try
				{
					n1 = actionParser.Statement();

					// System.out.println(n1);
					s.node = n1;
				}
				catch (Exception ex)
				{
					if (s.actionBlockVisible)
					{
						s.myActionLabel.setTextColor(red);
					}

					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				boolean compilationOK = s.node.compile(symbolList);

				if (s.actionBlockVisible)
				{
					if (!compilationOK)
					{
						s.myActionLabel.setTextColor(red);
					}
					else
					{
						s.myActionLabel.setTextColor(black);
					}
				}
			}
			else
			{
				String value;

				if (s.actionBlockVisible)
				{
					value = s.myActionLabel.getText();

					s.myActionLabel.setTextColor(black);
				}
				else
				{
					value = s.actionText;
				}

				actionParser.ReInit(new StringReader(value));

				try
				{
					n1 = actionParser.Statement();

					// System.out.println(n1);
					s.node = n1;
				}
				catch (Exception ex)
				{
					if (s.actionBlockVisible)
					{
						s.myActionLabel.setTextColor(red);
					}

					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				// System.out.println("Calling s.node.compile");
				boolean compilationOK = s.node.compile(symbolList);

				if (s.actionBlockVisible)
				{
					if (!compilationOK)
					{
						s.myActionLabel.setTextColor(red);
					}
					else
					{
						s.myActionLabel.setTextColor(black);
					}
				}
			}
		}

		if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
		{
			MacroStep ms = (MacroStep) obj;

			compileDocument(ms.myContentDocument, symbolList);
		}

		if (obj instanceof GrafcetProcedure)
		{
			GrafcetProcedure gp = (GrafcetProcedure) obj;

			compileDocument(gp.myContentDocument, symbolList);
		}

		if (obj instanceof ProcedureStep)
		{
			ProcedureStep ps = (ProcedureStep) obj;

			ps.viewOwner = myCurrentView;
			ps.symbolList = symbolList;

			if (ps.procNode != null)
			{
				actionParser.ReInit(new StringReader(ps.gp));

				try
				{
					n1 = actionParser.ProcCall();
					ps.procNode = n1;
				}
				catch (Exception ex)
				{
					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				ps.procNode.compile(symbolList);
			}
			else
			{
				actionParser.ReInit(new StringReader(ps.gp));

				try
				{
					n1 = actionParser.ProcCall();
					ps.procNode = n1;
				}
				catch (Exception ex)
				{
					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				ps.procNode.compile(symbolList);
			}

			if (ps.paramNode != null)
			{
				actionParser.ReInit(new StringReader(ps.parameters));

				try
				{
					n1 = actionParser.ProcParam();
					ps.paramNode = n1;
				}
				catch (Exception ex)
				{
					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				ps.paramNode.compile(symbolList);
			}
			else
			{
				actionParser.ReInit(new StringReader(ps.parameters));

				try
				{
					n1 = actionParser.ProcParam();
					ps.paramNode = n1;
				}
				catch (Exception ex)
				{
					System.out.println("Oops");
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}

				ps.paramNode.compile(symbolList);
			}
		}
	}

	ArrayList compileDocument(GCDocument doc, ArrayList symbolList)
	{
		ArrayList tempList = new ArrayList();
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{

			// if ((obj instanceof GCStep) || (obj instanceof MacroStep) ||
			// (obj instanceof DigitalIn) || (obj instanceof DigitalOut)
			// || (obj instanceof GrafcetProcedure)) {
			if (obj instanceof Referencable)
			{
				tempList.add(obj);
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}

		tempList.addAll(symbolList);    // adds to the end, i.e. lexical scoping

		pos = doc.getFirstObjectPos();
		obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			compileJGoObject(obj, tempList);

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}

		return tempList;
	}

	void compileAction()
	{
		ArrayList symbolList = topGrafcharts.getStorage();

		compileDocument((GCDocument) myCurrentView.getDocument(), symbolList);

		myCurrentView.compiledOnce = true;

		updateActions();
	}

	void stopAction()
	{
		myCurrentView.stopThread();
		myCurrentView.stopDocument(myCurrentView.getDoc());
		myCurrentView.setDragDropEnabled(true);
		updateActions();
	}

	void exitAction()
	{
		setVisible(false);
		dispose();
		System.exit(0);
	}

	// ------------- Step menu Actions -------------------------
	void showActionBlockAction(ActionEvent e)
	{
		GCStep s = (GCStep) selectedObject;

		s.showActionBlock();

		selectedObject = null;

		updateActions();
	}

	void hideActionBlockAction(ActionEvent e)
	{
		GCStep s = (GCStep) selectedObject;

		s.hideActionBlock();

		selectedObject = null;

		updateActions();
	}

	void stepEditAction(ActionEvent e)
	{
		GCStep s = (GCStep) selectedObject;
		GCView v = myCurrentView;

		if (v != null)
		{
			new StepDialog(v.getFrame(), v.getDoc(), s, v).setVisible(true);
		}

		selectedObject = null;

		updateActions();
	}

	// --------------------- Transition menu Actions -------------------
	void transitionEditAction(ActionEvent e)
	{
		GCTransition t = (GCTransition) selectedObject;
		GCView v = myCurrentView;

		if (v != null)
		{
			new TransitionDialog(v.getFrame(), v.getDoc(), t, v).setVisible(true);
		}

		selectedObject = null;

		updateActions();
	}

	// --------------------- Procedure Step menu Actions -------------------
	void procedureStepEditAction(ActionEvent e)
	{
		ProcedureStep s = (ProcedureStep) selectedObject;
		GCView v = myCurrentView;

		if (v != null)
		{
			new ProcedureStepDialog(v.getFrame(), v.getDoc(), s, v).setVisible(true);
		}

		selectedObject = null;

		updateActions();
	}

	void procedureStepShowAction(ActionEvent e)
	{
		ProcedureStep ps = (ProcedureStep) selectedObject;

		handleProcedureStep(ps);
	}

	// Initialize the View
	public void initView() {}

	public void initPalette()
	{
		JGoDocument paletteDoc = myPalette.getDocument();

		paletteDoc.setSuspendUpdates(true);

		String initString = "Comment";
		JGoText textVar = new JGoText(new Point(45, 25), 14, initString, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar.setTransparent(true);
		textVar.setResizable(true);
		textVar.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar);

		// create one initialstep
		GCStepInitial istep = new GCStepInitial(new Point(45, 80), null);

		paletteDoc.addObjectAtTail(istep);    // add to the document

		String initString1 = "Initial Step";
		JGoText textVar1 = new JGoText(new Point(45, 160), 12, initString1, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar1.setTransparent(true);
		textVar1.setSelectable(false);
		textVar1.setResizable(true);
		textVar1.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar1);

		// create one step and one transition.
		GCStep step1 = new GCStep(new Point(45, 200), null);

		paletteDoc.addObjectAtTail(step1);    // add to the document

		String initString2 = "Step";
		JGoText textVar2 = new JGoText(new Point(45, 280), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar2.setTransparent(true);
		textVar2.setSelectable(false);
		textVar2.setResizable(true);
		textVar2.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar2);

		GCTransition tran1 = new GCTransition(new Point(45, 320), "0");

		paletteDoc.addObjectAtTail(tran1);

		initString2 = "Transition";

		JGoText textVar3 = new JGoText(new Point(45, 345), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar3.setTransparent(true);
		textVar3.setSelectable(false);
		textVar3.setResizable(true);
		textVar3.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar3);

		ParallelSplit ps = new ParallelSplit(new Point(45, 380));

		paletteDoc.addObjectAtTail(ps);

		initString2 = "Parallel Split";

		JGoText textVar4 = new JGoText(new Point(45, 395), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar4.setTransparent(true);
		textVar4.setSelectable(false);
		textVar4.setResizable(true);
		textVar4.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar4);

		ParallelJoin pj = new ParallelJoin(new Point(45, 420));

		paletteDoc.addObjectAtTail(pj);

		initString2 = "Parallel Join";

		JGoText textVar5 = new JGoText(new Point(45, 440), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar5.setTransparent(true);
		textVar5.setSelectable(false);
		textVar5.setResizable(true);
		textVar5.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar5);

		MacroStep ms = new MacroStep(new Point(45, 510), null);

		paletteDoc.addObjectAtTail(ms);

		initString2 = "Macro Step";

		JGoText textVar6 = new JGoText(new Point(45, 560), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar6.setTransparent(true);
		textVar6.setSelectable(false);
		textVar6.setResizable(true);
		textVar6.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar6);

		EnterStep es = new EnterStep(new Point(200, 70), "S1");

		ms.myContentDocument.addObjectAtHead(es);

		ExitStep ex = new ExitStep(new Point(200, 200), "S2");

		ms.myContentDocument.addObjectAtHead(ex);

		ExceptionTransition et = new ExceptionTransition(new Point(45, 600), "0");

		paletteDoc.addObjectAtTail(et);

		initString2 = "Exception Trans.";

		JGoText textVar7 = new JGoText(new Point(45, 630), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar7.setTransparent(true);
		textVar7.setSelectable(false);
		textVar7.setResizable(true);
		textVar7.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar7);

		// GrafcetProcedure gp = new GrafcetProcedure(new Point(45,630), "Proc1");
		// paletteDoc.addObjectAtTail(gp);
		// 
		// es = new EnterStep(new Point(200, 70), "S1");
		// gp.myContentDocument.addObjectAtHead(es);
		// 
		// ex = new ExitStep(new Point(200, 200), "S2");
		// gp.myContentDocument.addObjectAtHead(ex);
		// 
		// ProcedureStep pst = new ProcedureStep(new Point(45,760), "P1");
		// paletteDoc.addObjectAtTail(pst);
		DigitalIn digIn = new DigitalIn(new Point(45, 700));

		paletteDoc.addObjectAtTail(digIn);

		initString2 = "Digital Input";

		JGoText textVar8 = new JGoText(new Point(45, 765), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar8.setTransparent(true);
		textVar8.setSelectable(false);
		textVar8.setResizable(true);
		textVar8.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar8);

		DigitalIn1 digIn1 = new DigitalIn1(new Point(45, 830));

		paletteDoc.addObjectAtTail(digIn1);

		initString2 = "Digital Input";

		JGoText textVar45 = new JGoText(new Point(45, 895), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar45.setTransparent(true);
		textVar45.setSelectable(false);
		textVar45.setResizable(true);
		textVar45.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar45);

		initString2 = "(inverse logic)";

		JGoText textVar46 = new JGoText(new Point(45, 908), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar46.setTransparent(true);
		textVar46.setSelectable(false);
		textVar46.setResizable(true);
		textVar46.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar46);

		DigitalOut0 digOut0 = new DigitalOut0(new Point(45, 975), "0");

		paletteDoc.addObjectAtTail(digOut0);

		initString2 = "Digital Output";

		JGoText textVar9 = new JGoText(new Point(45, 1040), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar9.setTransparent(true);
		textVar9.setSelectable(false);
		textVar9.setResizable(true);
		textVar9.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar9);

		DigitalOut1 digOut1 = new DigitalOut1(new Point(45, 1105), "0");

		paletteDoc.addObjectAtTail(digOut1);

		initString2 = "Digital Output";

		JGoText textVar10 = new JGoText(new Point(45, 1170), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar10.setTransparent(true);
		textVar10.setSelectable(false);
		textVar10.setResizable(true);
		textVar10.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar10);

		initString2 = "(inverse logic)";

		JGoText textVar13 = new JGoText(new Point(45, 1183), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar13.setTransparent(true);
		textVar13.setSelectable(false);
		textVar13.setResizable(true);
		textVar13.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar13);

		BooleanVariable inVar = new BooleanVariable(new Point(45, 1235));

		paletteDoc.addObjectAtTail(inVar);

		initString2 = "Boolean Variable";

		JGoText textVar11 = new JGoText(new Point(45, 1285), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar11.setTransparent(true);
		textVar11.setSelectable(false);
		textVar11.setResizable(true);
		textVar11.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar11);

		IntegerVariable intVar = new IntegerVariable(new Point(45, 1345));

		paletteDoc.addObjectAtTail(intVar);

		initString2 = "Integer Variable";

		JGoText textVar12 = new JGoText(new Point(45, 1395), 12, initString2, "Serif", true, false, false, JGoText.ALIGN_CENTER, false, true);

		textVar12.setTransparent(true);
		textVar12.setSelectable(false);
		textVar12.setResizable(true);
		textVar12.setEditOnSingleClick(true);
		paletteDoc.addObjectAtTail(textVar12);

		// StringVariable strVar = new StringVariable(new Point(45, 1350));
		// paletteDoc.addObjectAtTail(strVar);
		paletteDoc.setSuspendUpdates(false);
	}

	// This applet works as an application too
	public static void main(String[] args)
	{

		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Basic2GC app = new Basic2GC();

		app.updateActions();
		;
	}

	public GCView getCurrentView()
	{
		return myCurrentView;
	}

	static public void updateActions()
	{
		AppAction.updateAllActions();
	}

	public void processViewChange(JGoViewEvent e)
	{
		switch (e.getHint())
		{

		case JGoViewEvent.UPDATE_ALL :
		case JGoViewEvent.SELECTION_GAINED :
		case JGoViewEvent.SELECTION_LOST :
			updateActions();
			break;

		case JGoViewEvent.DOUBLE_CLICKED :
		{
			JGoObject obj = e.getJGoObject();

			while (obj.getParent() != null)
			{
				obj = obj.getParent();
			}

			selectedObject = obj;

			updateActions();
			callDialog(obj, e);

			break;
		}
		}
	}

	public void callDialog(JGoObject obj, JGoViewEvent e)
	{
		if (obj instanceof GCStep)
		{
			Point p = e.getPointViewCoords();

			stepmenu.show(myCurrentView, (int) p.getX(), (int) p.getY());
		}

		if (obj instanceof GCTransition)
		{
			Point p = e.getPointViewCoords();

			transitionmenu.show(myCurrentView, (int) p.getX(), (int) p.getY());
		}

		if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
		{
			MacroStep ms = (MacroStep) obj;

			handleMacroStep(ms);
		}

		if (obj instanceof GrafcetProcedure)
		{
			GrafcetProcedure gp = (GrafcetProcedure) obj;

			handleGrafcetProcedure(gp);
		}

		if (obj instanceof ProcedureStep)
		{
			Point p = e.getPointViewCoords();

			procedurestepmenu.show(myCurrentView, (int) p.getX(), (int) p.getY());

			// handleProcedureStep(ps);
		}

		if (obj instanceof JGoText)
		{
			TextPropsDialog dlg = new TextPropsDialog(myCurrentView.getFrame(), "", true, (JGoText) obj);

			dlg.setVisible(true);
		}
	}

	public void handleGrafcetProcedure(GrafcetProcedure ms)
	{
		if (ms.frame == null)
		{
			ms.myContentDocument.setName(ms.myLabel.getText());

			final JInternalFrame frame = new JInternalFrame(ms.myLabel.getText(), true, true, true, true);

			frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

			final GCView view = new GCView(ms.myContentDocument);

			view.setStepCounter(ms.stepCounterInt);
			view.initialize(this, frame);
			frame.addInternalFrameListener(new InternalFrameListener()
			{
				public void internalFrameActivated(InternalFrameEvent e)
				{
					myCurrentView = view;
					topLevelView = false;

					view.requestFocus();

					// view.getDoc().updateLocationModifiable();
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

			ms.parentView = myCurrentView;

			Container contentPane = frame.getContentPane();

			contentPane.setLayout(new BorderLayout());
			contentPane.add(view);
			frame.setSize(400, 400);
			getDesktop().add(frame);

			Point p = ms.getLocation();

			frame.setLocation((int) p.getX() + 130, (int) p.getY() - 70);
			frame.setClosable(false);
			frame.show();
			view.initializeDragDropHandling();

			myCurrentView = view;
			ms.frame = frame;
			ms.view = view;
			topLevelView = false;

			view.requestFocus();
			AppAction.updateAllActions();
		}
		else
		{
			ms.stepCounterInt = ms.view.getStepCounter();

			try
			{
				ms.frame.setClosed(true);
			}
			catch (Exception x) {}

			ms.frame = null;
			ms.view = null;
			myCurrentView = ms.parentView;

			myCurrentView.requestFocus();
			AppAction.updateAllActions();
		}
	}

	public void handleMacroStep(MacroStep ms)
	{
		if (ms.frame == null)
		{
			ms.myContentDocument.setName(ms.myLabel.getText());

			final JInternalFrame frame = new JInternalFrame(ms.myLabel.getText(), true, true, true, true);

			frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

			final GCView view = new GCView(ms.myContentDocument);

			view.setStepCounter(ms.stepCounterInt);
			view.initialize(this, frame);

			view.layer = myCurrentView.layer + 1;

			frame.addInternalFrameListener(new InternalFrameListener()
			{
				public void internalFrameActivated(InternalFrameEvent e)
				{
					myCurrentView = view;
					topLevelView = false;

					view.requestFocus();

					// view.getDoc().updateLocationModifiable();
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

			ms.parentView = myCurrentView;

			ms.setDraggable(false);

			Container contentPane = frame.getContentPane();

			contentPane.setLayout(new BorderLayout());
			contentPane.add(view);
			getDesktop().add(frame, new Integer(view.layer));

			if (ms.bounds == null)
			{
				frame.setSize(400, 400);

				Point p = ms.getLocation();

				frame.setLocation((int) p.getX() + 130, (int) p.getY() - 70);
			}
			else
			{
				view.setScale(ms.currentScale);
				frame.setBounds(ms.bounds);
			}

			frame.setClosable(false);
			frame.show();
			view.initializeDragDropHandling();

			myCurrentView = view;
			topLevelView = false;

			view.requestFocus();

			ms.frame = frame;
			ms.view = view;

			AppAction.updateAllActions();
		}
		else
		{
			if (!ms.frame.isShowing())
			{
				if (ms.frame.isIcon())
				{
					try
					{
						ms.frame.setIcon(false);
					}
					catch (Exception x) {}
				}

				ms.frame.show();
				ms.view.requestFocus();
			}
			else
			{
				ms.bounds = ms.frame.getBounds();
				ms.currentScale = ms.view.getScale();
				ms.stepCounterInt = ms.view.getStepCounter();

				try
				{
					ms.frame.setClosed(true);
				}
				catch (Exception x) {}

				ms.setDraggable(true);

				ms.frame = null;
				ms.view = null;
				myCurrentView = ms.parentView;

				myCurrentView.requestFocus();
				AppAction.updateAllActions();
			}
		}
	}

	public void handleProcedureStep(ProcedureStep ms)
	{
		if ((ms.frame == null) & (ms.myContentDocument != null))
		{
			ms.myContentDocument.setName("Call to " + ms.myLabel.getText());

			final JInternalFrame frame = new JInternalFrame("Call to " + ms.myLabel.getText(), true, true, true, true);

			frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

			final GCView view = new GCView(ms.myContentDocument);

			view.setStepCounter(ms.stepCounterInt);
			view.initialize(this, frame);
			frame.addInternalFrameListener(new InternalFrameListener()
			{
				public void internalFrameActivated(InternalFrameEvent e)
				{
					myCurrentView = view;
					topLevelView = false;

					view.requestFocus();

					// view.getDoc().updateLocationModifiable();
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

			ms.parentView = myCurrentView;

			Container contentPane = frame.getContentPane();

			contentPane.setLayout(new BorderLayout());
			contentPane.add(view);
			frame.setSize(400, 400);
			getDesktop().add(frame);

			Point p = ms.getLocation();

			frame.setLocation((int) p.getX() + 130, (int) p.getY() - 70);
			frame.setClosable(false);
			frame.show();
			view.initializeDragDropHandling();

			myCurrentView = view;
			ms.frame = frame;
			ms.view = view;
			topLevelView = false;

			view.requestFocus();
			AppAction.updateAllActions();
		}
		else
		{
			if (ms.myContentDocument != null)
			{
				ms.stepCounterInt = ms.view.getStepCounter();

				try
				{
					ms.frame.setClosed(true);
				}
				catch (Exception x) {}

				ms.frame = null;
				ms.view = null;
				myCurrentView = ms.parentView;

				myCurrentView.requestFocus();
				AppAction.updateAllActions();
			}
		}
	}

	public void processDocChange(JGoDocumentEvent e)
	{
		org.jgrafchart.Transitions.SimpleNode n;
		org.jgrafchart.Actions.SimpleNode n1;

		switch (e.getHint())
		{

		case JGoDocumentEvent.CHANGED :
			if ((e.getJGoObject() instanceof JGoText) && (e.getJGoObject().getParent() != null) && (e.getFlags() == JGoText.ChangedText) && (e.getJGoObject().getParent() instanceof GenericTransition))
			{
				JGoText label = (JGoText) e.getJGoObject();
				GenericTransition tran = (GenericTransition) label.getParent();
				String value = label.getText();
				StringReader sr = new StringReader(value);
				boolean parsingOK = true;

				parser.ReInit(sr);

				try
				{
					label.setTextColor(black);

					tran.node = null;
					n = parser.Start();
					tran.node = n;
				}
				catch (Throwable ex)
				{
					parsingOK = false;

					label.setTextColor(red);
					System.out.println("Oops. Nisse");

					// System.out.println(ex.getMessage());
					// ex.printStackTrace();
				}

				if (parsingOK && myCurrentView.executing && (tran.node != null))
				{
					boolean compilationOK = tran.node.compile(new ArrayList());

					if (!compilationOK)
					{
						label.setTextColor(red);
					}
					else
					{
						label.setTextColor(black);
					}

					// if (value.compareTo("0") == 0) {
					// tran.condition = false;
					// } else {
					// tran.condition = true;
					// }
				}
			}

			if ((e.getJGoObject() instanceof JGoText) && (e.getJGoObject().getParent() != null) && (e.getFlags() == JGoText.ChangedText) && (e.getJGoObject().getParent() instanceof DigitalIn))
			{
				JGoText label = (JGoText) e.getJGoObject();
				DigitalIn digIn = (DigitalIn) label.getParent();
				String value = label.getText();

				if (label == digIn.myIntext)
				{
					if (value.compareTo("0") == 0)
					{
						digIn.val = false;
					}
					else
					{
						digIn.val = true;
					}
				}
			}

			if ((e.getJGoObject() instanceof JGoText) && (e.getJGoObject().getParent() != null) && (e.getFlags() == JGoText.ChangedText) && (e.getJGoObject().getParent() instanceof BooleanVariable))
			{
				JGoText label = (JGoText) e.getJGoObject();
				BooleanVariable inVar = (BooleanVariable) label.getParent();
				String value = label.getText();

				if (label == inVar.myValue)
				{
					if (value.compareTo("0") == 0)
					{
						inVar.setStoredBoolAction(false);
					}
					else
					{
						inVar.setStoredBoolAction(true);
					}
				}
			}

			if ((e.getJGoObject() instanceof JGoText) && (e.getJGoObject().getParent() != null) && (e.getFlags() == JGoText.ChangedText) && (e.getJGoObject().getParent() instanceof IntegerVariable))
			{
				JGoText label = (JGoText) e.getJGoObject();
				IntegerVariable inVar = (IntegerVariable) label.getParent();
				String value = label.getText();

				if (label == inVar.myValue)
				{
					inVar.setStoredIntAction(Integer.parseInt(value));
				}
			}

			if ((e.getJGoObject() instanceof JGoText) && (e.getJGoObject().getParent() != null) && (e.getFlags() == JGoText.ChangedText) && (e.getJGoObject().getParent() instanceof StringVariable))
			{
				JGoText label = (JGoText) e.getJGoObject();
				StringVariable inVar = (StringVariable) label.getParent();
				String value = label.getText();

				if (label == inVar.myValue)
				{
					inVar.setStoredStringAction(value);
				}
			}

			// if ((e.getJGoObject() instanceof JGoText) &&
			// (e.getJGoObject().getParent() != null) &&
			// (e.getJGoObject().getParent() instanceof GCStep)) {
			// JGoText label = (JGoText)e.getJGoObject();
			// GCStep s = (GCStep)label.getParent();
			// if (label == s.myActionLabel){
			// String value = label.getText();
			// actionParser.ReInit(new StringReader(value));
			// try {
			// label.setTextColor(black);
			// n1 = actionParser.Statement();
			// s.node = n1;
			// s.node.compile(new ArrayList());
			// } catch (Exception ex) {
			// label.setTextColor(red);
			// System.out.println("Oops.");
			// System.out.println(ex.getMessage());
			// ex.printStackTrace();
			// }
			// }
			// }
			break;
		}
	}

	public JDesktopPane getDesktop()
	{
		return myDesktop;
	}

	public void setCurrentView(GCView v)
	{
		myCurrentView = v;

		myCurrentView.requestFocus();
		AppAction.updateAllActions();
	}

	private JDesktopPane myDesktop;
	public GCView myCurrentView;
	private GCPalette myPalette;
	private JGoDocument paletteDoc;
	private JGoDocument viewDoc;
	protected JMenuBar mainMenuBar = new JMenuBar();
	protected JMenu filemenu = new JMenu();
	protected JMenu viewmenu = new JMenu();
	protected JMenu grafcetmenu = new JMenu();
	protected JPopupMenu stepmenu = new JPopupMenu();
	protected JPopupMenu transitionmenu = new JPopupMenu();
	protected JPopupMenu procedurestepmenu = new JPopupMenu();
	public JGoObject selectedObject = null;
	private String myName = "";
	public TransitionParser parser;
	public ActionParser actionParser;
	static Color red = new Color(1.0f, 0f, 0f);
	static Color black = new Color(0.0f, 0.0f, 0.0f);
	private int myDocCount = 1;
	public boolean topLevelView = false;
	public GrafchartStorage topGrafcharts = new GrafchartStorage();
}
