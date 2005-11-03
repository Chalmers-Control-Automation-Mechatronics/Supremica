//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.printer
//# CLASS:   ProductDESProxyPrinter
//###########################################################################
//# $Id: ProductDESProxyPrinter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.printer;

import java.io.Writer;
import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


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

}