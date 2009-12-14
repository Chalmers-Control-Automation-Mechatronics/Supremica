package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import org.supremica.gui.WhiteScrollPane;
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
    final WhiteScrollPane pane = new WhiteScrollPane(mAutomataTable);
    mTabbedPane.addTab("Automata", pane);
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
    mAutomataTable.setShowGrid(!DISABLE_AUTOMATON_GRIDLINES);
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
    final SorterButton typeButton = new SorterButton("Type", mEventsTree, 0);
    final SorterButton nameButton = new SorterButton("Name", mEventsTree, 1);
    final SorterButton enabledButton = new SorterButton("Enb", mEventsTree, 2);
    typeButton.setPreferredSize(new Dimension(WIDTH_OF_BUTTON_COLUMNS[0], EventJTree.rowHeight));
    nameButton.setPreferredSize(new Dimension(WIDTH_OF_BUTTON_COLUMNS[1], EventJTree.rowHeight));
    enabledButton.setPreferredSize(new Dimension(WIDTH_OF_BUTTON_COLUMNS[2], EventJTree.rowHeight));
    final JPanel buttonPanel = new JPanel();
    final GridBagLayout layout = new GridBagLayout();
    layout.columnWidths = WIDTH_OF_BUTTON_COLUMNS;
    layout.rowHeights = new int[]{EventJTree.rowHeight};
    buttonPanel.setLayout(layout);
    buttonPanel.add(typeButton);
    buttonPanel.add(nameButton);
    buttonPanel.add(enabledButton);
    mEventsPanel.add(buttonPanel, BorderLayout.NORTH);
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
  private EventJTree mEventsTree;
  //private final JScrollPane mScrollPane = new JScrollPane();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final boolean DISABLE_AUTOMATON_GRIDLINES = true;
  private static final int[] WIDTH_OF_BUTTON_COLUMNS = new int[]{65, 110, 60};

}