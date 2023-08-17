//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * The common superinterface for all cloning tools for the various proxy
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
