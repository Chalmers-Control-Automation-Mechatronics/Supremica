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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.BooleanProperty;
import org.supremica.properties.SupremicaProperties;


/**
 * <P>An action to toggle a Boolean property.</P>
 *
 * <P>This a generic popup menu action, parameterised with a
 * {@link BooleanProperty} from Supremica's configuration. When triggered,
 * it toggles the value of the property, and saves the changes to the
 * properties file.</P>
 *
 * <P>This action is intended for popup menus only, it does not update its
 * enablement status or react to property changes.</P>
 *
 * @author Robi Malik
 */

public class ConfigBooleanPropertyAction
  extends WatersAction
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new Boolean property action.
   * @param ide       The IDE.
   * @param property  The property affected by the action.
   * @param shortName A short name to describe the action. The short name
   *                  is displayed when the action appears in the menu,
   *                  and the (longer) comment of the property is used as a
   *                  tool tip.
   */
  ConfigBooleanPropertyAction(final IDE ide,
                              final BooleanProperty property,
                              final String shortName)
  {
    super(ide);
    putValue(Action.NAME, shortName);
    putValue(Action.SHORT_DESCRIPTION, property.getComment());
    mProperty = property;
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    mProperty.toggle();
    SupremicaProperties.savePropertiesLater();
  }


  //#########################################################################
  //# Data Members
  private final BooleanProperty mProperty;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1145478200499065598L;

}
