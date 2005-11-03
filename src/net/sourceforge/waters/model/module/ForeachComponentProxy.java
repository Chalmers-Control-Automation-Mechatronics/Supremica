//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ForeachComponentProxy
//###########################################################################
//# $Id: ForeachComponentProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>A foreach construct for module components.</P>
 *
 * <P>A foreach component construct that occurs in the <I>component list</I>
 * of a module. The entries in its body can be of the following types.</P>
 *
 * <UL>
 * <LI>{@link SimpleComponentProxy}</LI>
 * <LI>{@link InstanceProxy}</LI>
 * <LI>{@link ForeachComponentProxy}</LI>
 * </UL>
 *
 * @see ModuleProxy#getComponentList()
 *
 * @author Robi Malik
 */
// @short foreach construct for module components

public interface ForeachComponentProxy extends ForeachProxy {

}
