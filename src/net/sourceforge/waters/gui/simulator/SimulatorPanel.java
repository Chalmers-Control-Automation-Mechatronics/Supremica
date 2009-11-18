package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.supremica.gui.ide.IDEDimensions;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;

public class SimulatorPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final ModuleContainer moduleContainer;

    public SimulatorPanel(ModuleContainer moduleContainer, String name)
    {
      super(name);
      this.name = name;
      this.moduleContainer = moduleContainer;
      mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
      mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
      mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
      setupAutomata();
      setLeftComponent(mTabbedPane);
      mDesktop.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
      mDesktop.setMinimumSize(IDEDimensions.rightEditorMinimumSize);
      setRightComponent(mDesktop);
      //JInternalFrame test = new JInternalFrame();
    }

    public String getName()
    {
        return name;
    }

    private void setupAutomataTable()
    {
      mAutomataTable = new JTable(new AbstractTunnelTable(moduleContainer));
    }

    private void setupAutomata()
    {
      JScrollPane panel = new JScrollPane();
      setupAutomataTable();
      mAutomataPanel.add(mAutomataTable, BorderLayout.CENTER);
      JButton stepButton = new JButton("Step");
      mAutomataPanel.add(stepButton, BorderLayout.SOUTH);
      panel.add(mAutomataPanel);
      mTabbedPane.addTab("Automata", panel);
    }


  //#######################################################################
    //# Data Members
    JTabbedPane mTabbedPane = new JTabbedPane();
    JDesktopPane mDesktop = new JDesktopPane();
    JPanel mAutomataPanel = new JPanel();
    JPanel mEventsPanel = new JPanel();
    JPanel mTracePanel = new JPanel();
    JTable mAutomataTable = new JTable();
    JTable mEventsTable = new JTable();
    JScrollPane mScrollPane = new JScrollPane();
}