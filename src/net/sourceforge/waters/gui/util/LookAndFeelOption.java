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


package net.sourceforge.waters.gui.util;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sourceforge.waters.model.base.ProxyTools;

/**
 * Enumeration of possible values for the Look&amp;Feel option.
 * The user can select between the default platform-independent look&amp;feel
 * (Metal) or the system look&amp;feel, which tries to mimic the operating
 * system. Please note the system look&amp;feel under Linux is GTK, which
 * displays tree selections poorly and is not recommended for use.
 *
 * @author Robi Malik
 */
public enum LookAndFeelOption
{
  //#########################################################################
  //# Enumeration Constants
  DEFAULT("Default") {
    @Override
    public String getLookAndFeelClassName()
    {
      return UIManager.getCrossPlatformLookAndFeelClassName();
    }
  },
  SYSTEM("System") {
    @Override
    public String getLookAndFeelClassName()
    {
      return UIManager.getSystemLookAndFeelClassName();
    }
  };


  //#########################################################################
  //# Constructor
  private LookAndFeelOption(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public abstract String getLookAndFeelClassName();


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Setting the Look & Feel
  public void setLookAndFeel()
  {
    try {
      final String className = getLookAndFeelClassName();
      UIManager.setLookAndFeel(className);
    } catch (final ClassNotFoundException |
                   InstantiationException |
                   IllegalAccessException |
                   UnsupportedLookAndFeelException exception) {
      System.err.println("Could not set requested look & feel (" + this +
                         " - " + ProxyTools.getShortClassName(exception) +
                         ") continuing with default.");
    }
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
