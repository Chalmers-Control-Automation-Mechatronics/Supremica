//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersGraphAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * The class of all actions that can be applied to a graph in the editor
 * panel. This is a more convenient subclass of {@link WatersAction} for
 * actions that expect the presence of an editor panel, a module, and an
 * open graph. In addition to {@link WatersAction}, it provides access to
 * the graph panel, and an enablement condition that requires the presence
 * of a graph.
 *
 * @author Robi Malik
 */

public abstract class WatersGraphAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  protected WatersGraphAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This default implementation
   * only updates the status if the user has switched panels, enabling
   * the action if a graph editor is active.
   */
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      final EditorWindowInterface gui = getActiveEditorWindowInterface();
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
   * Retrieves a references to the active graph editor panel.
   * @return  An editor window interface to access the active graph editor
   *          panel, or <CODE>null</CODE> if no graph editor panel is\
   *          currently active.
   */
  EditorWindowInterface getActiveEditorWindowInterface()
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
    return mcontainer.getActiveEditorWindowInterface();
  }

  /**
   * Retrieves the controlled surface of the active graph editor panel.
   * @return  The currently active controlled surface, or <CODE>null</CODE>
   *          if no graph is being edited.
   */
  GraphEditorPanel getActiveControlledSurface()
  {
    final EditorWindowInterface gui = getActiveEditorWindowInterface();
    return gui == null ? null : gui.getControlledSurface();
  }

  /**
   * Retrieves the event panel of the active graph editor panel.
   * @return  The currently active event panel, or <CODE>null</CODE>
   *          if no graph is being edited.
   */
  GraphEventPanel getActiveGraphEventPanel()
  {
    final EditorWindowInterface gui = getActiveEditorWindowInterface();
    return gui == null ? null : gui.getEventPanel();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
