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

package net.sourceforge.waters.analysis.options;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * <P>A collection of available options.</P>
 *
 * <P>An option map acts as a database that maps string identifiers to
 * {@link Option} objects. For persistent storage of options values,
 * the option map is first initialised with all available options,
 * typically at program start-up. The initialised option map is then
 * passed to {@link Configurable} objects, which select the options
 * stored in it.</P>
 *
 * @author Robi Malik
 */
public class OptionMap
{

  //#########################################################################
  //# Constructors
  public OptionMap()
  {
    mMap = new HashMap<>();
  }


  //#########################################################################
  //# Simple Access
  public Option<?> get(final String id)
  {
    return mMap.get(id);
  }

  public void add(final Option<?> param)
  {
    final String id = param.getID();
    mMap.put(id, param);
  }


  //#########################################################################
  //# Manipulating Option Lists
  public void append(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(option);
  }

  public void prepend(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(0, option);
  }

  public boolean remove(final List<Option<?>> list, final String id)
  {
    final Iterator<Option<?>> iter = list.iterator();
    while (iter.hasNext()) {
      final Option<?> option = iter.next();
      if (option.hasID(id)) {
        iter.remove();
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Data Members
  private final Map<String,Option<?>> mMap;

}
