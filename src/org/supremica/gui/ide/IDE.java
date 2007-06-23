//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDE
//###########################################################################
//# $Id: IDE.java,v 1.87 2007-06-23 10:16:00 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.MainPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.automata.Project;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
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
    implements ChangeListener, IDEActionInterface, IDEReportInterface, Subject
{

	//#######################################################################
	//# Data Members
    private static Logger logger = LoggerFactory.createLogger(IDE.class);
    private static InterfaceManager interfaceManager;
    
    // Document importing
    private final ModuleProxyFactory mModuleFactory;
    private final JAXBModuleMarshaller mModuleMarshaller;
    private final ProxyUnmarshaller<ModuleProxy> validUnmarshaller;
    private final ProxyUnmarshaller<Project> supremicaUnmarshaller;
    private final ProxyMarshaller<Project> mSupremicaMarshaller;
    private final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller;
    private final DocumentManager mDocumentManager;
    
    // GUI Components
    private JPanel contentPanel;
    private BorderLayout contentLayout;
    private IDEMenuBar menuBar;
    private IDEToolBar ideToolBar;
    private JToolBar currToolBar = null;
    private JTabbedPane tabPanel;
    private JSplitPane splitPanelVertical;
    private LogPanel logPanel;
	private final JFileChooser mFileChooser;

    private ModuleContainers mDocumentContainers;

    // Actions
    private final Collection<Observer> mObservers;
    private final Actions theActions;

    
	//#######################################################################
	//# Static Class Constants
    private static final long serialVersionUID = 1L;
	private static final String IDENAME = "Supremica";
    
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
        //setTitle(getName());
        
        // Create a default module
        mDocumentContainers = new ModuleContainers(this);
        ModuleContainer defaultModuleContainer = createNewModuleContainer();
        defaultModuleContainer.addStandardPropositions();
        add(defaultModuleContainer);
        mDocumentContainers.setActive(defaultModuleContainer);
        
        // Set up a factory for Module:s
        mModuleFactory = ModuleSubjectFactory.getInstance();
        final OperatorTable opTable = CompilerOperatorTable.getInstance();
        mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, opTable);
        supremicaUnmarshaller = new SupremicaUnmarshaller(mModuleFactory);
        mSupremicaMarshaller = new SupremicaMarshaller();
        validUnmarshaller = new ValidUnmarshaller(mModuleFactory, opTable);
        hiscUnmarshaller = new HISCUnmarshaller(mModuleFactory);
        umdesUnmarshaller = new UMDESUnmarshaller(mModuleFactory);
        adsUnmarshaller = new ADSUnmarshaller(mModuleFactory);
        
        // Document management
        mDocumentManager = new DocumentManager();
        // Add marshallers in order of importance (shows up in the file.save dialog)
        mDocumentManager.registerMarshaller(mModuleMarshaller);
        mDocumentManager.registerMarshaller(mSupremicaMarshaller);
        // Add unmarshallers in order of importance (shows up in the file.open dialog)
        mDocumentManager.registerUnmarshaller(mModuleMarshaller);
        mDocumentManager.registerUnmarshaller(supremicaUnmarshaller);
        mDocumentManager.registerUnmarshaller(validUnmarshaller);
        mDocumentManager.registerUnmarshaller(hiscUnmarshaller);
        mDocumentManager.registerUnmarshaller(umdesUnmarshaller);
        mDocumentManager.registerUnmarshaller(adsUnmarshaller);
        
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
        tabPanel = new JTabbedPane();
        tabPanel.addChangeListener(this);
        defaultModuleContainer.addToTabPanel(tabPanel);
        tabPanel.validate();
        logPanel = new LogPanel(this, "Logger");
        splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
        splitPanelVertical.setContinuousLayout(false);
        splitPanelVertical.setOneTouchExpandable(false);
        splitPanelVertical.setDividerLocation(0.8);
        splitPanelVertical.setResizeWeight(1.0);
        contentPanel.add(splitPanelVertical, BorderLayout.CENTER);
        //pack();
        //validate();

		final File startdir = new File(Config.FILE_OPEN_PATH.get());
		mFileChooser = new JFileChooser(startdir);

        // Show comment
        defaultModuleContainer.getEditorPanel().showComment();
        
        logger.info("Supremica version: " + (new Version()).toString());
    }
    
	//#######################################################################
	//# Simple Access
    public String getName()
    {
        return IDENAME;
    }
    
    public Actions getActions()
    {
        return theActions;
    }
    

	//#######################################################################
	//# Document Containers
    public Iterator<DocumentContainer> documentContainerIterator()
    {
        return mDocumentContainers.iterator();
    }
    
    public void add(DocumentContainer container)
    {
        mDocumentContainers.add(container);
    }
    
    public void remove(DocumentContainer moduleContainer)
    {
        DocumentContainer activeModuleContainer = getActiveDocumentContainer();
        DocumentContainer nextModuleContainer = null;
        
        if (activeModuleContainer == moduleContainer)
        {
            if (mDocumentContainers.size() <= 1)
            {
                installContainer(createNewModuleSubject());
                nextModuleContainer = getActiveDocumentContainer(); // Ugly fix
                //nextModuleContainer = createNewModuleContainer();
                //moduleContainers.add(nextModuleContainer);
            }
            else
            {
                nextModuleContainer = mDocumentContainers.getNext(moduleContainer);
            }
        }
        setActive(nextModuleContainer);
        mDocumentContainers.remove(moduleContainer);
    }
    
    public DocumentContainer getActiveDocumentContainer()
    {
        return mDocumentContainers.getActiveModuleContainer();
    }
    
    public void setActive(DocumentContainer documentContainer)
    {
        DocumentContainer oldDocumentContainer = getActiveDocumentContainer();
        if (documentContainer != oldDocumentContainer)
        {
            mDocumentContainers.setActive(documentContainer);
            
            if (oldDocumentContainer != null)
            {
                oldDocumentContainer.rememberSelectedComponent(tabPanel);
                
                // Clear tabPanel
                tabPanel.removeAll();
            }
            
            // Add the panels for this module
            documentContainer.addToTabPanel(tabPanel);
            documentContainer.restoreSelectedComponent(tabPanel);
        }
    }
    
    public ModuleSubject createNewModuleSubject()
    {
        return mDocumentContainers.createNewModuleSubject();
    }
    
    public ModuleContainer createNewModuleContainer()
    {
        return mDocumentContainers.createNewModuleContainer();
    }
    
    public void installContainer(final DocumentProxy document)
    {
        if (document instanceof ModuleSubject)
        {
            installContainer((ModuleSubject) document);
        }
        else if (document instanceof Project)
        {
            if (SupremicaUnmarshaller.validate((Project) document))
            {
                ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
                final ProductDESImporter importer = new ProductDESImporter(factory);
                ModuleProxy module = importer.importModule((ProductDESProxy)document);
                installContainer(module);
            }
            else
            {
                int choice = JOptionPane.showConfirmDialog(getFrame(), "This file contains attributes not supported by the editor.\nDo you want to edit it (and lose the unsupported features)?", "Warning", JOptionPane.YES_NO_OPTION);
                switch (choice)
                {
                    case JOptionPane.YES_OPTION:
                        ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
                        final ProductDESImporter importer = new ProductDESImporter(factory);
                        ModuleProxy module = importer.importModule((ProductDESProxy)document);
                        installContainer(module);
                        break;
                    case JOptionPane.NO_OPTION:
                        installContainer((Project) document);
                        break;
                    default:
                        break;
                }
            }            
        }
        else
            throw new ClassCastException("Bad document type.");
    }
    
    public void installContainer(final ModuleSubject module)
    {
        //installContainer(module);
        final ModuleContainer container = new ModuleContainer(this, module);
        container.addStandardPropositions();
        
        add(container);
        setActive(container);
        container.getEditorPanel().showComment();
    }
    
    public void installContainer(final Project project)
    {
        //installContainer(module);
        final AutomataContainer container = new AutomataContainer(this, project);        
        
        add(container);
        setActive(container);
    }


	//#######################################################################
    //#
    public JFrame getFrame()
    {
        return this;
    }
    
    public IDE getIDE()
    {
        return this;
    }
    
    public DocumentManager getDocumentManager()
    {
        return mDocumentManager;
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
        ideToolBar.add(getActions().newAction);
        ideToolBar.add(getActions().getAction(OpenAction.class));
        ideToolBar.add(getActions().getAction(SaveAction.class));
        ideToolBar.add(getActions().editorPrintAction);
        ideToolBar.addSeparator();
		ideToolBar.add(getActions().getAction(WatersUndoAction.class));
		ideToolBar.add(getActions().getAction(WatersRedoAction.class));
        ideToolBar.addSeparator();
        ideToolBar.add(getActions().editorStopEmbedderAction);
        
        getActiveDocumentContainer().getAnalyzerPanel().addToolBarEntries(ideToolBar);
        getActiveDocumentContainer().getEditorPanel().addToolBarEntries(ideToolBar);
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
	//# Interface javax.swing.event.ChangeListener
    public void stateChanged(final ChangeEvent event)
    {
        getActiveDocumentContainer().updateActiveTab(tabPanel);
        if (editorActive()) {
            menuBar.getEditorMenu().setEnabled(true);
            menuBar.getAnalyzerMenu().setEnabled(false);
        } else if (analyzerActive()) {
            menuBar.getEditorMenu().setEnabled(false);
            menuBar.getAnalyzerMenu().setEnabled(true);
        }

		final Object source = event.getSource();
		final EditorChangedEvent eevent = new MainPanelSwitchEvent(source);
		fireEditorChangedEvent(eevent);
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
	//#
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
        return tabPanel.getSelectedComponent() == getActiveDocumentContainer().getEditorPanel();
    }
    
    public boolean analyzerActive()
    {       
        return tabPanel.getSelectedComponent() == getActiveDocumentContainer().getAnalyzerPanel();
    }
    
    public Project getActiveProject()
    {
        DocumentContainer activeContainer = getActiveDocumentContainer();
        return activeContainer.getAnalyzerPanel().getVisualProject();
    }

    public boolean openFiles(final List<File> filesToOpen)
    {
		final OpenAction action =
			(OpenAction) theActions.getAction(OpenAction.class);
		boolean result = true;
		for (final File file : filesToOpen) {
			if (!action.openFile(file)) {
				result = false;
			}
		}
		return result;
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
