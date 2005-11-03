//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   AliasProxy
//###########################################################################
//# $Id: AliasProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>An alias definition used to assign a simple name to a commonly used
 * expression.</P>
 *
 * <P>Aliases are used in to separate ways in modules.</P>
 * <UL>
 * <LI>To define named constants representing integers or ranges. Such
 *     aliases occur in the <I>constant alias list</I> of a module.</LI>
 * <LI>To give names to groups of events that are used in several different
 *     edges or graphs of a module. Such aliases occur in the <I>event alias
 *     list</I> of a module.</LI>
 * </UL>
 *
 * <P>Each alias has a name and a value bound to the name. The name can be
 * an arbitrary identifier. In particular, it can be indexed, as aliases
 * can also be defined iteratively in a <I>foreach</I> construct ({@link
 * ForeachEventAliasProxy}). The value is an arbitrary expression, its type
 * can vary depending on the particular kind of alias.</P>
 *
 * @see ModuleProxy
 *
 * @author Robi Malik
 */

public interface AliasProxy extends IdentifiedProxy {

  //#########################################################################
  //# Getters and Setters
  public ExpressionProxy getExpression();

}
