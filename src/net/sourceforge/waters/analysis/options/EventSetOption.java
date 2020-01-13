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

import java.util.Set;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * A configurable parameter to pass a set of events ({@link EventProxy})
 * to a {@link ModelAnalyzer}.
 *
 * @author Brandon Bassett
 */

public class EventSetOption extends Option<Set<EventProxy>>
{
  //#########################################################################
  //# Constructors
  public EventSetOption(final String id,
                        final String shortName,
                        final String description,
                        final String commandLineOption,
                        final DefaultKind defaultKind,
                        final String selectedTitle,
                        final String unselectedTitle)
  {
    super(id, shortName, description, commandLineOption, null);
    mDefaultKind = defaultKind;
    mSelectedTitle = selectedTitle;
    mUnselectedTitle = unselectedTitle;
  }


  //#########################################################################
  //# Simple Access
  public DefaultKind getDefaultKind()
  {
    return mDefaultKind;
  }

  public String getSelectedTitle() {
    return mSelectedTitle;
  }

  public String getUnselectedTitle() {
    return mUnselectedTitle;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<Set<EventProxy>> createEditor(final OptionContext context)
  {
    return context.createEventSetEditor(this);
  }

  @Override
  public boolean isPersistent()
  {
    return false;
  }

  @Override
  public void set(final String text)
  {
    //Do nothing
  }

  //#########################################################################
  //# Inner Enumeration EventKind
  public enum DefaultKind {

    CONTROLLABLE {
      @Override
      public boolean isDefault(final EventKind ek)
      {
        if (ek == EventKind.CONTROLLABLE) {
          return true;
        }
        else return false;
      }
      @Override
      public boolean isChoosable(final EventKind ek)
      {
        return ek != EventKind.PROPOSITION;
      }
    },

    UNCONTROLLABLE {
      @Override
      public boolean isDefault(final EventKind ek)
      {
        if (ek == EventKind.UNCONTROLLABLE) {
          return true;
        }
        else return false;
      }
      @Override
      public boolean isChoosable(final EventKind ek)
      {
        return ek != EventKind.PROPOSITION;
      }
    },

    PROPER_EVENT {
      @Override
      public boolean isDefault(final EventKind ek)
      {
        if (ek == EventKind.CONTROLLABLE ||
          ek == EventKind.UNCONTROLLABLE) {
          return true;
        }
        else return false;
      }
      @Override
      public boolean isChoosable(final EventKind ek)
      {
        return ek != EventKind.PROPOSITION;
      }
    },

    PROPOSITION {
      @Override
      public boolean isDefault(final EventKind ek)
      {
        if (ek == EventKind.PROPOSITION) {
          return true;
        }
        else return false;
      }
      @Override
      public boolean isChoosable(final EventKind ek)
      {
        return ek == EventKind.PROPOSITION;
      }
    };

    public abstract boolean isDefault(EventKind ek);

    public abstract boolean isChoosable(EventKind ek);

  }


  //#########################################################################
  //# Data Members
  private final DefaultKind mDefaultKind;
  private final String mSelectedTitle;
  private final String mUnselectedTitle;

}
