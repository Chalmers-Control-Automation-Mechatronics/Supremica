package org.supremica.gui.ide;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.supremica.gui.TableSorter;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.AnalyzerAutomataPanel;

class SimulatorPanel
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

    public SimulatorPanel(AutomataContainer automataContainer, String name)
    {
      super(name);
      throw new UnsupportedOperationException();
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
      setupAutomataTable();
      mAutomataPanel.add(mAutomataTable);
      mTabbedPane.add(mAutomataPanel);
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