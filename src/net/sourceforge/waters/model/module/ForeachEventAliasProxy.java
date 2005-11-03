//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ForeachEventAliasProxy
//###########################################################################
//# $Id: ForeachEventAliasProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>A foreach construct for module event aliases.</P>
 *
 * <P>A foreach event alias construct that occurs in the <I>event alias
 * list</I> of a module. The entries in its body can be of the following
 * types.</P>
 *
 * <UL>
 * <LI>{@link AliasProxy}</LI>
 * <LI>{@link ForeachEventAliasProxy}</LI>
 * </UL>
 *
 * @see ModuleProxy#getEventAliasList()
 *
 * @author Robi Malik
 */
// @short foreach construct for aliases

public interface ForeachEventAliasProxy extends ForeachProxy {

}
