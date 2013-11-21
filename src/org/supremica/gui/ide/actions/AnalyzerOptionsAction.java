//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerOptionsAction
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.PropertiesDialog;


/**
 * The action that displays the options dialog.
 */
public class AnalyzerOptionsAction extends IDEAction
{

  //#########################################################################
  //# Constructors
  public AnalyzerOptionsAction(final List<IDEAction> actionList)
  {
    super(actionList);
    putValue(Action.NAME, "Options...");
    putValue(Action.SHORT_DESCRIPTION, "Options");
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_OPTIONS);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    doAction();
  }


  //#########################################################################
  //# Interface org.supremica.gui.ide.actions.IDEAction
  @Override
  public void doAction()
  {
    final PropertiesDialog dialog = new PropertiesDialog(ide.getFrame());
    dialog.setVisible(true);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
