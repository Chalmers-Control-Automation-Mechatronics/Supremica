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

package net.sourceforge.waters.gui.options;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.border.Border;

import net.sourceforge.waters.analysis.options.ColorOption;

/**
 *
 * @author Benjamin Wheeler
 */
class ColorOptionPanel
  extends OptionPanel<Color>
{
  //#########################################################################
  //# Constructors
  ColorOptionPanel(final GUIOptionContext context,
                     final ColorOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.OptionPanel
  @Override
  ColorChooser getEntryComponent()
  {
    return (ColorChooser) super.getEntryComponent();
  }

  @Override
  ColorChooser createEntryComponent()
  {
    final ColorOption option = getOption();
    final String title = option.getShortName();
    final ColorChooser colorChooser = new ColorChooser(title);
    final Color value = option.getValue();
    colorChooser.setColor(value);
    colorChooser.setRequestFocusEnabled(false);
    return colorChooser;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.options.OptionEditor
  @Override
  public ColorOption getOption()
  {
    return (ColorOption) super.getOption();
  }

  @Override
  public boolean commitValue()
  {
    final ColorChooser colorChooser = getEntryComponent();
    final Color value = colorChooser.getColor();
    final ColorOption option = getOption();
    option.setValue(value);
    return true;
  }


  //#########################################################################
  //# Inner Class ColorChooser
  /**
   * Chooser for colour properties.
   * Consists of a label describing the property and a button showing
   * the colour. When the button is clicked, a {@link JColorChooser}
   * dialog pops up.
   */
  private class ColorChooser
    extends JButton
    implements ActionListener
  {

    //#######################################################################
    //# Constructors
    private ColorChooser(final String title)
    {
      super("Click to change");
      mTitle = "Choose " + title;
      final Border bevel = BorderFactory.createLoweredBevelBorder();
      final Border empty = BorderFactory.createEmptyBorder(4, 6, 4, 6);
      final Border border = BorderFactory.createCompoundBorder(bevel, empty);
      setBorder(border);
      setFocusPainted(false);
      addActionListener(this);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      final Color color = getColor();
      final Color newcolor = JColorChooser.showDialog(this, mTitle, color);
      if (newcolor != null) {
        setColor(newcolor);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private Color getColor()
    {
      return getBackground();
    }

    private void setColor(final Color color)
    {
      setBackground(color);
      if (30 * color.getRed() + 59 * color.getGreen() +
        11 * color.getBlue() > 12750) {
        setForeground(Color.BLACK);
      } else {
        setForeground(Color.WHITE);
      }
    }

    //#######################################################################
    //# Data Members
    private final String mTitle;

    private static final long serialVersionUID = 5587390274318111775L;

  }




}
