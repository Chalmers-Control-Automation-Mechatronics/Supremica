//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;


class TraceStepTreeNode extends DefaultMutableTreeNode
{

  //#########################################################################
  //# Factory Methods
  static TraceStepTreeNode createTraceStepNode(final SimulatorState state,
                                               final int time)
  {
    final EventProxy event = state.getEvent();
    if (event != null) {
      return createEventStepNode(event, time);
    } else if (time == 0) {
      return createInitialStepNode();
    } else {
      return createTeleportStepNode(time);
    }
  }

  static TraceStepTreeNode createInitialStepNode()
  {
    return new TraceStepTreeNode("Initial state", null, 0);
  }

  static TraceStepTreeNode createEventStepNode(final EventProxy event,
                                               final int time)
  {
    return new TraceStepTreeNode(event.getName(), event, time);
  }

  static TraceStepTreeNode createTeleportStepNode(final int time)
  {
    return new TraceStepTreeNode("State set manually", null, time);
  }


  //#########################################################################
  //# Constructor
  private TraceStepTreeNode(final String description,
                            final EventProxy event,
                            final int time)
  {
    super(description, true);
    mEvent = event;
    mTime = time;
    // To ensure that the events list can be expanded. This is removed as soon
    // as the node is expanded. Still, it appears as a grey box on the tree.
    add(new DefaultMutableTreeNode("You shouldn't ever see this", false));
  }


  //#########################################################################
  //# Simple Access
  String getText()
  {
    return (String) getUserObject();
  }

  EventProxy getEvent()
  {
    return mEvent;
  }

  int getTime()
  {
    return mTime;
  }

  Icon getIcon()
  {
    if (mEvent != null) {
      final EventKind kind = mEvent.getKind();
      final boolean observable = mEvent.isObservable();
      return ModuleContext.getEventKindIcon(kind, observable);
    } else {
      return PropositionIcon.getUnmarkedIcon();
    }
  }


  //#########################################################################
  //# Tree Access
  void expand(final Simulation sim)
  {
    if (getChildAt(0).getClass() != AutomatonLeafNode.class) {
      removeAllChildren();
      final SimulatorState tuple = sim.getHistoryState(mTime);
      for (final AutomatonProxy aut : sim.getOrderedAutomata()) {
        final AutomatonStatus status = tuple.getStatus(aut);
        if (mTime == 0 || status.compareTo(AutomatonStatus.OK) >= 0) {
          final StateProxy state = tuple.getState(aut);
          final AutomatonLeafNode node =
            new AutomatonLeafNode(aut, state, mTime);
          add(node);
        }
      }
    }
  }

  String getToolTipText(final Simulation sim)
  {
    if (mEvent != null) {
      final ToolTipVisitor visitor = sim.getToolTipVisitor();
      return visitor.getToolTip(mEvent, false);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final int mTime;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1581075011997555080L;

}
