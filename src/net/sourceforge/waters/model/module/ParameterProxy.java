//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ParameterProxy
//###########################################################################
//# $Id: ParameterProxy.java,v 1.3 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A parameter for a Waters module.</P>
 *
 * <P>Modules can have parameters that are substituted by actual values
 * when the module is compiled. This can be used to describe interface
 * events and bind them to different events each time a module is used, or
 * to parameterise the number of components in a module.</P>
 *
 * <P>There can be different types of parameters, represented by
 * the subclasses of this general base class.</P>
 *
 * @see ModuleProxy
 * @see InstanceProxy
 *
 * @author Robi Malik
 */

public interface ParameterProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the required status of this parameter.
   * A required parameter must be given a value when its module is
   * instantiated, otherwise an error will be produced.
   * @return <CODE>true</CODE> if this parameter is required.
   */
  // @default true
  public boolean isRequired();

}
