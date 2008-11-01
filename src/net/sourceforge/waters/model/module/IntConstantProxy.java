//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   IntConstantProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * An expression representing an integer constant.
 *
 * @author Robi Malik
 */
// @short integer constant

public interface IntConstantProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the integer value of this constant.
   */
  public int getValue();

}
