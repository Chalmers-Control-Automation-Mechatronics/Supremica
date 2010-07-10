//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   ToolTipVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import java.util.Collection;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AbstractProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

class ToolTipVisitor extends AbstractProductDESProxyVisitor
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
  public String visitAutomatonProxy(final AutomatonProxy aut)
  {
    final StringBuffer buffer = new StringBuffer();
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

  public String visitEventProxy(final EventProxy event)
  {
    final StringBuffer buffer = new StringBuffer();
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

  public String visitStateProxy(final StateProxy state)
  {
    final StringBuffer buffer = new StringBuffer();
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
