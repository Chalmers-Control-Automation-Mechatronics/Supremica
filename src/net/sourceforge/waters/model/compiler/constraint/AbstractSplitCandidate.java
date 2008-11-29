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