//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersActionManager
//###########################################################################
//# $Id: WatersActionManager.java,v 1.8 2008-02-14 06:46:26 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

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
    mActionMap = new HashMap<Class<? extends IDEAction>, IDEAction>(32);
    addAction(new GraphLayoutAction(ide));
    addAction(new GraphSaveEPSAction(ide));
    addAction(new IDECopyAction(ide));
    addAction(new IDECutAction(ide));
    addAction(new IDEDeleteAction(ide));
    addAction(new IDEDeselectAllAction(ide));
    addAction(new IDEPasteAction(ide));
    addAction(new IDEPropertiesAction(ide));
    addAction(new IDESelectAllAction(ide));
    addAction(new InsertEventDeclAction(ide));
    addAction(new InsertForeachComponentAction(ide));
    addAction(new InsertSimpleComponentAction(ide));
    addAction(new InsertVariableAction(ide));
    addAction(new ShowGraphAction(ide));
    addAction(new ShowModuleCommentAction(ide));
    addAction(new ToolEdgeAction(ide));
    addAction(new ToolGroupNodeAction(ide));
    addAction(new ToolNodeAction(ide));
    addAction(new ToolSelectAction(ide));
    addAction(new WatersRedoAction(ide));
    addAction(new WatersUndoAction(ide));
    ide.attach(this);
  }


  //#########################################################################
  //# Access to Actions
  public IDEAction getAction(final Class<? extends IDEAction> clazz)
  {
    return mActionMap.get(clazz);
  }


  //#########################################################################
  //# Installing Keyboard Shortcuts
  public void installCutCopyPasteActions(final JComponent comp)
  {
    installAction(comp, IDECutAction.class);
    installAction(comp, IDECopyAction.class);
    installAction(comp, IDEPasteAction.class);
  }

  public void installAction(final JComponent comp,
                            final Class<? extends IDEAction> clazz)
  {
    final IDEAction action = getAction(clazz);
    final InputMap imap = comp.getInputMap();
    final ActionMap amap = comp.getActionMap();
    final String name = (String) action.getValue(Action.NAME);
    final KeyStroke key = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
    imap.put(key, name);
    amap.put(name, action);
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
