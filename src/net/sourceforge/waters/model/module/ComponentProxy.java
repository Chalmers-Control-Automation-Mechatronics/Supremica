//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ComponentProxy
//###########################################################################
//# $Id: ComponentProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * The common interface for all components in a Waters module.
 *
 * This interface represents the proper items that may occur in a module's
 * component list. Presently, these are <I>simple components</I> ({@link
 * SimpleComponentProxy}) and <I>instances</I> ({@link InstanceProxy}).
 *
 * @see SimpleComponentProxy
 * @see InstanceProxy
 *
 * @author Robi Malik
 */

public interface ComponentProxy extends IdentifiedProxy {

}
