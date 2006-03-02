//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   BooleanConstantProxy
//###########################################################################
//# $Id: BooleanConstantProxy.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * An expression representing an integer constant.
 *
 * @author Markus Sköldstam
 */


public interface BooleanConstantProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the boolean value of this constant.
   */
  public boolean isValue();

}
