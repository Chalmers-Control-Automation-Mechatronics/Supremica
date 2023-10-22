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

package net.sourceforge.waters.gui.options;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.options.EnumListOption;
import net.sourceforge.waters.model.options.EnumOption;


/**
 * <P>An option editor for lists of enumerated items.</P>
 *
 * <P>This preliminary implementation only supports singleton lists.
 * It displays a combo box like {@link EnumOption} and sets the option value
 * by creating a singleton list. If the option value is a list with more than
 * one element, only the first element gets displayed, and changing it results
 * in the remaining elements being deleted.</P>
 *
 * @author Robi Malik
 */

class EnumListOptionPanel<T>
  extends OptionPanel<List<T>>
{
  //#########################################################################
  //# Constructors
  EnumListOptionPanel(final GUIOptionContext context,
                      final EnumListOption<T> option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.Options.OptionPanel
  @SuppressWarnings("unchecked")
  @Override
  JComboBox<T> getEntryComponent()
  {
    return (JComboBox<T>) super.getEntryComponent();
  }

  @Override
  JComboBox<T> createEntryComponent()
  {
    final EnumListOption<T> option = getOption();
    final Vector<T> vector = new Vector<>(option.getEnumConstants());
    final JComboBox<T> comboBox = new JComboBox<>(vector);
    final List<T> list = option.getValue();
    final T value = list.get(0);
    comboBox.setSelectedItem(value);
    return comboBox;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public EnumListOption<T> getOption()
  {
    return (EnumListOption<T>) super.getOption();
  }

  @Override
  public void commitValue()
  {
    final JComboBox<T> comboBox = getEntryComponent();
    final int index = comboBox.getSelectedIndex();
    final T value = comboBox.getItemAt(index);
    final EnumListOption<T> option = getOption();
    final List<T> list = Collections.singletonList(value);
    option.setValue(list);
  }

}
