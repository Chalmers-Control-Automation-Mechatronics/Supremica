//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   VariableProxy
//###########################################################################
//# $Id: VariableProxy.java,v 1.5 2007-02-12 21:38:49 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A variable is used to define the guards
 *  and action on transitions.</P>
 *
 * @author Markus Sk&ouml;ldstam
 */

public interface VariableProxy extends Proxy{

  //#########################################################################
  //# Getters
  /**
   * Gets the Name of this variable.
   */
  public String getName(); 
  
  /**
   * Gets the type of this variable. Possible types:
   * EnumSetExpressionProxy (enum), BinaryExpressionProxy (integer) 
   * or SimpleIdentifierProxy (boolean).
   */
  public SimpleExpressionProxy getType(); 
  
  /**
   * Gets the initial value of this variable.
   */
  public SimpleExpressionProxy getInitialValue();
  
  /**
   * Gets the marked value of this variable.
   */
  // @optional
  public SimpleExpressionProxy getMarkedValue();
  
}
