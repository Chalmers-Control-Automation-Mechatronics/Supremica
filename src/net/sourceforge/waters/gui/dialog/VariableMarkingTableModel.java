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

package net.sourceforge.waters.gui.dialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.VariableMarkingSubject;


/**
 * @author Robi Malik
 */

public class VariableMarkingTableModel
  extends ListTableModel<VariableMarkingSubject>
{

  //#########################################################################
  //# Constructors
  VariableMarkingTableModel(final ModuleContext context)
  {
    this(new ArrayList<VariableMarkingSubject>(), context);
  }

  VariableMarkingTableModel(final List<VariableMarkingSubject> list,
                            final ModuleContext context)
  {
    super(list, VariableMarkingSubject.class);
    mModuleContext = context;
  }


  //#########################################################################
  //# Interface javax.swing.TableModel
  public Class<?> getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return ImageIcon.class;
    case 1:
      return IdentifierSubject.class;
    case 2:
      return SimpleExpressionSubject.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for markings table model!");
    }
  }

  public int getColumnCount()
  {
    return 3;
  }

  public Object getValueAt(final int row, final int column)
  {
    final List<VariableMarkingSubject> list = getList();
    final VariableMarkingSubject marking = list.get(row);
    if (marking == null) {
      return null;
    } else {
      switch (column) {
      case 0:
        final IdentifierSubject prop = marking.getProposition();
        return mModuleContext.guessPropositionIcon(prop);
      case 1:
        return marking.getProposition();
      case 2:
        return marking.getPredicate();
      default:
        throw new ArrayIndexOutOfBoundsException
          ("Bad column number for markings table model!");
      }
    }
  }

  public boolean isCellEditable(final int row, final int column)
  {
    return column > 0;
  }

  public void setValueAt(final Object value,
                         final int row,
                         final int column)
  {
    if (value != null) {
      final List<VariableMarkingSubject> list = getList();
      final VariableMarkingSubject marking = list.get(row);
      switch (column) {
      case 1:
        final IdentifierSubject prop = (IdentifierSubject) value;
        if (marking == null) {
          final SimpleExpressionSubject expr1 =
            new IntConstantSubject(1);
          final VariableMarkingSubject newmarking =
            new VariableMarkingSubject(prop, expr1);
          list.set(row, newmarking);
        } else {
          marking.setProposition(prop);
        }
        break;
      case 2:
        final SimpleExpressionSubject pred = (SimpleExpressionSubject) value;
        marking.setPredicate(pred);
        break;
      default:
        throw new ArrayIndexOutOfBoundsException
          ("Bad column number for list table model!");
      }
      fireTableRowsUpdated(row, row);
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleContext mModuleContext;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
