//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersActionManager
//###########################################################################
//# $Id: WatersActionManager.java,v 1.1 2007-06-21 15:57:55 robi Exp $
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
    mActionMap = new HashMap<Class<? extends WatersAction>, WatersAction>();
    addAction(new WatersUndoAction(ide));
    addAction(new WatersRedoAction(ide));
    ide.attach(this);
  }


  //#########################################################################
  //# Access to Actions
  public WatersAction getAction(final Class<? extends WatersAction> clazz)
  {
    return mActionMap.get(clazz);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    for (final WatersAction action : mActionMap.values()) {
      action.update(event);
    }
  }


  //#########################################################################
  //# Initialization
  private void addAction(final WatersAction action)
  {
    final Class<? extends WatersAction> clazz = action.getClass();
    mActionMap.put(clazz, action);
  }


  //#######################################################################
  //# Data Members
  private final Map<Class<? extends WatersAction>, WatersAction> mActionMap;

}
