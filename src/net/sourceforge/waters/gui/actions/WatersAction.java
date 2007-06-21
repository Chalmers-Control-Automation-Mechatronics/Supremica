//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersAction
//###########################################################################
//# $Id: WatersAction.java,v 1.1 2007-06-21 15:57:55 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import javax.swing.AbstractAction;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public abstract class WatersAction
  extends AbstractAction
  implements Observer
{

  //#########################################################################
  //# Constructors
  public WatersAction(final IDE ide)
  {
    mIDE = ide;
    setEnabled(false);
  }


  //#########################################################################
  //# Accessing the IDE
  public IDE getIDE()
  {
    return mIDE;
  }

  public ModuleWindowInterface getActiveModuleWindowInterface()
  {
    if (!mIDE.editorActive()) {
      return null;
    }
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    return mcontainer.getEditorPanel();    
  }

  public UndoInterface getActiveUndoInterface()
  {
    if (!mIDE.editorActive()) {
      return null;
    }
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    return (ModuleContainer) container;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case MAINPANEL_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Enabling and Disabling
  public boolean updateEnabledStatus()
  {
    final ModuleWindowInterface gui = getActiveModuleWindowInterface();
    final boolean enabled = gui != null;
    setEnabled(enabled);
    return enabled;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;

}
