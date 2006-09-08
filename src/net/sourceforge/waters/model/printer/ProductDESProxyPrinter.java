//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.printer
//# CLASS:   ProductDESProxyPrinter
//###########################################################################
//# $Id: ProductDESProxyPrinter.java,v 1.5 2006-09-08 07:45:50 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.printer;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


public class ProductDESProxyPrinter
  extends ProxyPrinter
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ProductDESProxyPrinter()
  {
    super();
  }

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
  public Object visitConflictTraceProxy(final ConflictTraceProxy trace)
    throws VisitorException
  {
    print("CONFLICT TRACE ");
    print(trace.getName());
    final ConflictKind kind = trace.getKind();
    if (kind != ConflictKind.CONFLICT) {
      print(" (");
      print(kind.toString());
      print(')');
    }
    visitTraceProxy(trace);
    return null;
  }

  public Object visitProductDESProxy(final ProductDESProxy des)
    throws VisitorException
  {
    print("DES ");
    println(des.getName());
    printSortedCollection("EVENTS", des.getEvents());
    printSortedCollection("AUTOMATA", des.getAutomata());
    return null;
  }

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
    printSortedCollection("EVENTS", aut.getEvents());
    printSortedCollection("STATES", aut.getStates());
    printSortedCollection("TRANSITIONS", aut.getTransitions());
    indentOut();
    print('}');
    return null;
  }

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

  public Object visitLoopTraceProxy(final LoopTraceProxy trace)
    throws VisitorException
  {
    print("LOOP TRACE ");
    print(trace.getName());
    final int loop = trace.getLoopIndex();
    visitTraceProxy(trace, loop);
    return null;
  }

  public Object visitSafetyTraceProxy(final SafetyTraceProxy trace)
    throws VisitorException
  {
    print("SAFETY TRACE ");
    print(trace.getName());
    visitTraceProxy(trace);
    return null;
  }

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
      printSortedCollection(propositions);
    }
    return null;
  }

  public Object visitTraceProxy(final TraceProxy trace)
    throws VisitorException
  {
    visitTraceProxy(trace, -1);
    return null;
  }

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
  public Object visitTraceProxy(final TraceProxy trace, final int loop)
    throws VisitorException
  {
    println(" {");
    indentIn();
    final ProductDESProxy des = trace.getProductDES();
    final List<ProductDESProxy> list = Collections.singletonList(des);
    printSortedRefCollection("DES", list);
    printSortedRefCollection("AUTOMATA", trace.getAutomata());
    println("STEPS {");
    indentIn();
    int index = 0;
    for (final TraceStepProxy step : trace.getTraceSteps()) {
      if (index++ == loop) {
        println("Loop begins here:");
      }
      visitTraceStepProxy(step);
    }
    indentOut();
    println('}');
    indentOut();
    println('}');
    return null;
  }

}
