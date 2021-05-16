//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Benjamin Wheeler
 */
public abstract class LeafOptionPage extends OptionPage
{

  //#########################################################################
  //# Constructor
  public LeafOptionPage(final String prefix, final String title)
  {
    mPrefix = prefix;
    mTitle = title;
    mMap = new HashMap<>();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.OptionPage
  @Override
  public Option<?> get(final String id)
  {
    return mMap.get(id);
  }

  @Override
  public void register(final Option<?> param)
  {
    final String id = param.getID();
    mMap.put(id, param);
  }

  @Override
  public String getPrefix()
  {
    return mPrefix;
  }

  @Override
  public String getTitle()
  {
    return mTitle;
  }


  //#########################################################################
  //# Hooks
  public String getShortName()
  {
    return null;
  }

  /**
   * Gets a list of all registered options in this option page, in an order
   * suitable for writing to a properties file.
   */
  public abstract List<Option<?>> getOptions();


  //#########################################################################
  //# Access
  public void restoreDefaultValues()
  {
    for (final Option<?> option : mMap.values()) {
      option.restoreDefaultValue();
    }
  }

  public static List<Option<?>> createOptionList(final Option<?>...options)
  {
    final List<Option<?>> list = new ArrayList<>(options.length);
    for (final Option<?> option : options) {
      list.add(option);
    }
    return list;
  }


  //#########################################################################
  //# Data Members
  private final String mTitle;
  private final String mPrefix;
  private final Map<String, Option<?>> mMap;

}
