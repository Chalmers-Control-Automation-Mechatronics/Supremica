//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDE
//###########################################################################
//# $Id: IDE.java,v 1.95 2007-06-29 18:03:25 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.LinkedList;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.automata.Project;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.Utility;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;
import org.supremica.util.ProcessCommandLineArguments;
import org.supremica.Version;
import org.xml.sax.SAXException;


/**
 * The IDE's main window.
 *
 * This class represents the IDE's main window, and provides a central
 * access point to all components of the graphical user interface. It
 * also is the main entry point to the program.
 *
 * @author Knut &Aring;kesson
 */

public class IDE
    extends JFrame
    implements IDEActionInterface, Observer, Subject
{
    
    //#######################################################################
    //# Constructor
    public IDE()
    throws JAXBException, SAXException
    {
        Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
        setTitle(getName());
        
        // Instantiate all actions
        mObservers = new LinkedList<Observer>();
        mActions = new Actions(this);
        
        // Create GUI
        final BorderLayout layout = new BorderLayout();
        final JPanel contents = (JPanel) getContentPane();
        contents.setLayout(layout);
        menuBar = new IDEMenuBar(this);
        setJMenuBar(menuBar);
        mToolBar = new IDEToolBar(this);
        contents.add(mToolBar, BorderLayout.NORTH);
        mBlankPanel = new JPanel();
        mBlankPanel.setPreferredSize(IDEDimensions.mainWindowPreferredSize);
        mLogPanel = new LogPanel(this, "Logger");
        mSplitPaneVertical =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT, mBlankPanel, mLogPanel);
        mSplitPaneVertical.setContinuousLayout(false);
        mSplitPaneVertical.setOneTouchExpandable(false);
        mSplitPaneVertical.setDividerLocation(0.8);
        mSplitPaneVertical.setResizeWeight(1.0);
        contents.add(mSplitPaneVertical, BorderLayout.CENTER);
        
        final File startdir = new File(Config.FILE_OPEN_PATH.get());
        mFileChooser = new JFileChooser(startdir);
        
        // Initialise Document Managers
        mDocumentContainerManager = new DocumentContainerManager(this);
        mDocumentContainerManager.attach(this);
        
        info("Supremica version: " + (new Version()).toString());
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
    
    public IDEToolBar getToolBar()
    {
        return mToolBar;
    }
    
    public Actions getActions()
    {
        return mActions;
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
    //# Listeners
    /**
     * Overriden to exit safely when the application window is closed.
     */
    protected void processWindowEvent(WindowEvent event)
    {
        if (event.getID() == WindowEvent.WINDOW_CLOSING)
        {
            final Action action = mActions.getAction(ExitAction.class);
            final ActionEvent aevent =
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
            action.actionPerformed(aevent);
        }
        else
        {
            super.processWindowEvent(event);
        }
    }
    
    
    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    public void update(final EditorChangedEvent event)
    {
        switch (event.getKind())
        {
            case CONTAINER_SWITCH:
                final DocumentContainer container =
                    mDocumentContainerManager.getActiveContainer();
                if (container == null)
                {
                    mSplitPaneVertical.setTopComponent(mBlankPanel);
                    setTitle(IDENAME);
                }
                else
                {
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
        final List<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy)
        {
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
        LOGGER.error(msg);
    }
    
    public void error(String msg, Throwable t)
    {
        LOGGER.error(msg, t);
    }
    
    public void info(String msg)
    {
        LOGGER.info(msg);
    }
    
    public void debug(String msg)
    {
        LOGGER.debug(msg);
    }
    
    
    //#######################################################################
    //# Main Program
    public static void main(String args[])
    throws Exception
    {
        final List<File> files = ProcessCommandLineArguments.process(args);
        final InterfaceManager manager = InterfaceManager.getInstance();
        manager.initLookAndFeel();
        final IDE ide = new IDE();
        ide.setVisible(true);
        ide.openFiles(files);
    }
    
    
    //#######################################################################
    //# Data Members
    // GUI Components
    private final DocumentContainerManager mDocumentContainerManager;
    private final IDEMenuBar menuBar;
    private final IDEToolBar mToolBar;
    private final JPanel mBlankPanel;
    private final JSplitPane mSplitPaneVertical;
    private final LogPanel mLogPanel;
    private final JFileChooser mFileChooser;
    
    // Actions
    private final Actions mActions;
    private final List<Observer> mObservers;
    
    
    //#######################################################################
    //# Static Class Constants
    private static final long serialVersionUID = 1L;
    private static final String IDENAME = "Supremica";
    private static final Logger LOGGER = LoggerFactory.createLogger(IDE.class);
    
    static
    {
        Config.XML_RPC_ACTIVE.set(false);
        Config.DOT_USE.set(true);
        Config.LOG_TO_CONSOLE.set(false);
        Config.LOG_TO_GUI.set(true);
    }
}
