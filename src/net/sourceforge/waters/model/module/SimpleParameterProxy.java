//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   SimpleParameterProxy
//###########################################################################
//# $Id: SimpleParameterProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>A scalar type parameter for a Waters module.</P>
 *
 * <P>A simple parameter is a parameter that is bound to the value of an
 * expression when a module is compiled. There can be different types of
 * simple parameters, represented by the subclasses of this general base
 * class.</P>
 *
 * @author Robi Malik
 */

public interface SimpleParameterProxy extends ParameterProxy {

  public SimpleExpressionProxy getDefaultValue();

}
