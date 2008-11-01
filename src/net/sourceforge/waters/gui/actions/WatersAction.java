//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * The class of all actions that can be applied to a module in the editor
 * panel. This is a more convenient subclass of {@link IDEAction} for
 * actions that expect the presence of an editor panel and a module. It
 * provides some support for access to the editor panel. It also provides a
 * more specific enablement condition, implementing an action that is
 * enabled only if the Waters module editor is active.
 *
 * @author Robi Malik
 */

public abstract class WatersAction
  extends IDEAction
{

  //#########################################################################
  //# Constructors
  protected WatersAction(final IDE ide)
  {
    super(ide);
    setEnabled(false);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This default implementation
   * only updates the status if the user has switched main panels, enabling
   * the action if the editor is active.
   */
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
      final ModuleWindowInterface gui = getActiveModuleWindowInterface();
      final boolean enabled = gui != null;
      setEnabled(enabled);
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Accessing the IDE
  /**
   * Retrieves a references to the active editor panel.
   * @return  A module window interface to access the active editor panel,
   *          or <CODE>null</CODE> if no editor panel is currently active.
   */
  ModuleWindowInterface getActiveModuleWindowInterface()
  {
    final IDE ide = getIDE();
    if (!ide.editorActive()) {
      return null;
    }
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    return mcontainer.getEditorPanel();    
  }

  /**
   * Retrieves a references to the current module context.
   * @return  A module context object providing access to the module
   *          being edited, or <CODE>null</CODE> if no module is currently
   *          available.
   */
  ModuleContext getModuleContext()
  {
    final ModuleWindowInterface iface = getActiveModuleWindowInterface();
    if (iface == null) {
      return null;
    } else {
      return iface.getModuleContext();
    }
  }

  /**
   * Retrieves a references to undo interface for the currently edited module.
   * @return  An undo interface that can be used to send commands to the
   *          current module, or <CODE>null</CODE> if no editor panel is
   *          currently active.
   */
  UndoInterface getActiveUndoInterface()
  {
    final IDE ide = getIDE();
    if (!ide.editorActive()) {
      return null;
    }
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof UndoInterface)) {
      return null;
    }
    return (UndoInterface) container;
  }

}
