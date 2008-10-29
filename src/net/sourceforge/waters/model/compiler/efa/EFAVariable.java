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
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * <P>A compiler-internal representation of an EFA variable.
 *
 * @author Robi Malik
 */

class EFAVariable implements Comparable<EFAVariable> {

  //#########################################################################
  //# Constructors
  EFAVariable(final IdentifierProxy ident, final CompiledRange range)
  {
    mIdentifier = ident;
    mRange = range;
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariable var)
  {
    return mIdentifier.compareTo(var.mIdentifier);
  }


  //#########################################################################
  //# Simple Access
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
  private final IdentifierProxy mIdentifier;
  private final CompiledRange mRange;

}
