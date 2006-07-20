//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   BooleanConstantProxy
//###########################################################################
//# $Id: BooleanConstantProxy.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * An expression representing an integer constant.
 *
 * @author Markus Sk&ouml;ldstam
 */


public interface BooleanConstantProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the boolean value of this constant.
   */
  public boolean isValue();

}
