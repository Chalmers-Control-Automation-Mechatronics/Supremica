//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAEventNameBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


/**
 * A simple algorithm to determine reasonably short name suffixes for
 * EFA events. EFA event names are suffixed by a string representing
 * the guard conditions for each event. This class transforms clauses
 * representing guard conditions into canonical names, by sorting the
 * literals, and by removing literals that are not needed to distinguish
 * an event name suffix from others.
 *
 * @see EFAEvent
 * @see EFAEventDecl
 * @author Robi Malik
 */

class EFAEventNameBuilder {

  //#########################################################################
  //# Constructor
  EFAEventNameBuilder(final ModuleProxyFactory factory,
                      final Comparator<SimpleExpressionProxy> comparator)
  {
    mComparator = comparator;
    mRoot = null;
  }


  //#########################################################################
  //# Invocation
  void restart()
  {
    mRoot = new NameTreeNode();
  }

  void addClause(final CompiledClause clause)
  {
    final List<SimpleExpressionProxy> list = getSortedList(clause);
    final Iterator<SimpleExpressionProxy> iter = list.iterator();
    mRoot.add(iter);
  }

  String getNameSuffix(final CompiledClause clause)
  {
    final List<SimpleExpressionProxy> list = getSortedList(clause);
    final Iterator<SimpleExpressionProxy> iter = list.iterator();
    final StringWriter writer = new StringWriter();
    final ModuleProxyPrinter printer = new ModuleProxyPrinter(writer);
    mRoot.print(printer, iter, true);
    final StringBuffer buffer = writer.getBuffer();
    if (buffer.length() > 0) {
      buffer.append('}');
    }
    return buffer.toString();
  }

  void clear()
  {
    mRoot = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<SimpleExpressionProxy> getSortedList
    (final CompiledClause clause)
  {
    final Collection<SimpleExpressionProxy> collection = clause.getLiterals();
    final List<SimpleExpressionProxy> list =
      new ArrayList<SimpleExpressionProxy>(collection);
    Collections.sort(list, mComparator);
    return list;
  }


  //#########################################################################
  //# Inner Class NameTreeNode
  private class NameTreeNode
  {

    //#######################################################################
    //# Constructor
    private NameTreeNode()
    {
      mChildren = new TreeMap<SimpleExpressionProxy,NameTreeNode>(mComparator);
      mIsEndOfClause = 0;
    }

    //#######################################################################
    //# Simple Access
    private int size()
    {
      return mChildren.size() + mIsEndOfClause;
    }

    //#######################################################################
    //# Algorithm
    private void add(final Iterator<SimpleExpressionProxy> iter)
    {
      if (iter.hasNext()) {
        final SimpleExpressionProxy literal = iter.next();
        NameTreeNode child = mChildren.get(literal);
        if (child == null) {
          child = new NameTreeNode();
          mChildren.put(literal, child);
        }
        child.add(iter);
      } else {
        mIsEndOfClause = 1;
      }
    }

    private void print(final ModuleProxyPrinter printer,
                       final Iterator<SimpleExpressionProxy> iter,
                       final boolean first)
    {
      if (iter.hasNext()) {
        final SimpleExpressionProxy literal = iter.next();
        final NameTreeNode child = mChildren.get(literal);
        final boolean nextfirst;
        if (size() == 1) {
          nextfirst = first;
        } else {
          try {
            printer.print(first ? '{' : ',');
            printer.printProxy(literal);
            nextfirst = false;
          } catch (final VisitorException exception) {
            throw exception.getRuntimeException();
          }
        }
        child.print(printer, iter, nextfirst);
      }
    }

    //#######################################################################
    //# Data Members
    private final Map<SimpleExpressionProxy,NameTreeNode> mChildren;
    private int mIsEndOfClause;

  }


  //#########################################################################
  //# Data Members
  private final Comparator<SimpleExpressionProxy> mComparator;

  private NameTreeNode mRoot;

}
