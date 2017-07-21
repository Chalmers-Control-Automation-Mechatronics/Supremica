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

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * An auxiliary class to store information about replacements in the module
 * data structure.  In addition to the item replaced and its new value, it
 * holds a position object of unspecified type. Different panels will use
 * this to store different types of information. Among others, this is used
 * by rename operations to record the positions of renamed items in order
 * to facilitate undo.
 *
 * @see ListInsertPosition
 * @author Robi Malik
 */

public class ReplaceInfo
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new replace information record with <CODE>null</CODE>
   * position.
   * @param  old      The item to be replaced.
   * @param  neo      The item replacing the old value.
   */
  public ReplaceInfo(final Proxy old, final Proxy neo)
  {
    this(old, neo, null);
  }

  /**
   * Creates a new replace information record.
   * @param  old      The item to be replaced.
   * @param  neo      The item replacing the old value.
   * @param  inspos   An object that specfies where to replace the item.
   */
  public ReplaceInfo(final Proxy old, final Proxy neo, final Object inspos)
  {
    mOldProxy = old;
    mNewProxy = neo;
    mPosition = inspos;
  }



  //#########################################################################
  //# Simple Access
  /**
   * Gets the item to be replaced by this operation.
   */
  public Proxy getOldProxy()
  {
    return mOldProxy;
  }

  /**
   * Gets the item to replace the old value in this operation.
   */
  public Proxy getNewProxy()
  {
    return mNewProxy;
  }

  /**
   * Gets the position of the item replaced. The type of the insert
   * position is application-specific, as it depends on the operation how
   * excatly to specify an replacement. The default, <CODE>null</CODE> is
   * used if the position can already be determined from the item replaced,
   * e.g., when modifying a sorted list.
   */
  public Object getReplacePosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Advanced Access
  /**
   * Gets the item to be replaced by this operation.
   * @param  undoing  A flag indicating whether the operation is being undone.
   * @return The new value when undoing, otherwise the old value.
   */
  public Proxy getOldProxy(final boolean undoing)
  {
    return undoing ? getNewProxy() : getOldProxy();
  }

  /**
   * Gets the item to replace the old value in this operation.
   * @param  undoing  A flag indicating whether the operation is being undone.
   * @return The old value when undoing, otherwise the new value.
   */
  public Proxy getNewProxy(final boolean undoing)
  {
    return undoing ? getOldProxy() : getNewProxy();
  }


  //#########################################################################
  //# Static Methods
  /**
   * A convenience method to obtain the list of replaced items from a list
   * of <CODE>ReplaceInfo</CODE> objects.
   */
  public static List<Proxy> getOldProxies(final List<ReplaceInfo> replacements)
  {
    final int size = replacements.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final ReplaceInfo replacement : replacements) {
      final Proxy proxy = replacement.getOldProxy();
      result.add(proxy);
    }
    return result;
  }

  /**
   * A convenience method to obtain the list of items after replacement
   * from a list of <CODE>ReplaceInfo</CODE> objects.
   */
  public static List<Proxy> getNewProxies(final List<ReplaceInfo> replacements)
  {
    final int size = replacements.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final ReplaceInfo replacement : replacements) {
      final Proxy proxy = replacement.getNewProxy();
      result.add(proxy);
    }
    return result;
  }

  /**
   * A convenience method to obtain the list of replaced items from a list
   * of <CODE>ReplaceInfo</CODE> objects.
   * @param  undoing  A flag indicating whether the operation is being undone.
   * @return The list of old values when undoing,
   *         otherwise the list of new values.
   */
  public static List<Proxy> getOldProxies(final List<ReplaceInfo> replacements,
                                          final boolean undoing)
  {
    return undoing ? getNewProxies(replacements) : getOldProxies(replacements);
  }

  /**
   * A convenience method to obtain the list of items after replacement from
   * a list of <CODE>ReplaceInfo</CODE> objects.
   * @param  undoing  A flag indicating whether the operation is being undone.
   * @return The list of new values when undoing,
   *         otherwise the list of old values.
   */
  public static List<Proxy> getNewProxies(final List<ReplaceInfo> replacements,
                                          final boolean undoing)
  {
    return undoing ? getOldProxies(replacements) : getNewProxies(replacements);
  }


  //#########################################################################
  //# Data Members
  private final Proxy mOldProxy;
  private final Proxy mNewProxy;
  private final Object mPosition;

}
