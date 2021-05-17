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

package net.sourceforge.waters.gui.options;

import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.gui.dialog.IntegerInputCell;


class PositiveIntOptionPanel
  extends OptionPanel<Integer>
{
  //#########################################################################
  //# Constructors
  PositiveIntOptionPanel(final GUIOptionContext context,
                         final PositiveIntOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Simple Access
  @Override
  IntegerInputCell getEntryComponent()
  {
    return (IntegerInputCell) super.getEntryComponent();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public PositiveIntOption getOption()
  {
    return (PositiveIntOption) super.getOption();
  }

  @Override
  public boolean commitValue()
  {
    final IntegerInputCell cell = getEntryComponent();
    if (cell.shouldYieldFocus()) {
      final PositiveIntOption option = getOption();
      final int value = cell.getValue();
      option.setValue(value);
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.OptionPanel
  @Override
  IntegerInputCell createEntryComponent()
  {
    final PositiveIntOption option = getOption();
    final int value = option.getValue();
    final int minValue = option.getMinValue();
    final int maxValue = option.getMaxValue();
    final IntegerInputCell cell = new IntegerInputCell(minValue, maxValue,
                                                       Integer.MAX_VALUE);
    cell.setValue(value);
    cell.setColumns(10);
    final GUIOptionContext context = getContext();
    cell.setErrorDisplay(context.getErrorDisplay());
    return cell;
  }

}
