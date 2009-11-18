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
    mAutomataPanel.add(stepButton, BorderLayout.SOUTH);
    mTabbedPane.addTab("Automata", mAutomataPanel);
  }

  private void setupAutomataTable()
  {
    mAutomataTable = new JTable(new AbstractTunnelTable(mModuleContainer));
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private JTabbedPane mTabbedPane = new JTabbedPane();
  private final JDesktopPane mDesktop = new JDesktopPane();
  private final JPanel mAutomataPanel = new JPanel();
  //private final JPanel mEventsPanel = new JPanel();
  //private final JPanel mTracePanel = new JPanel();
  private JTable mAutomataTable = new JTable();
  //private final JTable mEventsTable = new JTable();
  //private final JScrollPane mScrollPane = new JScrollPane();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}