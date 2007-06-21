//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersActionManager
//###########################################################################
//# $Id: WatersActionManager.java,v 1.2 2007-06-21 20:56:53 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.IDE;


public abstract class WatersActionManager
  implements Observer
{

  //#########################################################################
  //# Constructor
  public WatersActionManager(final IDE ide)
  {
    mActionMap = new HashMap<Class<? extends IDEAction>, IDEAction>();
    addAction(new WatersUndoAction(ide));
    addAction(new WatersRedoAction(ide));
    ide.attach(this);
  }


  //#########################################################################
  //# Access to Actions
  public IDEAction getAction(final Class<? extends IDEAction> clazz)
  {
    return mActionMap.get(clazz);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    for (final IDEAction action : mActionMap.values()) {
      action.update(event);
    }
  }


  //#########################################################################
  //# Initialization
  protected void addAction(final IDEAction action)
  {
    final Class<? extends IDEAction> clazz = action.getClass();
    mActionMap.put(clazz, action);
  }


  //#######################################################################
  //# Data Members
  private final Map<Class<? extends IDEAction>, IDEAction> mActionMap;

}
