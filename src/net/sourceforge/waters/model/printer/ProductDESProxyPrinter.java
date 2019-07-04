//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.printer;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class ProductDESProxyPrinter
  extends ProxyPrinter
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ProductDESProxyPrinter(final Writer writer)
  {
    super(writer);
  }

  public ProductDESProxyPrinter(final Writer writer, final int indentwidth)
  {
    super(writer, indentwidth);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Object visitAutomatonProxy(final AutomatonProxy aut)
    throws VisitorException
  {
    final ComponentKind kind = aut.getKind();
    final String kindname = kind.toString();
    final String lowername = kindname.toLowerCase();
    print(lowername);
    print(' ');
    print(aut.getName());
    println(" {");
    indentIn();
    printCollection("EVENTS", aut.getEvents());
    printCollection("STATES", aut.getStates());
    printCollection("TRANSITIONS", aut.getTransitions());
    indentOut();
    print('}');
    return null;
  }

  @Override
  public Object visitConflictCounterExampleProxy
    (final ConflictCounterExampleProxy counter)
    throws VisitorException
  {
    print("CONFLICT COUNTEREXAMPLE ");
    print(counter.getName());
    final ConflictKind kind = counter.getKind();
    if (kind != ConflictKind.CONFLICT) {
      print(" (");
      print(kind.toString());
      print(')');
    }
    printCounterExampleHeader(counter);
    final TraceProxy trace = counter.getTrace();
    return printTraceProxy(trace, 0);
  }

  @Override
  public Object visitCounterExampleProxy(final CounterExampleProxy counter)
    throws VisitorException
  {
    print("COUNTEREXAMPLE ");
    print(counter.getName());
    return printCounterExample(counter);
  }

  @Override
  public Object visitDualCounterExampleProxy
    (final DualCounterExampleProxy counter)
    throws VisitorException
  {
    print("DUAL COUNTEREXAMPLE ");
    print(counter.getName());
    return printCounterExample(counter);
  }

  @Override
  public Object visitEventProxy(final EventProxy event)
    throws VisitorException
  {
    final EventKind kind = event.getKind();
    final String kindname = kind.toString();
    final String lowername = kindname.toLowerCase();
    print(lowername);
    print(' ');
    if (!event.isObservable()) {
      print("unobservable ");
    }
    print(event.getName());
    return null;
  }

  @Override
  public Object visitLoopCounterExampleProxy
    (final LoopCounterExampleProxy counter)
    throws VisitorException
  {
    print("LOOP COUNTEREXAMPLE ");
    print(counter.getName());
    printCounterExampleHeader(counter);
    final TraceProxy trace = counter.getTrace();
    return printTraceProxy(trace, 0);
  }

  @Override
  public Object visitProductDESProxy(final ProductDESProxy des)
    throws VisitorException
  {
    print("DES ");
    println(des.getName());
    printComment(des);
    printCollection("EVENTS", des.getEvents());
    printCollection("AUTOMATA", des.getAutomata());
    return null;
  }

  @Override
  public Object visitSafetyCounterExampleProxy
    (final SafetyCounterExampleProxy counter)
    throws VisitorException
  {
    print("SAFETY COUNTEREXAMPLE ");
    print(counter.getName());
    printCounterExampleHeader(counter);
    final TraceProxy trace = counter.getTrace();
    return printTraceProxy(trace, 0);
  }

  @Override
  public Object visitStateProxy(final StateProxy state)
    throws VisitorException
  {
    if (state.isInitial()) {
      print("initial ");
    }
    print(state.getName());
    final Collection<EventProxy> propositions = state.getPropositions();
    if (!propositions.isEmpty()) {
      print(' ');
      printCollection(propositions);
    }
    return null;
  }

  @Override
  public Object visitTraceProxy(final TraceProxy trace)
    throws VisitorException
  {
    printTraceProxy(trace, -1);
    return null;
  }

  @Override
  public Object visitTraceStepProxy(final TraceStepProxy step)
    throws VisitorException
  {
    final EventProxy event = step.getEvent();
    if (event != null) {
      print("- ");
      print(event.getName());
      println(" ->");
    }
    indentIn();
    final Map<AutomatonProxy,StateProxy> statemap = step.getStateMap();
    final Collection<AutomatonProxy> keyset = statemap.keySet();
    final List<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(keyset);
    Collections.sort(automata);
    for (final AutomatonProxy aut : automata) {
      print(aut.getName());
      print(": ");
      final StateProxy state = statemap.get(aut);
      if (state != null) {
        println(state.getName());
      } else {
        println("??");
      }
    }
    indentOut();
    return null;
  }

  @Override
  public Object visitTransitionProxy(final TransitionProxy trans)
    throws VisitorException
  {
    print(trans.getSource().getName());
    print(" -> ");
    print(trans.getTarget().getName());
    print(" {");
    print(trans.getEvent().getName());
    print('}');
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private Object printCounterExample(final CounterExampleProxy counter)
    throws VisitorException
  {
    printCounterExampleHeader(counter);
    int i = 1;
    for (final TraceProxy trace : counter.getTraces()) {
      printTraceProxy(trace, i++);
    }
    return null;
  }

  private void printCounterExampleHeader(final CounterExampleProxy counter)
    throws VisitorException
  {
    println();
    printComment(counter);
    final ProductDESProxy des = counter.getProductDES();
    final List<ProductDESProxy> list = Collections.singletonList(des);
    printRefCollection("DES", list);
    printRefCollection("AUTOMATA", counter.getAutomata());
  }

  private Object printTraceProxy(final TraceProxy trace,
                                 final int traceNo)
    throws VisitorException
  {
    print("TRACE");
    if (traceNo > 0) {
      print(" #");
      print(traceNo);
    }
    final String name = trace.getName();
    if (name != null && name.length() > 0) {
      print(": ");
      print(name);
    }
    println(" {");
    indentIn();
    final int loop = trace.getLoopIndex();
    int index = 0;
    for (final TraceStepProxy step : trace.getTraceSteps()) {
      visitTraceStepProxy(step);
      if (index++ == loop) {
        println("Loop begins here:");
      }
    }
    indentOut();
    println('}');
    return null;
  }

}
