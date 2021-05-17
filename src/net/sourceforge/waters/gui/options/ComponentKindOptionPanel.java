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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import net.sourceforge.waters.model.options.ComponentKindOption;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.options.OptionContext;


class ComponentKindOptionPanel
  extends OptionPanel<ComponentKind>
{
  //#########################################################################
  //# Constructors
  ComponentKindOptionPanel(final GUIOptionContext context,
                           final ComponentKindOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.Options.OptionPanel
  @Override
  JPanel getEntryComponent()
  {
    return (JPanel) super.getEntryComponent();
  }

  @Override
  JPanel createEntryComponent()
  {
    final JPanel panel = new JPanel();
    final GridBagLayout layout = new GridBagLayout();
    panel.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridy = 0;
    final ButtonGroup group = new ButtonGroup();
    final ComponentKind value = getPreselectedValue();
    final IconRadioButton<ComponentKind> plantButton =
      new IconRadioButton<>(ComponentKind.PLANT, "Plant",
                            IconAndFontLoader.ICON_PLANT, group);
    addButtom(panel, plantButton, constraints, value);
    final IconRadioButton<ComponentKind> propertyButton =
      new IconRadioButton<>(ComponentKind.PROPERTY, "Property",
                            IconAndFontLoader.ICON_PROPERTY, group);
    addButtom(panel, propertyButton, constraints, value);
    constraints.gridy++;
    final IconRadioButton<ComponentKind> specButton =
      new IconRadioButton<>(ComponentKind.SPEC, "Specification",
                            IconAndFontLoader.ICON_SPEC, group);
    addButtom(panel, specButton, constraints, value);
    final IconRadioButton<ComponentKind> supervisorButton =
      new IconRadioButton<>(ComponentKind.SUPERVISOR, "Supervisor",
                            IconAndFontLoader.ICON_SUPERVISOR, group);
    addButtom(panel, supervisorButton, constraints, value);
    return panel;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public ComponentKindOption getOption()
  {
    return (ComponentKindOption) super.getOption();
  }

  @Override
  public boolean commitValue()
  {
    final JPanel panel = getEntryComponent();
    for (final Component child : panel.getComponents()) {
      if (child instanceof IconRadioButton<?>) {
        @SuppressWarnings("unchecked")
        final IconRadioButton<ComponentKind> button =
          (IconRadioButton<ComponentKind>) child;
        if (button.isSelected()) {
          final ComponentKindOption option = getOption();
          final ComponentKind value = button.getValue();
          option.setValue(value);
          break;
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private ComponentKind getPreselectedValue()
  {
    final OptionContext context = getContext();
    final ProductDESProxy des = context.getProductDES();
    int plantCount = 0;
    int propCount = 0;
    int specCount = 0;
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (aut.getKind()) {
      case PLANT:
        plantCount++;
        break;
      case PROPERTY:
        propCount++;
        break;
      case SPEC:
        specCount++;
        break;
      default:
        break;
      }
    }
    if (plantCount > 0) {
      return ComponentKind.PLANT;
    } else if (propCount > 0) {
      return ComponentKind.PROPERTY;
    } else if (specCount > 0) {
      return ComponentKind.SPEC;
    } else {
      return ComponentKind.SUPERVISOR;
    }
  }

  private void addButtom(final JPanel panel,
                         final IconRadioButton<ComponentKind> button,
                         final GridBagConstraints constraints,
                         final ComponentKind value)
  {
    if (button.getValue() == value) {
      button.setSelected(true);
    }
    panel.add(button, constraints);
  }


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
