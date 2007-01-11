package org.supremica.gui.ide;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import org.supremica.automata.Automata;


public class AnalyzerPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;
    
    private JTabbedPane tabPanel;
    private JComponent automatonViewerPanel;
    private AnalyzerAutomataPanel automataPanel;
    
    public AnalyzerPanel(ModuleContainer moduleContainer, String name)
    {
        super(moduleContainer, name);
        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);
        
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

        /*
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), "SelectAll");
        this.getActionMap().put("SelectAll",
            new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.err.println("Select all!");
                automataPanel.selectAllAutomata();
            }
        });
         */
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
    
    
    public void addToolBarEntries(IDEToolBar toolBar)
    {
    }
    
    public void disablePanel()
    {
    }
    
    public void enablePanel()
    {
    }
}
