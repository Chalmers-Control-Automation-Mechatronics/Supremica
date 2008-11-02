//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A compiler-internal representation of an EFA variable.
 *
 * @author Robi Malik
 */

class EFAVariable implements Comparable<EFAVariable> {

  //#########################################################################
  //# Constructors
  EFAVariable(final ComponentProxy comp,
              final SimpleExpressionProxy varname,
              final CompiledRange range)
  {
    mComponent = comp;
    mVariableName = varname;
    mIsNext = varname instanceof UnaryExpressionProxy;
    if (mIsNext) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
      mIdentifier = (IdentifierProxy) unary.getSubTerm();
    } else {
      mIdentifier = (IdentifierProxy) varname;
    }
    mRange = range;
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariable var)
  {
    if (mIsNext != var.mIsNext) {
      return mIsNext ? 1 : -1;
    } else {
      return mIdentifier.compareTo(var.mIdentifier);
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
    return
      mIsNext != var.mIsNext && mIdentifier.equalsByContents(var.mIdentifier);
  }

  IdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }

  CompiledRange getRange()
  {
    return mRange;
  }


  //#########################################################################
  //# Data Members
  private final ComponentProxy mComponent;
  private final SimpleExpressionProxy mVariableName;
  private final IdentifierProxy mIdentifier;
  private final boolean mIsNext;
  private final CompiledRange mRange;

}
