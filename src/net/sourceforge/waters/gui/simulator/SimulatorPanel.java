package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

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
    mSimulation = new Simulation(moduleContainer);
    mModuleContainer = moduleContainer;
    setupDesktop();
    mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
    mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
    setupAutomata();
    setupEvents();
    setLeftComponent(mTabbedPane);
  }

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
    final JButton stepButton = new JButton("Step");
    stepButton.addActionListener(new TunnelActionListener(mAutomataTable , mModuleContainer));
    buttonPanel.add(stepButton, BorderLayout.WEST);
    final JButton undoButton = new JButton("Undo");
    undoButton.addActionListener(new UndoActionListener(mAutomataTable, mModuleContainer));
    buttonPanel.add(undoButton, BorderLayout.EAST);
    mAutomataPanel.add(buttonPanel, BorderLayout.SOUTH);
    mTabbedPane.addTab("Automata", mAutomataPanel);
  }

  private void setupAutomataTable()
  {
    mAutomataTable = new JTable(new AbstractTunnelTable(mModuleContainer, mSimulation));
    final int width = 245; // DEBUG: Arbitrary value: Any value will work, but this is close to the 'normal' value
    if (mAutomataTable.getColumnModel().getColumnCount() == 0)
    {
      System.out.println("DEBUG: ERROR: 0 columns in AutomatonTable " + mAutomataTable.toString());
    }
    else
      {
      mAutomataTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.35));
      mAutomataTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(3).setPreferredWidth((int)(width * 0.1));
      mAutomataTable.getColumnModel().getColumn(4).setPreferredWidth((int)(width * 0.35));
    }
    mAutomataTable.addMouseListener(new AutomatonMouseListener(mSimulation, mAutomataTable, mDesktop));
    final ListSelectionModel listMod =  mAutomataTable.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(mAutomataTable);
  }

  private void setupEvents()
  {
    setupEventsTable();
    final JScrollPane scroll = new JScrollPane(mEventsTable);
    mEventsPanel.setLayout(new BorderLayout());
    mEventsPanel.add(scroll, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    final JButton stepButton = new JButton("Step");
    stepButton.addActionListener(new TunnelActionListener(mAutomataTable , mModuleContainer));
    buttonPanel.add(stepButton, BorderLayout.WEST);
    final JButton undoButton = new JButton("Undo");
    undoButton.addActionListener(new UndoActionListener(mAutomataTable, mModuleContainer));
    buttonPanel.add(undoButton, BorderLayout.EAST);
    mEventsPanel.add(buttonPanel, BorderLayout.SOUTH);
    mTabbedPane.addTab("Events", mEventsPanel);
    final int width = 245; // DEBUG: Arbitrary value: Any value will work, but this is close to the 'normal' value
    mEventsTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.1));
    mEventsTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.8));
    mEventsTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
  }

  private void setupEventsTable()
  {
    mEventsTableModel = new EventTableModel(mModuleContainer, mSimulation);
    mEventsTable = new JTable(mEventsTableModel);
    final ListSelectionModel listMod =  mEventsTable.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(mEventsTable);
    mEventsTable.addMouseListener(new EventMouseListener(mSimulation, mEventsTable));
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private JTabbedPane mTabbedPane = new JTabbedPane();
  private AutomatonDesktopPane mDesktop;
  private final JPanel mAutomataPanel = new JPanel();
  private final JPanel mEventsPanel = new JPanel();
  private final Simulation mSimulation;
  //private final JPanel mTracePanel = new JPanel();
  private JTable mAutomataTable = new JTable();
  private JTable mEventsTable = new JTable();
  private EventTableModel  mEventsTableModel;
  //private final JScrollPane mScrollPane = new JScrollPane();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}