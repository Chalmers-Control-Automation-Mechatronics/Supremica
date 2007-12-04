//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDE
//###########################################################################
//# $Id: IDE.java,v 1.108 2007-12-04 03:22:58 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.apps.SupremicaWithGui;
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
import org.supremica.comm.xmlrpc.Server;
import org.xml.sax.SAXException;

/**
 * The IDE's main window.
 *
 * This class represents the IDE's main window, and provides a central
 * access point to all components of the graphical user interface. It
 * also is the main entry point to the program.
 *
 * @author Knut &Aring;kesson, Robi Malik
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
        // Initialise logging
        LoggerFactory.getInstance().initialiseAppenders();        
        info("Supremica version: " + (new Version()));
                
        // Instantiate all actions
        mObservers = new LinkedList<Observer>();
        mFocusTracker = new FocusTracker(this);
        mActions = new Actions(this);
        mPopupActionManager = new WatersPopupActionManager(this);
        mFocusTracker.attach(this);

        // Create GUI
        Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
        setTitle(getName());
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
        
        final File startdir = new File(Config.FILE_OPEN_PATH.getAsString());
        mFileChooser = new JFileChooser(startdir);
        
        // Initialise document managers
        mDocumentContainerManager = new DocumentContainerManager(this);
        mDocumentContainerManager.attach(this);

        // Initialise XML_RPC
        if (Config.XML_RPC_ACTIVE.isTrue())
        {
            try
            {
                new Server(this, Config.XML_RPC_PORT.get());
                info("XML-RPC server running on port " +
                     Config.XML_RPC_PORT.get());
            }
            catch (final Exception exception)
            {
                warn("Another server already running on port " +
                     Config.XML_RPC_PORT.get() +
                     ". XML-RPC server not started!");
            }
        }
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

    public FocusTracker getFocusTracker()
    {
        return mFocusTracker;
    }

    public WatersPopupActionManager getPopupActionManager()
    {
        return mPopupActionManager;
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
                }
                else
                {
                    final Component panel = container.getPanel();
                    mSplitPaneVertical.setTopComponent(panel);
                }
                setTitle(getWindowTitle());
                break;
            default:
                break;
        }
        fireEditorChangedEvent(event);
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
    
    private void openEmptyDocument()
    {
        mDocumentContainerManager.newModuleContainer();
    }
    
    public String getWindowTitle()
    {
        final DocumentContainer container =
            mDocumentContainerManager.getActiveContainer();
        if (container == null)
        {
            return IDENAME;
        }
        else
        {
            final DocumentProxy doc = container.getDocument();
            final String name = doc.getName();
            final File file = container.getFileLocation();
            final StringBuffer buffer = new StringBuffer(IDENAME + " - Module");
            if (name != null && !name.equals(""))
            {
                buffer.append(": ");
                buffer.append(name);
            }
            if (file != null)
            {
                buffer.append(" [");
                buffer.append(file);
                buffer.append(']');
            }
            return buffer.toString();
        }
        
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
    
    public void warn(String msg)
    {
        logger.warn(msg);
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
        // Process command line arguments
        final List<File> files = ProcessCommandLineArguments.process(args);
        
        // Now start the gui...
        logger = LoggerFactory.createLogger(IDE.class);
        InterfaceManager.getInstance().initLookAndFeel();
        final IDE ide = new IDE();

        // Open initial module(s)
        if (files != null && files.size() > 0)
        {
            ide.openFiles(files);
        }
        else if (Config.GUI_EDITOR_DEFAULT_EMPTY_MODULE.isTrue())
        {
            ide.openEmptyDocument();
        }
        
        // Show!
        ide.setVisible(true);
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
    private final FocusTracker mFocusTracker;
    private final Actions mActions;
    private final WatersPopupActionManager mPopupActionManager;
    private final List<Observer> mObservers;

    // Logger. Must not be initialised until ProcessCommandLineArguments
    // has finished (or messages WILL disappear).  Try running "IDE -h" and
    // "IDE", _both_ should give output, to console and logdisplay,
    // respectively.
    private static Logger logger = null;
    
    
    //#######################################################################
    //# Static Class Constants
    private static final long serialVersionUID = 1L;
    private static final String IDENAME = "Supremica";
    //private static final InterfaceManager manager;
    
    static
    {
        Locale.setDefault(Locale.ENGLISH);
        Config.XML_RPC_ACTIVE.set(false);
        Config.DOT_USE.set(true);
        Config.LOG_TO_CONSOLE.set(false);
        Config.LOG_TO_GUI.set(true);
    }
}
