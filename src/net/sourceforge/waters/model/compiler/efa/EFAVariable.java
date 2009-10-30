//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A compiler-internal representation of an EFA variable.
 *
 * @author Robi Malik
 */

class EFAVariable implements Comparable<EFAVariable> {

  //#########################################################################
  //# Constructors
  EFAVariable(final boolean isnext,
              final ComponentProxy comp,
              final CompiledRange range,
              final ModuleProxyFactory factory,
              final CompilerOperatorTable optable)
  {
    mIsNext = isnext;
    mComponent = comp;
    mRange = range;
    final IdentifierProxy ident = mComponent.getIdentifier();
    if (mIsNext) {
      final UnaryOperator nextop = optable.getNextOperator();
      mVariableName = factory.createUnaryExpressionProxy(nextop, ident);
    } else {
      mVariableName = ident;
    }
    if (mIsNext) {
      mEventList = null;
      mEventSet = null;
    } else {
      mEventList = new LinkedList<EFAEvent>();
      mEventSet = new HashSet<EFAEvent>();
    }
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariable var)
  {
    if (mIsNext != var.mIsNext) {
      return mIsNext ? 1 : -1;
    } else {
      return mComponent.compareTo(var.mComponent);
    }
  }


  //#########################################################################
  //# Simple Access
  ComponentProxy getComponent()
  {
    return mComponent;
  }

  SimpleExpressionProxy getVariableName()
  {
    return mVariableName;
  }

  boolean isNext()
  {
    return mIsNext;
  }

  boolean isPartnerOf(final EFAVariable var)
  {
    return mIsNext != var.mIsNext && mComponent == var.mComponent;
  }

  CompiledRange getRange()
  {
    return mRange;
  }

  List<EFAEvent> getEFAEvents()
  {
    return mEventList;
  }

  void addEvent(final EFAEvent event)
  {
    if (mEventSet.add(event)) {
      mEventList.add(event);
    }
  }
    

  //#########################################################################
  //# Data Members
  private final boolean mIsNext;
  private final ComponentProxy mComponent;
  private final CompiledRange mRange;
  private final SimpleExpressionProxy mVariableName;
  private final List<EFAEvent> mEventList;
  private final Set<EFAEvent> mEventSet;

}
