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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.OptionEditor;


public abstract class OptionPanel<T> implements OptionEditor<T>
{
  //#########################################################################
  //# Constructors
  OptionPanel(final GUIOptionContext context,
              final Option<T> option)
  {
    mContext = context;
    mOption = option;
    mLabel = createLabel();
    setToolTip(mLabel);
    mEntryComponent = createEntryComponent();
    setToolTip(mEntryComponent);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.OptionEditor
  @Override
  public Option<T> getOption()
  {
    return mOption;
  }


  //#########################################################################
  //# Simple Access
  GUIOptionContext getContext()
  {
    return mContext;
  }

  JLabel getLabel()
  {
    return mLabel;
  }

  Component getEntryComponent()
  {
    return mEntryComponent;
  }

  abstract void commitValue();

  /**
   * Checks whether this option panel matches the given search query and
   * updates the query accordingly.
   */
  void search(final SearchQuery query)
  {
    if (query.matches(getLabel().getText())) {
      query.addResult(this);
    }
  }

  /**
   * Tries to display the given option by calling {@link
   * JComponent#scrollRectToVisible(java.awt.Rectangle) scrollRectToVisible()}
   * on the parent component.
   * @param  panel   Option panel representing the option to be displayed.
   * @return <CODE>true</CODE> if the given option is equal to this option
   *         (or contained within its children) and was displayed,
   *         <CODE>false</CODE> otherwise.
   */
  boolean scrollToVisible(final OptionPanel<?> panel)
  {
    if (this == panel) {
      final JLabel label = getLabel();
      final Rectangle bounds = label.getBounds();
      bounds.x -= 2;
      bounds.y-= 2;
      bounds.width += 4;
      bounds.height += 4;
      label.scrollRectToVisible(bounds);
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# GUI
  JLabel createLabel()
  {
    final String text = mOption.getShortName();
    if (text == null) {
      return new JLabel();
    } else {
      return new JLabel(text);
    }
  }

  abstract Component createEntryComponent();

  void addComponentsToPanel(final JPanel panel,
                            final GridBagConstraints constraints)
  {
    constraints.gridwidth = 1;
    final JLabel label = getLabel();
    constraints.gridx = 0;
    panel.add(label, constraints);
    final Component entry = getEntryComponent();
    constraints.gridx = 1;
    panel.add(entry, constraints);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Recursively sets tool tip for component.
   * @param  comp  Components to have its tool tip set.
   */
  void setToolTip(final Component comp)
  {
    if (comp instanceof JPanel) {
      final JPanel panel = (JPanel) comp;
      for (final Component child : panel.getComponents()) {
        setToolTip(child);
      }
    } else if (comp instanceof JComponent) {
      final JComponent jComp = (JComponent) comp;
      final String text = mOption.getDescription();
      jComp.setToolTipText(text);
    }
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final Option<T> mOption;
  private final JLabel mLabel;
  private final Component mEntryComponent;

}
