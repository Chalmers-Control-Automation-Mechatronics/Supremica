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

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A configurable parameter of a {@link ModelAnalyzer} of enumeration type.
 *
 * @author Brandon Bassett
 */
public class EnumParameter<T> extends Parameter
{

  //#########################################################################
  //# Constructors
  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final List<? extends T> data)
  {
    super(id, name, description);
    mList = data;
    mValue = mList.get(0);
  }

  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final T[] data)
  {
    this(id, name, description, Arrays.asList(data));
  }

  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final EnumFactory<? extends T> factory)
  {
    this(id, name, description, factory.getEnumConstants());
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Parameter
  @Override
  public Component createComponent()
  {
    final Vector<T> vector = new Vector<> (mList);
    final JComboBox<T> ret = new JComboBox<>(vector);
    ret.setSelectedItem(mValue);
    return ret;
  }

  public T getValue()
  {
    return  mValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboBox = (JComboBox<T>) comp;
  //  final int index = comboBox.getSelectedIndex();
  //  mValue = comboBox.getItemAt(index);
    mValue = (T) comboBox.getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboBox = (JComboBox<T>) comp;
    comboBox.setSelectedItem(mValue);
  }


  //#########################################################################
  //# Data Members
  private List<? extends T> mList;
  private T mValue;

}
