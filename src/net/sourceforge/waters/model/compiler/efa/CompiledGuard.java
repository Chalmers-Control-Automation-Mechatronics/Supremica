//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledGuard
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class CompiledGuard {

  //#########################################################################
  //# Constructor
  CompiledGuard()
  {
    mExpression = null;
    mDNF = CompiledNormalForm.getTrueDNF();
  }

  CompiledGuard(final SimpleExpressionProxy expr,
		final CompiledNormalForm dnf)
  {
    mExpression = expr;
    mDNF = dnf;
  }


  //#########################################################################
  //# Simple Access
  SimpleExpressionProxy getExpression()
  {
    return mExpression;
  }
  
  CompiledNormalForm getDNF()
  {
    return mDNF;
  }

  boolean isTrue()
  {
    return mDNF.isTrue();
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mExpression;
  private final CompiledNormalForm mDNF;

}