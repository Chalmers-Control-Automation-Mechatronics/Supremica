//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.ourceforge.waters.model.module
//# CLASS:   ConstantAliasProxy
//###########################################################################
//# $Id: ConstantAliasProxy.java,v 1.4 2007-06-08 10:57:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>An alias representing a constant definition.</P>
 *
 * <P>A constant alias is used to assign a simple constant to a name.  The
 * name of a constant alias must be a simple identifier ({@link
 * SimpleIdentifierProxy}).</P>
 *
 * <P>A constant alias may be a <I>parameter</I>, in which case the value
 * in the declaration only is a default that can be overridden by
 * instantiation from another module. A parameter can be <I>optional</i> or
 * <I>required</I>: for required parameters, a value must be provided when
 * instantiating. Nevertheless, even required parameters must have a
 * default value, which is used when compiling the module on its own.</P>
 *
 * @author Robi Malik
 */

public interface ConstantAliasProxy extends AliasProxy {

  /**
   * Gets the scope of this alias declaration.
   * The scope defines whether this alias actually is a parameter.
   * @return One of {@link ScopeKind#LOCAL},
   *         {@link ScopeKind#OPTIONAL_PARAMETER}, or
   *         {@link ScopeKind#REQUIRED_PARAMETER}.
   */
  // @default ScopeKind.LOCAL
  public ScopeKind getScope();

}
