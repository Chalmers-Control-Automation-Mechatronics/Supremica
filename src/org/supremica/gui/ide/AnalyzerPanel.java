package org.supremica.gui.ide;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.actions.Actions;

public class AnalyzerPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;
    
    private JTabbedPane tabPanel;
    private JComponent automatonViewerPanel;
    private AnalyzerAutomataPanel automataPanel;
    
    private final DocumentContainer mDocumentContainer;
    
    public AnalyzerPanel(DocumentContainer moduleContainer, String name)
    {
        super(name);
        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);
        
        mDocumentContainer = moduleContainer;
        
        tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
        tabPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
        tabPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
        
        automataPanel = new AnalyzerAutomataPanel(this, moduleContainer, "All");
        automataPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
        automataPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
        tabPanel.add(automataPanel);
        
        tabPanel.setSelectedComponent(automataPanel);
        
        automatonViewerPanel = getEmptyRightPanel();
        
        splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, automatonViewerPanel);
        splitPanelHorizontal.setContinuousLayout(false);
        splitPanelHorizontal.setOneTouchExpandable(false);
        splitPanelHorizontal.setDividerLocation(0.2);
        splitPanelHorizontal.setResizeWeight(0.0);
        
        ((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());
        
        add(splitPanelHorizontal);

        // Add CTRL-A as a "Select All" action
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), "SelectAll");
        this.getActionMap().put("SelectAll",
            new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                automataPanel.selectAllAutomata();
            }
        });
    }
    
    /**
     * Gets the selected automata.
     */
    public Automata getSelectedAutomata()
    {
        return automataPanel.getSelectedAutomata();
    }
    
/*
        public Project getSelectedProject()
        {
                return automataPanel.getSelectedProject();
        }
 */
    
    public Automata getUnselectedAutomata()
    {
        return automataPanel.getUnselectedAutomata();
    }
    
    public Automata getAllAutomata()
    {
        return automataPanel.getAllAutomata();
    }
            
    /**
     * Updates the automata in the analyzer-tab.
     */
    public boolean updateAutomata()
    {
        ProjectBuildFromWaters builder = null;
        Project supremicaProject = null;
        try
        {
            final DocumentManager manager = mDocumentContainer.getIDE().getDocumentManager();
            builder = new ProjectBuildFromWaters(manager);
            supremicaProject = builder.build(mDocumentContainer.getEditorPanel().getModuleSubject());
        }
        catch (EvalException eex)
        {
            JOptionPane.showMessageDialog(mDocumentContainer.getIDE(), eex.getMessage(),
                "Error in graph",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(mDocumentContainer.getIDE(), ex.getMessage(),
                "Error in graph",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
        mVisualProject.clear();
        mVisualProject.addAutomata(supremicaProject);
        mVisualProject.updated();
        
        /*
        if (Config.GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE.isTrue())
        {
            ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
            ModuleSubject flatModule = (ModuleSubject) importer.importModule(mVisualProject);
            flatModuleContainer = new ModuleContainer(getIDE(), flatModule);
        }
         */
        
        return true;
    }
    
    //    private ModuleSubject flatModule = null;
    //private ModuleContainer flatModuleContainer = null;
    
    public void addToolBarEntries(IDEToolBar toolBar)
    {
    }
    
    public void disablePanel()
    {
        getActions().enableAnalyzerActions(false);
    }
    
    public void enablePanel()
    {
        getActions().enableAnalyzerActions(true);
    }
    
    public VisualProject getVisualProject()
    {
        return mVisualProject;
    }

    public Actions getActions()
    {
        return mDocumentContainer.getIDE().getActions();
    }

    private VisualProject mVisualProject = new VisualProject();
    
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
            else if (getVisualProject().containsAutomaton(newName))
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
    
    
    public int numberOfSelectedAutomata()
    {
        return getSelectedAutomata().size();
    }
    
    public boolean addAutomaton(Automaton theAutomaton)
    {
        mVisualProject.addAutomaton(theAutomaton);
        return true; // This is not always the correct return value!!!
    }
    
    public int addAutomata(Automata theAutomata)
    {
        mVisualProject.addAutomata(theAutomata);
        return theAutomata.size(); // This is not always the correct return value!!!
    }

}
