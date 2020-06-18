//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
