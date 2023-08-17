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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.options.EnumOption;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to change an enumeration-type property.</P>
 *
 * <P>This a generic popup menu action, parameterised with an
 * {@link EnumOption} from Supremica's configuration and one value. When
 * triggered, it sets the property to the specified value, and triggers
 * saving of the configuration file if the property was changed.</P>
 *
 * <P>This action is intended for popup menus only, it does not update its
 * enablement status or react to property changes.</P>
 *
 * @author Robi Malik
 */

public class ConfigEnumPropertyAction<E extends Enum<E>>
  extends WatersAction
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new enumeration property action.
   * @param ide       The IDE.
   * @param option    The property affected by the action.
   * @param value     The value assigned to the property when the action
   *                  is triggered.
   * @param comment   A comment to explain the action. Menu items are
   *                  labelled by the string representation of the value,
   *                  while the comment is used as a tool tip.
   */
  ConfigEnumPropertyAction(final IDE ide,
                           final EnumOption<E> option,
                           final E value,
                           final String comment)
  {
    super(ide);
    putValue(Action.NAME, value.toString());
    putValue(Action.SHORT_DESCRIPTION, comment);
    mOption = option;
    mValue = value;
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    mOption.setValue(mValue);
  }


  //#########################################################################
  //# Data Members
  private final EnumOption<E> mOption;
  private final E mValue;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5473433986208336416L;

}
