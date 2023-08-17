//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.gui.util.IconAndFontLoader;

/**
 * An enumeration representing the different status values an event
 * may have in each simulation step.
 *
 * @author Robi Malik
 */

public enum EventStatus
{

  //#########################################################################
  //# Enumeration Values
  /**
   * Status to indicate that the event is enabled in the current step.
   */
  DISABLED(IconAndFontLoader.ICON_EVENTTREE_INVALID_EVENT),
  /**
   * Status to indicate that the event is disabled in the current step.
   */
  ENABLED(IconAndFontLoader.ICON_EVENTTREE_VALID_EVENT),
  /**
   * Status to indicate that the event is enabled in the model but disabled in
   * some property automaton. The language inclusion check fails in this step.
   */
  WARNING(IconAndFontLoader.ICON_EVENTTREE_CAUSES_WARNING_EVENT),
  /**
   * Status to indicate this is an uncontrollable event enabled in the plant but
   * disabled in some specification. The controllability check fails in this
   * step.
   */
  ERROR(IconAndFontLoader.ICON_EVENTTREE_BLOCKING_EVENT);


  //#########################################################################
  //# Constructor
  private EventStatus(final ImageIcon icon)
  {
    mIcon = icon;
  }


  //#########################################################################
  //# Simple Access
  ImageIcon getIcon()
  {
    return mIcon;
  }

  public boolean canBeFired()
  {
    return this == ENABLED || this == WARNING;
  }


  //#########################################################################
  //# Data Members
  private ImageIcon mIcon;

}
