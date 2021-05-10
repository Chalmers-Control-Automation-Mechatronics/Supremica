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

import java.util.Collection;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.DefaultProductDESProxyVisitor;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Robi Malik
 */

class ToolTipVisitor extends DefaultProductDESProxyVisitor
{

  //#########################################################################
  //# Constructor
  ToolTipVisitor(final Simulation sim)
  {
    mSimulation = sim;
  }


  //#########################################################################
  //# Invocation
  String getToolTip(final Proxy proxy, final boolean activity)
  {
    try {
      mShowActivity = activity;
      return (String) proxy.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  String getToolTip(final StateProxy state, final AutomatonProxy aut)
  {
    mAutomaton = aut;
    mShowActivity = true;
    final String result = visitStateProxy(state);
    mAutomaton = null;
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public String visitAutomatonProxy(final AutomatonProxy aut)
  {
    final StringBuilder buffer = new StringBuilder();
    final ComponentKind kind = aut.getKind();
    buffer.append(ModuleContext.getComponentKindToolTip(kind));
    buffer.append(' ');
    buffer.append(aut.getName());
    if (mShowActivity) {
      final AutomatonStatus status = mSimulation.getAutomatonStatus(aut);
      final String text = status.getText();
      if (text != null) {
        buffer.append(", ");
        buffer.append(text);
      }
    }
    return buffer.toString();
  }

  @Override
  public String visitEventProxy(final EventProxy event)
  {
    final StringBuilder buffer = new StringBuilder();
    final EventKind kind = event.getKind();
    buffer.append(ModuleContext.getEventKindToolTip(kind, false));
    buffer.append(' ');
    if (!event.isObservable()) {
      buffer.append("unobservable ");
    }
    buffer.append("event ");
    buffer.append(event.getName());
    if (mShowActivity) {
      final String text = mSimulation.getEventStatusText(event);
      if (text != null) {
        buffer.append(", ");
        buffer.append(text);
      }
    }
    return buffer.toString();
  }

  @Override
  public String visitStateProxy(final StateProxy state)
  {
    final StringBuilder buffer = new StringBuilder();
    if (state.isInitial()) {
      buffer.append("Initial state ");
    } else {
      buffer.append("State ");
    }
    buffer.append(state.getName());
    if (mShowActivity && mSimulation.getCurrentState(mAutomaton) == state) {
      buffer.append(", current state");
    }
    final Collection<EventProxy> props = state.getPropositions();
    if (!props.isEmpty()) {
      buffer.append(", marked as ");
      boolean first = true;
      for (final EventProxy prop : props) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        buffer.append(prop.getName());
      }
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final Simulation mSimulation;

  private AutomatonProxy mAutomaton;
  private boolean mShowActivity;

}
