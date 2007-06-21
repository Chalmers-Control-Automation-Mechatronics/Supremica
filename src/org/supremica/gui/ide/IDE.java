//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDE
//###########################################################################
//# $Id: IDE.java,v 1.84 2007-06-21 15:47:42 flordal Exp $
//###########################################################################

package org.supremica.gui.ide;

import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.valid.ValidUnmarshaller;
import org.supremica.util.ProcessCommandLineArguments;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import net.sourceforge.waters.gui.EditorWindowInterface;


import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.*;
import java.io.File;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import org.supremica.gui.Utility;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.properties.Config;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;

import org.supremica.automata.Automaton;
import org.supremica.log.*;
import org.supremica.Version;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.xml.sax.SAXException;

public class IDE
    extends JFrame
    implements ChangeListener, IDEActionInterface, IDEReportInterface
{
    private static final long serialVersionUID = 1L;
    
    static
    {
        Config.XML_RPC_ACTIVE.set(false);
        Config.DOT_USE.set(true);
        Config.LOG_TO_CONSOLE.set(false);
        Config.LOG_TO_GUI.set(true);
    }
    private static Logger logger = LoggerFactory.createLogger(IDE.class);
    private static InterfaceManager interfaceManager;
    
    // Document importing
    private final ModuleProxyFactory mModuleFactory;
    private final JAXBModuleMarshaller mModuleMarshaller;
    private final ProxyUnmarshaller<ModuleProxy> validUnmarshaller;
    private final ProxyUnmarshaller<DocumentProxy> supremicaUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller;
    private final DocumentManager mDocumentManager;
    
    private Actions theActions;
    
    private JPanel contentPanel;
    private BorderLayout contentLayout;
    
    private IDEMenuBar menuBar;
    private IDEToolBar ideToolBar;
    private JToolBar currToolBar = null;
    
    private ModuleContainers mDocumentContainers;
    
    private LogPanel logPanel;
    
    private JTabbedPane tabPanel;
    private JSplitPane splitPanelVertical;
    
    private static final String IDENAME = "Supremica";
    
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
        validUnmarshaller = new ValidUnmarshaller(mModuleFactory, opTable);
        hiscUnmarshaller = new HISCUnmarshaller(mModuleFactory);
        umdesUnmarshaller = new UMDESUnmarshaller(mModuleFactory);
        adsUnmarshaller = new ADSUnmarshaller(mModuleFactory);
        
        // Document management
        mDocumentManager = new DocumentManager();
        // Add marshallers in order of importance (shows up in the file.save dialog)
        mDocumentManager.registerMarshaller(mModuleMarshaller);
        // Add unmarshallers in order of importance (shows up in the file.open dialog)
        mDocumentManager.registerUnmarshaller(mModuleMarshaller);
        mDocumentManager.registerUnmarshaller(supremicaUnmarshaller);
        mDocumentManager.registerUnmarshaller(validUnmarshaller);
        mDocumentManager.registerUnmarshaller(hiscUnmarshaller);
        mDocumentManager.registerUnmarshaller(umdesUnmarshaller);
        mDocumentManager.registerUnmarshaller(adsUnmarshaller);
        
        // Instantiate all actions
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
        
        // Show comment
        defaultModuleContainer.getEditorPanel().showComment();
        
        logger.info("Supremica version: " + (new Version()).toString());
    }
    
    public Actions getActions()
    {
        return theActions;
    }
    
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
    
    public String getName()
    {
        return IDENAME;
    }
    
    public ModuleSubject createNewModuleSubject()
    {
        return mDocumentContainers.createNewModuleSubject();
    }
    
    public ModuleContainer createNewModuleContainer()
    {
        return mDocumentContainers.createNewModuleContainer();
    }
    
    //###################################################################
    //# Auxiliary Methods
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
        ideToolBar.add(getActions().openAction);
        //ideToolBar.add(getActions().saveAction);
        ideToolBar.add(getActions().saveAsAction);
        ideToolBar.add(getActions().editorPrintAction);
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
    
    /**
     * ChangeListener interface
     */
    public void stateChanged(ChangeEvent e)
    {
        getActiveDocumentContainer().updateActiveTab(tabPanel);
        
        if (editorActive())
        {
            menuBar.getEditorMenu().setEnabled(true);//.enable();
            menuBar.getAnalyzerMenu().setEnabled(false);//.disable();
        }
        else if (analyzerActive())
        {
            menuBar.getEditorMenu().setEnabled(false);//.disable();
            menuBar.getAnalyzerMenu().setEnabled(true);//.enable();
        }
        
        repaint();
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
    
    public boolean openFiles(List<File> filesToOpen)
    {
        ((OpenAction)theActions.openAction).doAction(filesToOpen);
        return true;
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
    
    public static void main(String args[])
    throws Exception
    {
        List<File> filesToOpen = ProcessCommandLineArguments.process(args);
        
        interfaceManager = InterfaceManager.getInstance();
        interfaceManager.initLookAndFeel();
        
        IDE ide = new IDE();
        ide.setVisible(true);
        ide.openFiles(filesToOpen);
    }
}
