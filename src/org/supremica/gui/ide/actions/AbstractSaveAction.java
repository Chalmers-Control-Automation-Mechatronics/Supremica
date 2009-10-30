//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AbstractSaveAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;


/**
 * A common base class for saving actions.
 * This action class unifies the 'save' and 'save-as' actions for IDE
 * documents, ensuring that they share the same file chooser and also code.
 *
 * The action handles both Waters modules and Supremica projects,
 * and the conversion between these two formats when the user chooses a
 * different file filter.
 *
 * @author Robi Malik, Hugo Flordal
 */

public abstract class AbstractSaveAction
  extends net.sourceforge.waters.gui.actions.IDEAction
{

  //#########################################################################
  //# Constructor
  AbstractSaveAction(final IDE ide)
  {
    super(ide);
    setEnabled(false);
  }
    
    
  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Enabling and Disabling
  private void updateEnabledStatus()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    final boolean enabled = container != null;
    setEnabled(enabled);
    if (enabled) {
      final String type = container.getTypeString().toLowerCase();
      final String text = getShortDescription(type);
      putValue(Action.SHORT_DESCRIPTION, text);
    }
  }

  abstract String getShortDescription(final String type);

  
  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
