//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.util.IconLoader;

/**
 * An enumeration representing the different status values an automaton
 * may have in each simulation step.
 *
 * @author Robi Malik
 */

enum AutomatonStatus
{

  /**
   * Status to indicate a property automaton that has been disabled because
   * of a failure in an earlier step.
   */
  DISABLED(IconLoader.ICON_TABLE_DISABLED_PROPERTY, "has been disabled"),
  /**
   * Status to indicate that the state of an automaton is unchanged
   * from the previous step, with the current event not contained in
   * the automaton alphabet.
   */
  IGNORED(null, null),
  /**
   * Status to indicate that the state of an automaton is unchanged
   * from the previous step, with the current event being an explicit
   * selfloop on that state.
   */
  SELFLOOPED(IconLoader.ICON_TABLE_ENABLED_AUTOMATON,
             "contains a selfloop that has just been fired"),
  /**
   * Status to indicate that the state of the automaton is correctly
   * changed from the previous step.
   */
  OK(IconLoader.ICON_TABLE_ENABLED_AUTOMATON,
     "contains a transition that has just been fired"),
  /**
   * Status to indicate an invalid successor state in a property automaton.
   * The language inclusion check fails in this step.
   */
  WARNING(IconLoader.ICON_TABLE_WARNING_PROPERTY,
          "contains a language inclusion problem"),
  /**
   * Status to indicate an invalid successor state in a specification with
   * an uncontrollable event. The controllability check fails in this step.
   */
  ERROR(IconLoader.ICON_TABLE_ERROR_AUTOMATON,
        "contains a controllability problem");


  //#########################################################################
  //# Constructor
  private AutomatonStatus(final ImageIcon icon, final String text)
  {
    mIcon = icon;
    mText = text;
  }


  //#########################################################################
  //# Enumeration Values
  ImageIcon getIcon()
  {
    return mIcon;
  }

  String getText()
  {
    return mText;
  }


  //#########################################################################
  //# Data Members
  private ImageIcon mIcon;
  private String mText;

}








