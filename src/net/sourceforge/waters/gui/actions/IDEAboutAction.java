//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEAboutAction
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.about.AboutPanel;
import net.sourceforge.waters.gui.about.AboutPopup;
import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.IDE;


/**
 * The action associated with the About menu button.
 * This action shows the version information in a popup window that
 * contains an {@link AboutPanel}.
 *
 * @author Robi Malik
 */

public class IDEAboutAction extends IDEAction
{

  //#########################################################################
  //# Constructors
  public IDEAboutAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "About ...");
    putValue(Action.SHORT_DESCRIPTION, "Display version information");
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_ABOUT);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    final IDE ide = getIDE();
    final AboutPopup popup = new AboutPopup(ide);
    popup.setVisible(true);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2970500283126405824L;

}
