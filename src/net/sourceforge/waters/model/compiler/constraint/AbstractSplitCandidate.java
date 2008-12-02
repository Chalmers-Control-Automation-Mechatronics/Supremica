//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   AbstractSplitCandidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


abstract class AbstractSplitCandidate
  implements SplitCandidate
{

  //#########################################################################
  //# Overrides for Base Class java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(getSplitExpression().toString());
    buffer.append(": occ=");
    buffer.append(getNumberOfOccurrences());
    buffer.append(", size=");
    buffer.append(getSplitSize());
    buffer.append(", occnext=");
    buffer.append(getOccursWithNext());
    buffer.append(", kind=");
    buffer.append(getKindValue());
    return buffer.toString();
  }

  //#########################################################################
  //# Simple Access
  abstract int getNumberOfOccurrences();

  abstract int getSplitSize();

  abstract boolean getOccursWithNext();

  abstract int getKindValue();

  abstract SimpleExpressionProxy getSplitExpression();


  //#########################################################################
  //# Class Constants
  static final int VARIABLE_SPLIT = 1;
  static final int DISJUNCTION_SPLIT = 2;

}