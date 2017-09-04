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

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * The initial greeting displayed when opening the IDE, or when no
 * module is currently open. This is panel that contains the version
 * information ({@link AboutPanel}) and some logo images.
 *
 * @author Robi Malik
 */
public class WelcomeScreen
  extends JPanel
  implements ComponentListener, SupremicaPropertyChangeListener
{

  //#########################################################################
  //# Constructor
  public WelcomeScreen(final IDE ide)
  {
    mIDE = ide;
    createContents();
    setBackground(new Color(240, 255, 255));
    addComponentListener(this);
    Config.DOT_USE.addPropertyChangeListener(this);
    Config.DOT_EXECUTE_COMMAND.addPropertyChangeListener(this);
  }


  //#########################################################################
  //# Simple Access
  public String getWindowTitle()
  {
    return Version.getInstance().getTitle();
  }


  //#########################################################################
  //# Painting
  @Override
  public void paintComponent(final Graphics graphics)
  {
    if (BACKGROUND != null) {
      final int width = getWidth();
      final int height = getHeight();
      final int tileWidth = BACKGROUND.getIconWidth();
      final int tileHeight = BACKGROUND.getIconHeight();
      final Image image = BACKGROUND.getImage();
      for (int x = 0; x < width; x+= tileWidth) {
        for (int y = 0; y < height; y += tileHeight) {
          graphics.drawImage(image, x, y, null);
        }
      }
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ComponentListener;
  @Override
  public void componentResized(final ComponentEvent event)
  {
    removeAll();
    createContents();
    revalidate();
  }

  @Override
  public void componentMoved(final ComponentEvent event)
  {
    // nothing
  }


  @Override
  public void componentShown(final ComponentEvent event)
  {
    // nothing
  }


  @Override
  public void componentHidden(final ComponentEvent event)
  {
    // nothing
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
  {
    removeAll();
    createContents();
    revalidate();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createContents()
  {
    // Create the about box with the version information
    mAboutPanel = new AboutPanel(mIDE);
    final Border border = new AboutBoxBorder();
    mAboutPanel.setBorder(border);
    final int scaledWidth =
      Math.round(TEXT_WIDTH * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
    mAboutPanel.setSize(scaledWidth, Integer.MAX_VALUE);
    final int textHeight = mAboutPanel.getPreferredSize().height;
    final Dimension size = new Dimension(scaledWidth, textHeight);
    mAboutPanel.setPreferredSize(size);

    // Is there enough space for the images also
    final int fullHeight =
      VERTICAL_SPACE + LOGO_SUPREMICA.getIconHeight() +
      Math.max(LOGO_SUPREMICA.getIconHeight(), textHeight);
    final int fullWidth = LOGO_WATERS.getIconWidth() + scaledWidth;
    final boolean showingLogos =
      fullHeight <= mIDE.getHeight() * 3 / 4 &&
      fullWidth <= mIDE.getWidth() * 7 / 8;

    // Put the information on the screen
    final LayoutManager layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    if (showingLogos) {
      constraints.gridwidth = 2;
      final JLabel supremicaLogo = new JLabel(LOGO_SUPREMICA);
      final Border verticalSpace =
        BorderFactory.createEmptyBorder(0, 0, VERTICAL_SPACE, 0);
      supremicaLogo.setBorder(verticalSpace);
      add(supremicaLogo, constraints);
      constraints.gridwidth = 1;
      constraints.gridy = 1;
      final JLabel watersLogo = new JLabel(LOGO_WATERS);
      add(watersLogo, constraints);
    }
    add(mAboutPanel, constraints);
  }


  //#########################################################################
  //# Inner Class AboutBoxBorder
  /**
   * A thick raised bevel border in shades of blue-grey, used to display the
   * about box with a cute 3d-effect.
   */
  private static class AboutBoxBorder extends AbstractBorder
  {
    //#######################################################################
    //# Overrides for javax.swing.border.AbstractBorder
    @Override
    public void paintBorder(final Component comp, final Graphics graphics,
                            final int x, final int y, final int width,
                            final int height)
    {
      super.paintBorder(comp, graphics, x, y, width, height);
      final int[] xPoints = {x, x + width,
                             x + width - BORDER_WIDTH, x + BORDER_WIDTH,
                             x + BORDER_WIDTH, x};
      final int[] yPoints = {y, y,
                             y + BORDER_WIDTH, y + BORDER_WIDTH,
                             y + height - BORDER_WIDTH, y + height};
      graphics.setColor(BORDER_BRIGHT_COLOR);
      graphics.fillPolygon(xPoints, yPoints, xPoints.length);
      xPoints[0] = xPoints[1];
      xPoints[3] = xPoints[2];
      yPoints[0] = yPoints[5];
      yPoints[3] = yPoints[4];
      graphics.setColor(BORDER_DARK_COLOR);
      graphics.fillPolygon(xPoints, yPoints, xPoints.length);
      graphics.drawLine(x, y, x + width, y);
      graphics.drawLine(x, y, x, y + height);
      graphics.drawLine(x, y, x + BORDER_WIDTH, y + BORDER_WIDTH);
      graphics.drawLine(x + BORDER_WIDTH, y + BORDER_WIDTH,
                        x + width - BORDER_WIDTH, y + BORDER_WIDTH);
      graphics.drawLine(x + BORDER_WIDTH, y + BORDER_WIDTH,
                        x + BORDER_WIDTH, y + height - BORDER_WIDTH);
      graphics.setColor(BORDER_BRIGHT_COLOR);
      graphics.drawLine(x + width, y + height,
                        x + width - BORDER_WIDTH, y + height - BORDER_WIDTH);
    }

    @Override
    public Insets getBorderInsets(final Component c, final Insets insets)
    {
      insets.left = BORDER_WIDTH + 4;
      insets.top = BORDER_WIDTH + 2;
      insets.right = insets.bottom = BORDER_WIDTH;
      return insets;
    }

    @Override
    public boolean isBorderOpaque()
    {
      return true;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -5784899618541645886L;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private AboutPanel mAboutPanel;


  //#########################################################################
  //# Class Constants
  private static final int VERTICAL_SPACE = 24;
  private static final int TEXT_WIDTH = 512;
  private static final int BORDER_WIDTH = 6;

  private static final ImageIcon LOGO_SUPREMICA =
    IconAndFontLoader.loadImage("greeter", "supremica");
  private static final ImageIcon LOGO_WATERS =
    IconAndFontLoader.loadImage("greeter", "waters");
  private static final ImageIcon BACKGROUND =
    IconAndFontLoader.loadImage("greeter", "waves");

  private static final Color BORDER_BRIGHT_COLOR = new Color(232, 232, 255);
  private static final Color BORDER_DARK_COLOR = new Color(120, 120, 136);

  private static final long serialVersionUID = -4208529601505410762L;

}
