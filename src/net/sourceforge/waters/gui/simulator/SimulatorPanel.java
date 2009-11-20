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

  public SimulatorPanel(final ModuleContainer moduleContainer,
                        final String name)
  {
    super(name);
    mModuleContainer = moduleContainer;
    mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
    mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
    setupAutomata();
    setupEvents();
    setLeftComponent(mTabbedPane);
    mDesktop.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
    mDesktop.setMinimumSize(IDEDimensions.rightEditorMinimumSize);
    setRightComponent(mDesktop);
    //JInternalFrame test = new JInternalFrame();
  }

  private void setupAutomata()
  {
    setupAutomataTable();
    final JScrollPane scroll = new JScrollPane(mAutomataTable);
    mAutomataPanel.setLayout(new BorderLayout());
    mAutomataPanel.add(scroll, BorderLayout.CENTER);
    JButton stepButton = new JButton("Step");
    stepButton.addActionListener(new TunnelActionListener(mAutomataTable , mModuleContainer));
    mAutomataPanel.add(stepButton, BorderLayout.SOUTH);
    mTabbedPane.addTab("Automata", mAutomataPanel);
  }

  private void setupAutomataTable()
  {
    mAutomataTable = new JTable(new AbstractTunnelTable(mModuleContainer));
  }

  private void setupEvents()
  {
    setupEventsTable();
    final JScrollPane scroll = new JScrollPane(mEventsTable);
    mEventsPanel.setLayout(new BorderLayout());
    mEventsPanel.add(scroll, BorderLayout.CENTER);
    JButton stepButton = new JButton("Step");
    stepButton.addActionListener(new TunnelActionListener(mEventsTable , mModuleContainer));
    mEventsPanel.add(stepButton, BorderLayout.SOUTH);
    mTabbedPane.addTab("Events", mEventsPanel);
  }

  private void setupEventsTable()
  {
    mEventsTable = new JTable(new EventTableModel(mModuleContainer));
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private JTabbedPane mTabbedPane = new JTabbedPane();
  private final JDesktopPane mDesktop = new JDesktopPane();
  private final JPanel mAutomataPanel = new JPanel();
  private final JPanel mEventsPanel = new JPanel();
  //private final JPanel mTracePanel = new JPanel();
  private JTable mAutomataTable = new JTable();
  private JTable mEventsTable = new JTable();
  //private final JScrollPane mScrollPane = new JScrollPane();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;


  public void updateAutomata()
  {
    ((AbstractTunnelTable)mAutomataTable.getModel()).update();
  }

  public void updateEvents()
  {
    ((EventTableModel)mEventsTable.getModel()).update();
  }

}