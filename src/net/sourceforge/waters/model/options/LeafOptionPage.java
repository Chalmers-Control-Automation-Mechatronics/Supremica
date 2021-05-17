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

package net.sourceforge.waters.model.options;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;


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
    mMap = new LinkedHashMap<>();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.OptionPage
  @Override
  public String getTitle()
  {
    return mTitle;
  }

  @Override
  public LeafOptionPage getLeafOptionPage(final String prefix)
  {
    if (mPrefix.equals(prefix)) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public void loadProperties(final Properties properties)
  {
    for (final Option<?> option : mMap.values()) {
      if (option.isPersistent()) {
        option.load(properties, mPrefix);
      }
    }
  }

  @Override
  public void saveProperties(final Writer writer, final boolean saveAll)
    throws IOException
  {
    for (final Option<?> option : mMap.values()) {
      if (option.isPersistent()) {
        option.save(writer, this, saveAll);
      }
    }
  }


  //#########################################################################
  //# Access
  public String getPrefix()
  {
    return mPrefix;
  }

  public Option<?> get(final String id)
  {
    return mMap.get(id);
  }

  public void register(final Option<?> param)
  {
    final String id = param.getID();
    mMap.put(id, param);
  }

  public Collection<Option<?>> getRegisteredOptions()
  {
    return mMap.values();
  }

  public String getShortDescription()
  {
    return null;
  }


  //#########################################################################
  //# Manipulating Option Lists
  public void restoreDefaultValues()
  {
    for (final Option<?> option : mMap.values()) {
      option.restoreDefaultValue();
    }
  }

  public void append(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    if (option != null) {
      list.add(option);
    }
  }

  public void prepend(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    if (option != null) {
      list.add(0, option);
    }
  }

  public void insertAfter(final List<Option<?>> list,
                          final String id,
                          final String afterThis)
  {
    final Option<?> option = get(id);
    if (option != null) {
      final ListIterator<Option<?>> iter = list.listIterator();
      while (iter.hasNext()) {
        final Option<?> next = iter.next();
        if (next.hasID(afterThis)) {
          iter.add(option);
          return;
        }
      }
    }
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
  private final String mTitle;
  private final String mPrefix;
  private final Map<String, Option<?>> mMap;

}
