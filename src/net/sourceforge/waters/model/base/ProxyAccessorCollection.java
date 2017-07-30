//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import java.util.Iterator;
import java.util.Map;

/**
 * An alternative collection interface that allows {@link Proxy} objects to
 * be treated using alternative equalities.
 * This interface supports actual collections, where more than a single
 * instance of a set of equal items may be contained, and where the order
 * is immaterial.  A proxy accessor map essentially is a map that maps
 * {@link ProxyAccessor} objects to the number of times they are contained
 * in the collection. This interface provides set-like access to this set.
 *
 * @see ProxyAccessorSet
 * @author Robi Malik
 */

public interface ProxyAccessorCollection<P extends Proxy>
  extends Map<ProxyAccessor<P>,Integer>
{

  //#########################################################################
  //# Access as Proxy Collection
  public boolean addProxy(P proxy);

  public boolean addAll(Collection<? extends P> collection);

  public boolean containsProxy(P proxy);

  public boolean containsAll(Collection<? extends P> collection);

  public <PP extends P> ProxyAccessor<PP> createAccessor(PP proxy);

  public int getCount(P proxy);

  public Iterator<P> iterator();

  public boolean removeProxy(P proxy);

  public boolean removeAll(Collection<? extends P> collection);

}
