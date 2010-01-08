package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.JTableHeader;

import net.sourceforge.waters.model.des.TraceProxy;

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

  public void switchToTraceMode(final TraceProxy trace)
  {
    mSimulation.run(trace);
    mTabbedPane.setSelectedIndex(2);
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
    mAutomataTable = new AutomataTable(mSimulation, mDesktop);
    final JScrollPane scroll = new JScrollPane(mAutomataTable);
    mAutomataPanel.setLayout(new BorderLayout());
    mAutomataPanel.add(scroll, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    mAutomataPanel.add(buttonPanel, BorderLayout.SOUTH);
    final WhiteScrollPane pane = new WhiteScrollPane(mAutomataTable);
    mTabbedPane.addTab("Automata", pane);
  }

  private void setupTrace()
  {
    mTraceTree = new TraceJTree(mSimulation, mDesktop, mModuleContainer);
    final JScrollPane scroll = new JScrollPane(mTraceTree);
    mTraceTree.addScrollPane(scroll);
    mTracePanel.setLayout(new BorderLayout());
    mTracePanel.add(scroll, BorderLayout.CENTER);
    mTabbedPane.addTab("Trace", mTracePanel);
  }

  private void setupEvents()
  {
    mEventsTree = new EventJTree(mSimulation, mDesktop, mModuleContainer);
    final JScrollPane scroll = new JScrollPane(mEventsTree);
    if (EVENT_VERTICAL_SCROLLBAR_ALWAYS)
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    mEventsTree.addPane(scroll);
    final GridBagLayout layout = new GridBagLayout();
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;
    mEventsPanel.setLayout(layout);
    final JTableHeader header = new EventJTreeHeader(mEventsPanel);
    final JTable fakeTable = new JTable();
    fakeTable.setTableHeader(header);
    header.addMouseListener(new TreePseudoTable(mEventsTree, header));
    //final JScrollPane topPane = new JScrollPane(psuedoTable);
    //mEventsPanel.add(topPane, BorderLayout.NORTH);
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weighty = 0;
    layout.setConstraints(header, constraints);
    mEventsPanel.add(header);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridy = 1;
    constraints.weighty = 1;
    layout.setConstraints(scroll, constraints);
    mEventsPanel.add(scroll);
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
  private JTable mAutomataTable;
  private TraceJTree mTraceTree;
  private EventJTree mEventsTree;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final boolean EVENT_VERTICAL_SCROLLBAR_ALWAYS = true;
}