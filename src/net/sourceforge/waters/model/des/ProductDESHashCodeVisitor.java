//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.des;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.AbstractHashCodeVisitor;
import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


public class ProductDESHashCodeVisitor
  extends AbstractHashCodeVisitor
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static ProductDESHashCodeVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private ProductDESHashCodeVisitor()
  {
  }

  private static class SingletonHolder {
    private static final ProductDESHashCodeVisitor INSTANCE =
      new ProductDESHashCodeVisitor();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  public Integer visitConflictTraceProxy(final ConflictTraceProxy trace)
      throws VisitorException
  {
    int result = visitTraceProxy(trace);
    final ConflictKind kind = trace.getKind();
    result *= 5;
    result += kind.hashCode();
    return result;
  }

  public Integer visitAutomatonProxy(final AutomatonProxy aut)
      throws VisitorException
  {
    int result = visitNamedProxy(aut);
    final ComponentKind kind = aut.getKind();
    result *= 5;
    result += kind.hashCode();
    final Set<EventProxy> events = aut.getEvents();
    result *= 5;
    result += computeRefCollectionHashCode(events);
    final Set<StateProxy> states = aut.getStates();
    result *= 5;
    result += computeCollectionHashCode(states);
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    result *= 5;
    result += computeCollectionHashCode(transitions);
    final Map<String,String> attribs = aut.getAttributes();
    result *= 5;
    result += attribs.hashCode();
    return result;
  }

  public Integer visitEventProxy(final EventProxy event)
      throws VisitorException
  {
    int result = visitNamedProxy(event);
    final EventKind kind = event.getKind();
    result *= 5;
    result += kind.hashCode();
    result *= 5;
    if (event.isObservable()) {
      result++;
    }
    final Map<String,String> attribs = event.getAttributes();
    result *= 5;
    result += attribs.hashCode();
    return result;
  }

  public Integer visitLoopTraceProxy(final LoopTraceProxy trace)
      throws VisitorException
  {
    int result = visitTraceProxy(trace);
    final int loop = trace.getLoopIndex();
    result *= 5;
    result += loop;
    return result;
  }

  public Integer visitProductDESProxy(final ProductDESProxy des)
      throws VisitorException
  {
    int result = visitDocumentProxy(des);
    final Set<EventProxy> events = des.getEvents();
    result *= 5;
    result += computeCollectionHashCode(events);
    final Set<AutomatonProxy> automata = des.getAutomata();
    result *= 5;
    result += computeCollectionHashCode(automata);
    return result;
  }

  public Integer visitSafetyTraceProxy(final SafetyTraceProxy trace)
      throws VisitorException
  {
    return visitTraceProxy(trace);
  }

  public Integer visitStateProxy(final StateProxy state)
      throws VisitorException
  {
    int result = visitNamedProxy(state);
    result *= 5;
    if (state.isInitial()) {
      result++;
    }
    final Collection<EventProxy> props = state.getPropositions();
    result *= 5;
    result += computeRefSetHashCode(props);
    return result;
  }

  public Integer visitTraceProxy(final TraceProxy trace)
      throws VisitorException
  {
    int result = visitDocumentProxy(trace);
    final ProductDESProxy des = trace.getProductDES();
    result *= 5;
    result += des.hashCode();
    final Set<AutomatonProxy> automata = trace.getAutomata();
    result *= 5;
    result += computeRefCollectionHashCode(automata);
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    result *= 5;
    result *= computeListHashCode(steps);
    return result;
  }

  public Integer visitTraceStepProxy(final TraceStepProxy step)
      throws VisitorException
  {
    int result = visitProxy(step);
    final EventProxy event = step.getEvent();
    result *= 5;
    result += computeOptionalHashCode(event);
    final Map<AutomatonProxy,StateProxy> statemap = step.getStateMap();
    result *= 5;
    result += computeRefMapHashCode(statemap);
    return result;
  }

  public Integer visitTransitionProxy(final TransitionProxy trans)
      throws VisitorException
  {
    int result = visitProxy(trans);
    final StateProxy source = trans.getSource();
    result *= 5;
    result += source.refHashCode();
    final EventProxy event = trans.getEvent();
    result *= 5;
    result += event.refHashCode();
    final StateProxy target = trans.getTarget();
    result *= 5;
    result += target.refHashCode();
    return result;
  }

}








