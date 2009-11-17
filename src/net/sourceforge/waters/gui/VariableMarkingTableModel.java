//# -*-  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   VariableMarkingTableModel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

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
