//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   EFSMVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * A representation of an EFSM variable for use in compositional
 * analysis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMVariable implements Comparable<EFSMVariable> {

  //#########################################################################
  //# Constructors
  EFSMVariable(final VariableComponentProxy var,
               final CompiledRange range)
  {
    mIsNext = false;
    mComponent = var;
    mRange = range;
    mVariableName = var.getIdentifier();
  }

  EFSMVariable(final boolean isnext,
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
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mVariableName.toString();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  @Override
  public int compareTo(final EFSMVariable var)
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

  boolean isPartnerOf(final EFSMVariable var)
  {
    return mIsNext != var.mIsNext && mComponent == var.mComponent;
  }

  CompiledRange getRange()
  {
    return mRange;
  }

  //#########################################################################
  //# Data Members
  private final boolean mIsNext;
  private final ComponentProxy mComponent;
  private final CompiledRange mRange;
  private final SimpleExpressionProxy mVariableName;

}
