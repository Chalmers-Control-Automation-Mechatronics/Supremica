//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyCloner
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;


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
   * @param  proxy       The object to be copied.
   */
  public Proxy getClone(Proxy proxy);

  /**
   * Clones a collection of proxy objects. This method creates a deep copy
   * of all objects in the given collection, using the underlying factory,
   * and ensuring consistency of internal references.
   * @param  collection  The objects to be copied.
   * @return The list of clones, in the same order as their originals
   *         are encountered in the input collection.
   */
  public <P extends Proxy>
  List<P> getClonedList(Collection<? extends P> collection);

  /**
   * Clones a collection of proxy objects. This method creates a deep copy
   * of all objects in the given collection, using the underlying factory,
   * and ensuring consistency of internal references.
   * @param  collection  The objects to be copied.
   * @return A set containing the clones.
   */
  public <P extends Proxy>
  Set<P> getClonedSet(Collection<? extends P> collection);

}
