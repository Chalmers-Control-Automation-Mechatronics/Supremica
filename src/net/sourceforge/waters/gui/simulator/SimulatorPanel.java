//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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

import net.sourceforge.waters.model.des.CounterExampleProxy;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.IDEMenuBar;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;


public class SimulatorPanel
  extends MainPanel
{

  //#########################################################################
  //# Constructor
  public SimulatorPanel(final ModuleContainer moduleContainer,
                        final String name)
  {
    super(name);
    mSimulation = new Simulation(moduleContainer);
    mModuleContainer = moduleContainer;
    setupDesktop();
    mTabbedPane = new JTabbedPane();
    setupAutomata();
    setupEvents();
    setupTrace();
    setLeftComponent(mTabbedPane);
  }


  //#########################################################################
  //# Menu Setup
  @Override
  public void createPanelSpecificMenus(final IDEMenuBar menuBar)
  {
    menuBar.createSimulateMenu();
    menuBar.createVerifyMenu();
  }


  //#########################################################################
  //# Simple Access
  public Simulation getSimulation()
  {
    return mSimulation;
  }

  public void switchToTraceMode(final CounterExampleProxy counterexample)
  {
    mSimulation.switchToTraceMode(counterexample);
    mTabbedPane.setSelectedIndex(2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setupDesktop()
  {
    mDesktop = new AutomatonDesktopPane(mModuleContainer, mSimulation);
    setRightComponent(mDesktop);
  }
  public AutomatonDesktopPane getDesktop()
  {
    return mDesktop;
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
    pane.setPreferredSize(mAutomataTable.getPreferredSize());
    mTabbedPane.addTab("Automata", pane);
  }

  private void setupTrace()
  {
    mTraceTree = new TraceJTree(mSimulation, mDesktop);
    final JScrollPane scroll = new JScrollPane(mTraceTree);
    mTraceTree.addScrollPane(scroll);
    mTracePanel.setLayout(new BorderLayout());
    mTracePanel.add(scroll, BorderLayout.CENTER);
    mTabbedPane.addTab("Trace", mTracePanel);
  }

  private void setupEvents()
  {
    mEventsTree = new EventJTree(mSimulation, mDesktop);
    final JScrollPane scroll = new JScrollPane(mEventsTree);
    if (EVENT_VERTICAL_SCROLLBAR_ALWAYS)
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    if (EVENT_HORIZONTAL_SCROLLBAR_NEVER)
      scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
  private static final boolean EVENT_VERTICAL_SCROLLBAR_ALWAYS = false;
  private static final boolean EVENT_HORIZONTAL_SCROLLBAR_NEVER = true;
}
