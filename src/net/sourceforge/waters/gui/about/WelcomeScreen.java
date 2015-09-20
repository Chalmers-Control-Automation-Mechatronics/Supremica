//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.about
//# CLASS:   WelcomeScreen
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.util.IconLoader;

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
    Config.DOT_EXECUTE_COMMAND.addPropertyChangeListener(this);
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
    mAboutPanel.update();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createContents()
  {
    // Create the about box with the version information
    final AboutPanel mAboutPanel = new AboutPanel(mIDE);
    final Border border1 = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
    final Border border2 = BorderFactory.createRaisedBevelBorder();
    Border border = BorderFactory.createCompoundBorder(border1, border2);
    final Border border3 = BorderFactory.createRaisedBevelBorder();
    border = BorderFactory.createCompoundBorder(border, border3);
    final Border border4 = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
    border = BorderFactory.createCompoundBorder(border, border4);
    final Border border5 = BorderFactory.createEmptyBorder(2, 4, 2, 1);
    border = BorderFactory.createCompoundBorder(border, border5);
    mAboutPanel.setBorder(border);
    mAboutPanel.setSize(TEXT_WIDTH, Integer.MAX_VALUE);
    final int textHeight = mAboutPanel.getPreferredSize().height;
    final Dimension size = new Dimension(TEXT_WIDTH, textHeight);
    mAboutPanel.setPreferredSize(size);

    // Is there enough space for the images also
    final int fullHeight =
      VERTICAL_SPACE + LOGO_SUPREMICA.getIconHeight() +
      Math.max(LOGO_SUPREMICA.getIconHeight(), textHeight);
    final int fullWidth = LOGO_WATERS.getIconWidth() + TEXT_WIDTH;
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
  //# Data Members
  private final IDE mIDE;
  private AboutPanel mAboutPanel;


  //#########################################################################
  //# Class Constants
  private static final int VERTICAL_SPACE = 24;
  private static final int TEXT_WIDTH = 512;

  private static final ImageIcon LOGO_SUPREMICA =
    IconLoader.loadImage("greeter", "supremica");
  private static final ImageIcon LOGO_WATERS =
    IconLoader.loadImage("greeter", "waters");

  private static final long serialVersionUID = -4208529601505410762L;

}
