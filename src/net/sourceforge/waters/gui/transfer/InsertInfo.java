//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * An auxiliary class to store information about insertions in the module
 * data structure.  In addition to the item inserted, it holds an insert
 * position object of unspecified type. Different panels will use this to
 * store different types of information. Among others, this is used by
 * delete operations to record the positions of deleted items in order to
 * facilitate undo.
 *
 * @see ListInsertPosition
 * @author Robi Malik
 */

public class InsertInfo
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new insert information record with <CODE>null</CODE>
   * insert position.
   * @param  proxy    The item inserted.
   */
  public InsertInfo(final Proxy proxy)
  {
    this(proxy, null);
  }

  /**
   * Creates a new insert information record.
   * @param  proxy    The item inserted.
   * @param  inspos   An object that specifies how and where to insert the
   *                  item.
   */
  public InsertInfo(final Proxy proxy, final Object inspos)
  {
    mProxy = proxy;
    mPosition = inspos;
  }



  //#########################################################################
  //# Simple Access
  /**
   * Gets the item inserted by this operation.
   */
  public Proxy getProxy()
  {
    return mProxy;
  }

  /**
   * Gets the position for the item inserted. The type of the insert
   * position is application-specific, as it depends on the operation how
   * exactly to specify an insertion. The default, <CODE>null</CODE> is
   * used if the insert position can already be determined from the item
   * inserted, e.g., when inserting into a sorted list.
   */
  public Object getInsertPosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Static Methods
  /**
   * A convenience method to convert a list of <CODE>InsertInfo</CODE>
   * objects back into the list of their proxies.
   */
  public static List<Proxy> getProxies(final List<InsertInfo> inserts)
  {
    final int size = inserts.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      result.add(proxy);
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final Proxy mProxy;
  private final Object mPosition;

}
