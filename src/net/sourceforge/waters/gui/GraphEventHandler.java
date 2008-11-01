//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEventHandler
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;


import net.sourceforge.waters.subject.module.IdentifierSubject;


/**
 * A callback interface to access editing functionalities in the {@link
 * GraphEventPanel}. This is used by the {@link EventTableModel} to relay
 * editing events from editor cells to the panel, which can handle the
 * update of the model as well as the selection.
 *
 * @author Robi Malik
 */

interface GraphEventHandler
{

  public void addEvent(IdentifierSubject neo);

  public void removeEvent(IdentifierSubject victim);

  public void replaceEvent(IdentifierSubject old, IdentifierSubject neo);

}
