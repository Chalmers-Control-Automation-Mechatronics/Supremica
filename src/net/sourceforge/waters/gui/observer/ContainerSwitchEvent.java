//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   ContainerSwitchEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.observer;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.DocumentContainerManager;


/**
 * A notification sent by the document container manager to indicate a
 * switch of document containers.  This event is generated when a new file
 * is opened or activated.
 *
 * @see DocumentContainerManager
 * @author Robi Malik
 */

public class ContainerSwitchEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a container switch event.
   * @param  newcontainer  The document container active after
   *                       the change, or <CODE>null</CODE>.
   */
  public ContainerSwitchEvent(final DocumentContainerManager source,
                              final DocumentContainer newcontainer)
  {
    super(source);
    mNewContainer = newcontainer;
  }

	
  //#########################################################################
  //# Simple Access
  public DocumentContainerManager getSource()
  {
    return (DocumentContainerManager) super.getSource();
  }

  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.CONTAINER_SWITCH;
  }

  /**
   * Gets the new document container.
   * @return The document container active after the change,
   *         or <CODE>null</CODE>.
   */
  public DocumentContainer getNewContainer()
  {
    return mNewContainer;
  }


  //#########################################################################
  //# Data Members
  private final DocumentContainer mNewContainer;

}
