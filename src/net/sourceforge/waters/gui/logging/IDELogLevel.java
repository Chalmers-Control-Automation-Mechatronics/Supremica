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

package net.sourceforge.waters.gui.logging;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;


/**
 * <P>Enumeration of logging verbosity levels supported by the IDE.</P>
 *
 * <P>This class is a wrapper around LOG4J2's {@link Level}, in order
 * to provide nicer names in the options dialog and pop-up menu.</P>
 *
 * @author Robi Malik
 */

public enum IDELogLevel
{

  //#########################################################################
  //# Enumeration Constants
  NONE(Level.OFF, "None"),
  FATAL(Level.FATAL, "Fatal"),
  ERROR(Level.ERROR, "Error"),
  WARN(Level.WARN, "Warning"),
  INFO(Level.INFO, "Info"),
  DEBUG(Level.DEBUG, "Debug"),
  TRACE(Level.TRACE, "Trace"),
  ALL(Level.ALL, "All");


  //#########################################################################
  //# Static Access
  public static IDELogLevel[] getAllowedValuesForLogPanel()
  {
    final IDELogLevel[] all = values();
    final List<IDELogLevel> allowed = new ArrayList<>(all.length);
    for (final IDELogLevel level : all) {
      if (level.isLessSpecificThan(Level.ERROR)) {
        allowed.add(level);
      }
    }
    final IDELogLevel[] result = new IDELogLevel[allowed.size()];
    return allowed.toArray(result);
  }


  //#########################################################################
  //# Constructor
  private IDELogLevel(final Level level, final String name)
  {
    mLevel = level;
    mName = name;
  }


  //#########################################################################
  //# Simple Access
  public Level getLevel()
  {
    return mLevel;
  }

  public String getName()
  {
    return mName;
  }

  public boolean isMoreSpecificThan(final Level level)
  {
    return mLevel.isMoreSpecificThan(level);
  }

  public boolean isLessSpecificThan(final Level level)
  {
    return mLevel.isLessSpecificThan(level);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Data Members
  private Level mLevel;
  private String mName;

}
