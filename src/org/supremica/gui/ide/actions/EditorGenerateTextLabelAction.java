//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
// import javax.swing.ImageIcon;
// import javax.swing.JFileChooser;
// import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.external.tumses.TextLabelGenerator;
import org.supremica.gui.ide.IDE;
// import org.supremica.properties.Config;

public class EditorGenerateTextLabelAction extends IDEAction
{
  //#########################################################################
  //# Constructor
  public EditorGenerateTextLabelAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(true);

    final String actName = "Recompute Guards and Actions Text labels";
    final String description = "Recompute the XML Text labels of Guards and Actions";

    putValue(Action.NAME, actName);
    putValue(Action.SHORT_DESCRIPTION, description);
  }

  //#########################################################################
  //# Overridden methods
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    logger.info("Recomputing Guards (and Actions) Text labels...");
    TextLabelGenerator.GenerateTextLabel(ide);
  }

  //#########################################################################
  //# Class Constants
  private final Logger logger = LogManager.getLogger(IDE.class);
  private final static long serialVersionUID = 1L;
 }