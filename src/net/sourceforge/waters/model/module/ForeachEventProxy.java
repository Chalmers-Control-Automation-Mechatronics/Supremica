//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ForeachEventProxy
//###########################################################################
//# $Id: ForeachEventProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>A foreach construct for module events.</P>
 *
 * <P>A foreach event construct that occurs in an <I>event list</I> ({@link
 * EventListExpressionProxy}), which may occur on a graph's edge, in an alias
 * definition, or in the actual parameter of an instnce. The entries in its
 * body can be of the following types.</P>
 *
 * <UL>
 * <LI>{@link SimpleIdentifierProxy}</LI>
 * <LI>{@link IndexedIdentifierProxy}</LI>
 * <LI>{@link ForeachEventProxy}</LI>
 * </UL>
 *
 * @author Robi Malik
 */
// @short foreach construct for events

public interface ForeachEventProxy extends ForeachProxy {

}
