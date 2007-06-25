//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDE
//###########################################################################
//# $Id: IDE.java,v 1.89 2007-06-25 07:42:27 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.automata.Project;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.Utility;
import org.supremica.log.*;
import org.supremica.properties.Config;
import org.supremica.util.ProcessCommandLineArguments;
import org.supremica.Version;
import org.xml.sax.SAXException;


public class IDE
    extends JFrame
    implements IDEActionInterface, Observer, Subject
{

	//#######################################################################
	//# Data Members
    // GUI Components
	private final DocumentContainerManager mDocumentContainerManager;
    private JPanel contentPanel;
    private BorderLayout contentLayout;
    private IDEMenuBar menuBar;
    private IDEToolBar ideToolBar;
    private JToolBar currToolBar = null;
	private final JPanel mBlankPanel;
    private final JSplitPane mSplitPaneVertical;
    private final LogPanel mLogPanel;
	private final JFileChooser mFileChooser;

    // Actions
    private final Collection<Observer> mObservers;
    private final Actions theActions;

    
	//#######################################################################
	//# Static Class Constants
    private static final long serialVersionUID = 1L;
	private static final String IDENAME = "Supremica";
    private static final Logger logger = LoggerFactory.createLogger(IDE.class);
    private static InterfaceManager interfaceManager;

    static
    {
        Config.XML_RPC_ACTIVE.set(false);
        Config.DOT_USE.set(true);
        Config.LOG_TO_CONSOLE.set(false);
        Config.LOG_TO_GUI.set(true);
    }


	//#######################################################################
	//# Constructor
    public IDE()
		throws JAXBException, SAXException
    {
        Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
        setTitle(getName());
        
        // Instantiate all actions
		mObservers = new LinkedList<Observer>();
        theActions = new Actions(this);
        
        // Create GUI
        contentPanel = (JPanel)getContentPane();
        contentLayout = new BorderLayout();
        contentPanel.setLayout(contentLayout);
        menuBar = new IDEMenuBar(this);
        setJMenuBar(menuBar);
        setToolBar(createToolBar());
		mBlankPanel = new JPanel();
        mLogPanel = new LogPanel(this, "Logger");
        mSplitPaneVertical =
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, mBlankPanel, mLogPanel);
        mSplitPaneVertical.setContinuousLayout(false);
        mSplitPaneVertical.setOneTouchExpandable(false);
        mSplitPaneVertical.setDividerLocation(0.8);
        mSplitPaneVertical.setResizeWeight(1.0);
        contentPanel.add(mSplitPaneVertical, BorderLayout.CENTER);

		final File startdir = new File(Config.FILE_OPEN_PATH.get());
		mFileChooser = new JFileChooser(startdir);

        // Initialise Document Managers
        mDocumentContainerManager = new DocumentContainerManager(this);
		mDocumentContainerManager.attach(this);
		mDocumentContainerManager.newModuleContainer();

		// *** BUG ***
		// Toolbar must be set up without document loaded ...
        getActiveDocumentContainer().getAnalyzerPanel().
			addToolBarEntries(ideToolBar);
        getActiveDocumentContainer().getEditorPanel().
			addToolBarEntries(ideToolBar);
		// ***
        
        logger.info("Supremica version: " + (new Version()).toString());
    }

    
	//#######################################################################
	//# Simple Access
    public String getName()
    {
        return IDENAME;
    }

    public JFrame getFrame()
    {
        return this;
    }

    public IDE getIDE()
    {
        return this;
    }

    public Actions getActions()
    {
        return theActions;
    }

	public DocumentContainerManager getDocumentContainerManager()
	{
		return mDocumentContainerManager;
	}

    public DocumentManager getDocumentManager()
    {
        return mDocumentContainerManager.getDocumentManager();
    }

	public JFileChooser getFileChooser()
	{
		return mFileChooser;
	}


	//#######################################################################
    //# Toolbar
    private void setToolBar(JToolBar toolBar)
    {
        if (toolBar == null)
        {
            return;
        }
        if (toolBar == currToolBar)
        {
            return;
        }
        if (currToolBar != null)
        {
            contentPanel.remove(currToolBar);
        }
        contentPanel.add(toolBar, BorderLayout.NORTH);
        currToolBar = toolBar;
    }
    
    private IDEToolBar createToolBar()
    {
        ideToolBar = new IDEToolBar(this);
        
        // Set standard actions
        ideToolBar.add(getActions().getAction(NewAction.class));
        ideToolBar.add(getActions().getAction(OpenAction.class));
        ideToolBar.add(getActions().getAction(SaveAction.class));
        ideToolBar.add(getActions().editorPrintAction);
        ideToolBar.addSeparator();
		ideToolBar.add(getActions().getAction(WatersUndoAction.class));
		ideToolBar.add(getActions().getAction(WatersRedoAction.class));
        ideToolBar.addSeparator();
        ideToolBar.add(getActions().editorStopEmbedderAction);
        return ideToolBar;
    }
    
    public IDEToolBar getToolBar()
    {
        return ideToolBar;
    }

    
	//#######################################################################
	//# Listeners
    /**
     * Overridden so we can exit when window is closed
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            getActions().exitAction.doAction();
        }
    }

    
	//#######################################################################
	//# Interface net.sourceforge.waters.gui.observer.Observer
	public void update(final EditorChangedEvent event)
	{
		switch (event.getKind()) {
		case CONTAINER_SWITCH:
			final DocumentContainer container =
				mDocumentContainerManager.getActiveContainer();
			if (container == null) {
				mSplitPaneVertical.setTopComponent(mBlankPanel);
				setTitle(IDENAME);
			} else {
				final Component panel = container.getPanel();
				mSplitPaneVertical.setTopComponent(panel);
				final String title = container.getWindowTitle();
				setTitle(title);
			}
			break;
		default:
			break;
		}
	}


	//#######################################################################
	//# Interface net.sourceforge.waters.gui.observer.Subject
    public void attach(final Observer observer)
    {
        mObservers.add(observer);
    }

    public void detach(final Observer observer)
    {
        mObservers.remove(observer);
    }

    public void fireEditorChangedEvent(final EditorChangedEvent event)
    {
		// Just in case they try to register or deregister observers
		// in response to the update ...
		final Collection<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy) {
            observer.update(event);
        }
    }


	//#######################################################################
	//# Public Shortcuts
	//# (use with caution --- these should be considered as deprecated)
	public DocumentContainer getActiveDocumentContainer()
	{
		return mDocumentContainerManager.getActiveContainer();
	}

    public void setEditorMode(IDEAction theAction)
    {
        ideToolBar.setCommand((String)theAction.getValue(Action.ACTION_COMMAND_KEY));
    }
    
    public EditorWindowInterface getActiveEditorWindowInterface()
    {
        return getActiveDocumentContainer().getEditorPanel().getActiveEditorWindowInterface();
    }
    
    public boolean editorActive()
    {
        final DocumentContainer active =
			mDocumentContainerManager.getActiveContainer();
        return active != null && active.isEditorActive();
    }
    
    public boolean analyzerActive()
    {       
        final DocumentContainer active =
			mDocumentContainerManager.getActiveContainer();
        return active != null && active.isAnalyzerActive();
    }
    
    public Project getActiveProject()
    {
        final DocumentContainer active =
			mDocumentContainerManager.getActiveContainer();
        return active.getAnalyzerPanel().getVisualProject();
    }

    private boolean openFiles(final List<File> filesToOpen)
    {
		return mDocumentContainerManager.openContainers(filesToOpen);
    }


	//#######################################################################
	//# Interface org.supremica.gui.ide.IDEReportInterface
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
    

	//#######################################################################
	//# Main Program
    public static void main(String args[])
		throws Exception
    {
        List<File> filesToOpen = ProcessCommandLineArguments.process(args);
        
        interfaceManager = InterfaceManager.getInstance();
        interfaceManager.initLookAndFeel();
        
        final IDE ide = new IDE();
        ide.setVisible(true);
        ide.openFiles(filesToOpen);
    }
}
