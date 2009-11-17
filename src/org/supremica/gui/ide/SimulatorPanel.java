package org.supremica.gui.ide;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.supremica.gui.WhiteScrollPane;

class SimulatorPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;

    private final String name;

    SimulatorPanel(DocumentContainer moduleContainer, String name)
    {
      super(name);
      this.name = name;
      mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
      mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
      mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
      setLeftComponent(mTabbedPane);
      setRightComponent(mDesktop);

    }

    public String getName()
    {
        return name;
    }

    private void setupAutomataTable()
    {
      throw new UnsupportedOperationException();
      //mAutomataTable = new JTable(dataModel);
      //mScrollPane = new JScrollPane(mAutomataTable);

    }

    private void setupAutomata()
    {

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