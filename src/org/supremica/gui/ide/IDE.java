package org.supremica.gui.ide;

import javax.xml.bind.JAXBException;
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
    private final ProxyUnmarshaller<ModuleProxy> supremicaUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller;
    private final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller;
    private final DocumentManager documentManager;
    
    private Actions theActions;
    
    private JPanel contentPanel;
    private BorderLayout contentLayout;
    
    private IDEMenuBar menuBar;
    private IDEToolBar ideToolBar;
    private JToolBar currToolBar = null;
    
    private ModuleContainers moduleContainers;
    
    private LogPanel logPanel;
    
    private JTabbedPane tabPanel;
    private JSplitPane splitPanelVertical;
    
    private final String ideName = "Supremica";
    
    public IDE()
    throws JAXBException, SAXException
    {
        Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
        setTitle(getName());
        moduleContainers = new ModuleContainers(this);
        
        ModuleContainer defaultModule = createNewModuleContainer();
        defaultModule.addStandardPropositions();
        moduleContainers.add(defaultModule);
        moduleContainers.setActive(defaultModule);
        
        contentPanel = (JPanel)getContentPane();
        contentLayout = new BorderLayout();
        contentPanel.setLayout(contentLayout);
        
        documentManager = new DocumentManager();
        mModuleFactory = ModuleSubjectFactory.getInstance();
        final OperatorTable opTable = CompilerOperatorTable.getInstance();
        mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, opTable);
        supremicaUnmarshaller = new SupremicaUnmarshaller(mModuleFactory);
        validUnmarshaller = new ValidUnmarshaller(mModuleFactory, opTable);
        hiscUnmarshaller = new HISCUnmarshaller(mModuleFactory);
        umdesUnmarshaller = new UMDESUnmarshaller(mModuleFactory);
        adsUnmarshaller = new ADSUnmarshaller(mModuleFactory);
        documentManager.registerMarshaller(mModuleMarshaller);
        // Add unmarshallers in the order of importance
        documentManager.registerUnmarshaller(mModuleMarshaller);
        documentManager.registerUnmarshaller(supremicaUnmarshaller);
        documentManager.registerUnmarshaller(validUnmarshaller);
        documentManager.registerUnmarshaller(hiscUnmarshaller);
        documentManager.registerUnmarshaller(umdesUnmarshaller);
        documentManager.registerUnmarshaller(adsUnmarshaller);
        
        theActions = new Actions(this);
        
        menuBar = new IDEMenuBar(this);
        setJMenuBar(menuBar);
        
        setToolBar(createToolBar());
        
        tabPanel = new JTabbedPane();
        tabPanel.addChangeListener(this);
        
        ModuleContainer currModuleContainer = moduleContainers.getActiveModuleContainer();
        tabPanel.add(currModuleContainer.getEditorPanel());
        tabPanel.add(currModuleContainer.getAnalyzerPanel());
        //tabPanel.add(currModuleContainer.getSimulatorPanel());
        
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
        
        logger.info("Supremica version: " + (new Version()).toString());
    }
    
    public Actions getActions()
    {
        return theActions;
    }
    
    public Iterator moduleContainerIterator()
    {
        return moduleContainers.iterator();
    }
    
    public void add(ModuleContainer moduleContainer)
    {
        moduleContainers.add(moduleContainer);
    }
    
    public void remove(ModuleContainer moduleContainer)
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        ModuleContainer nextModuleContainer = null;
        
        if (activeModuleContainer == moduleContainer)
        {
            if (moduleContainers.size() <= 1)
            {
                installContainer(createNewModuleSubject());
                nextModuleContainer = getActiveModuleContainer(); // Ugly fix
                //nextModuleContainer = createNewModuleContainer();
                //moduleContainers.add(nextModuleContainer);
            }
            else
            {
                nextModuleContainer = moduleContainers.getNext(moduleContainer);
            }
        }
        setActive(nextModuleContainer);
        moduleContainers.remove(moduleContainer);
    }
    
    public ModuleContainer getActiveModuleContainer()
    {
        return moduleContainers.getActiveModuleContainer();
    }
    
    public void setActive(ModuleContainer moduleContainer)
    {
        ModuleContainer oldModuleContainer = getActiveModuleContainer();
        if (moduleContainer != oldModuleContainer)
        {
            moduleContainers.setActive(moduleContainer);
            
            oldModuleContainer.setSelectedComponent(tabPanel.getSelectedComponent());
            
            tabPanel.remove(oldModuleContainer.getEditorPanel());
            tabPanel.remove(oldModuleContainer.getAnalyzerPanel());
            //tabPanel.remove(oldModuleContainer.getSimulatorPanel());
            
            tabPanel.add(moduleContainer.getEditorPanel());
            tabPanel.add(moduleContainer.getAnalyzerPanel());
            //tabPanel.add(moduleContainer.getSimulatorPanel());
            
            tabPanel.setSelectedComponent(moduleContainer.getSelectedComponent());
        }
    }
    
    public String getName()
    {
        return ideName;
    }
    
    public ModuleSubject createNewModuleSubject()
    {
        return moduleContainers.createNewModuleSubject();
    }
    
    public ModuleContainer createNewModuleContainer()
    {
        return moduleContainers.createNewModuleContainer();
    }
    
    //###################################################################
    //# Auxiliary Methods
    public void installContainer(final ModuleSubject module)
    {
        installContainer(module, true);
    }
    
    public void installContainer(final ModuleSubject module, boolean showComment)
    {
        final ModuleContainer moduleContainer = new ModuleContainer(this, module);
        moduleContainer.addStandardPropositions();
        
        add(moduleContainer);
        setActive(moduleContainer);
        if (showComment)
        {
            moduleContainer.getEditorPanel().showComment();
        }
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
        return documentManager;
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
        ideToolBar.add(getActions().saveAction);
        ideToolBar.add(getActions().editorPrintAction);
        ideToolBar.addSeparator();
        ideToolBar.add(getActions().editorStopEmbedderAction);
        
        getActiveModuleContainer().getAnalyzerPanel().addToolBarEntries(ideToolBar);
        getActiveModuleContainer().getEditorPanel().addToolBarEntries(ideToolBar);
        return ideToolBar;
    }
    
    public IDEToolBar getToolBar()
    {
        return ideToolBar;
    }
    
    // Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            getActions().exitAction.doAction();
            System.exit(0);
        }
    }
    
    // ChangeListener interface
    public void stateChanged(ChangeEvent e)
    {
        if (editorActive())
        {
            getActiveModuleContainer().getEditorPanel().enablePanel();
            getActiveModuleContainer().getAnalyzerPanel().disablePanel();
            
            menuBar.getEditorMenu().setEnabled(true);//.enable();
            menuBar.getAnalyzerMenu().setEnabled(false);//.disable();
        }
        if (analyzerActive())
        {
            if (getActiveModuleContainer().updateAutomata())
            {
                getActiveModuleContainer().getEditorPanel().disablePanel();
                getActiveModuleContainer().getAnalyzerPanel().enablePanel();
            }
            else
            {
                tabPanel.setSelectedComponent(getActiveModuleContainer().getEditorPanel());
            }

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
        return getActiveModuleContainer().getActiveEditorWindowInterface();
    }
    
    public boolean editorActive()
    {
        return tabPanel.getSelectedComponent() == getActiveModuleContainer().getEditorPanel();
    }
    
    public boolean analyzerActive()
    {
        return tabPanel.getSelectedComponent() == getActiveModuleContainer().getAnalyzerPanel();
    }
    
    public int numberOfSelectedAutomata()
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        return activeModuleContainer.numberOfSelectedAutomata();
    }
    
    public Automata getSelectedAutomata()
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        return activeModuleContainer.getSelectedAutomata();
    }
    
    public Project getActiveProject()
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        return activeModuleContainer.getVisualProject();
    }
    
    public Automata getAllAutomata()
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        return activeModuleContainer.getAllAutomata();
    }
    
    public Automata getUnselectedAutomata()
    {
        ModuleContainer activeModuleContainer = getActiveModuleContainer();
        return activeModuleContainer.getUnselectedAutomata();
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
    
    public boolean addAutomaton(Automaton theAutomaton)
    {
        return getActiveModuleContainer().addAutomaton(theAutomaton);
    }
    
    public int addAutomata(Automata theAutomata)
    {
        return getActiveModuleContainer().addAutomata(theAutomata);
    }
    
    public String getNewAutomatonName(String msg, String nameSuggestion)
    {
        boolean finished = false;
        String newName = "";
        
        while (!finished)
        {
            newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name.", JOptionPane.QUESTION_MESSAGE, null, null, nameSuggestion);
            
            if (newName == null)
            {
                return null;
            }
            else if (newName.equals(""))
            {
                JOptionPane.showMessageDialog(this, "An empty name is not allowed.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else if (getActiveModuleContainer().getVisualProject().containsAutomaton(newName))
            {
                JOptionPane.showMessageDialog(this, "'" + newName + "' already exists.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                finished = true;
            }
        }
        
        return newName;
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
