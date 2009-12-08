package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import org.supremica.gui.ide.IDEDimensions;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;


public class SimulatorPanel
  extends MainPanel
{

  // #########################################################################
  // # Constructor

  public SimulatorPanel(final ModuleContainer moduleContainer,
                        final String name)
  {
    super(name);
    mSimulation = new Simulation(moduleContainer);
    mModuleContainer = moduleContainer;
    setupDesktop();
    mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
    mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
    setupAutomata();
    setupEvents();
    setupTrace();
    setLeftComponent(mTabbedPane);
  }


  // #########################################################################
  // # Simple Access
  public Simulation getSimulation()
  {
    return mSimulation;
  }


  // #########################################################################
  // # Auxiliary Methods
  private void setupDesktop()
  {
    mDesktop = new AutomatonDesktopPane(mModuleContainer, mSimulation);
    mDesktop.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
    mDesktop.setMinimumSize(IDEDimensions.rightEditorMinimumSize);
    setRightComponent(mDesktop);
  }

  private void setupAutomata()
  {
    setupAutomataTable();
    final JScrollPane scroll = new JScrollPane(mAutomataTable);
    mAutomataPanel.setLayout(new BorderLayout());
    mAutomataPanel.add(scroll, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    mAutomataPanel.add(buttonPanel, BorderLayout.SOUTH);
    mTabbedPane.addTab("Automata", mAutomataPanel);
  }

  private void setupAutomataTable()
  {
    mAutomataTable = new JTable(new AbstractTunnelTable(mModuleContainer, mSimulation, mDesktop));
    ((AbstractTunnelTable)mAutomataTable.getModel()).attachTable(mAutomataTable);
    final int width = 245; // DEBUG: Arbitrary value: Any value will work, but this is close to the 'normal' value
    mAutomataTable.setDefaultRenderer(mAutomataTable.getColumnClass(1), new SelectedTableCellRenderer(mSimulation, mDesktop));
    if (mAutomataTable.getColumnModel().getColumnCount() != 0)
    {
      mAutomataTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(0).setMaxWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.35));
      mAutomataTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(2).setMaxWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(3).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(3).setMaxWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(4).setPreferredWidth((int)(width * 0.35));
    }
    mAutomataTable.addMouseListener(new AutomatonMouseListener(mSimulation, mAutomataTable, mDesktop));
    mAutomataTable.getTableHeader().setReorderingAllowed(false);
    final ListSelectionModel listMod =  mAutomataTable.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(mAutomataTable);
    mAutomataTable.getTableHeader().addMouseListener(new TableHeaderMouseAdapter(mAutomataTable, mAutomataTable.getTableHeader()));
  }

  private void setupTrace()
  {
    mTraceTree = new TraceJTree(mSimulation, mDesktop, mModuleContainer);
    final JScrollPane scroll = new JScrollPane(mTraceTree);
    mTracePanel.setLayout(new BorderLayout());
    mTracePanel.add(scroll, BorderLayout.CENTER);
    mTabbedPane.addTab("Trace", mTracePanel);
  }

  private void setupEvents()
  {
    mEventsTree = new EventJTree(mSimulation, mDesktop, mModuleContainer);
    final JScrollPane scroll = new JScrollPane(mEventsTree);
    mEventsPanel.setLayout(new BorderLayout());
    mEventsPanel.add(scroll, BorderLayout.CENTER);
    mTabbedPane.addTab("Events", mEventsPanel);
  }

  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private JTabbedPane mTabbedPane = new JTabbedPane();
  private AutomatonDesktopPane mDesktop;
  private final JPanel mAutomataPanel = new JPanel();
  private final JPanel mTracePanel = new JPanel();
  private final JPanel mEventsPanel = new JPanel();
  private final Simulation mSimulation;
  //private final JPanel mTracePanel = new JPanel();
  private JTable mAutomataTable = new JTable();
  private JTree mTraceTree;
  private JTree mEventsTree;
  //private final JScrollPane mScrollPane = new JScrollPane();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}