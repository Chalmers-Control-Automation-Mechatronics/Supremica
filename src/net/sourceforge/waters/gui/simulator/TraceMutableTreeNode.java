//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import javax.swing.tree.DefaultMutableTreeNode;

public class TraceMutableTreeNode extends DefaultMutableTreeNode implements SimulationObserver
{
  // ################################################################
  // # Constructor
  public TraceMutableTreeNode(final Simulation sim, final TraceJTree parent)
  {
    super("Trace", true);
    sim.attach(this);
    mSim = sim;
    mParent = parent;
    setupAllEvents(sim);
  }

  // #################################################################
  // # Simple Access
  /*
  public String getData(final int indents)
  {
    String output = getIndents(indents) + this.toString() + "[" + (System.currentTimeMillis() - mStartTime) + "]";
    for (int childLoop = 0; childLoop < this.getChildCount(); childLoop++)
    {
      if (this.getChildAt(childLoop).getClass() == EventBranchNode.class)
      {
        final EventBranchNode node = (EventBranchNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
      else if (this.getChildAt(childLoop).getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode node = (AutomatonLeafNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
    }
    return output;
  }
  private String getIndents(final int indents)
  {
    String output = "";
    for (int looper = 0; looper < indents; looper++)
      output += "-";
    return output;
  }
  */


  //#########################################################################
  //# Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    setupAllEvents(event.getSource());
    mParent.forceRecalculation();
    mSim.detach(this);
  }

  //#########################################################################
  //# Auxiliary Methods
  private void setupAllEvents(final Simulation sim)
  {
    removeAllChildren();
    for (int time = 0; time < sim.getHistorySize(); time++) {
      final SimulatorState state = sim.getHistoryState(time);
      final TraceStepTreeNode node =
        TraceStepTreeNode.createTraceStepNode(state, time);
      add(node);
    }
  }


  //#########################################################################
  //# Data Members
  private final TraceJTree mParent;
  private final Simulation mSim;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4899696734198560636L;

}
