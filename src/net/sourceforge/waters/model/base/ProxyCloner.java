//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyCloner
//###########################################################################
//# $Id: ProxyCloner.java,v 1.2 2007-12-04 03:22:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * The common superinterface for all loning tools for the various proxy
 * implementations and categories. Parameterised by a factory, a proxy
 * cloner can accept objects from one {@link Proxy} implementation and
 * translate them to another.
 *
 * @author Robi Malik
 */

public interface ProxyCloner {

  /**
   * Clones a proxy object. This method creates a deep copy of the given
   * object, using the underlying factory, and ensuring consistency of
   * internal references.
   * @param  proxy   The object to be copied.
   */
  public Proxy getClone(Proxy proxy);

}
