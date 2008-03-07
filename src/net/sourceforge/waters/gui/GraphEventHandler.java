//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEventHandler
//###########################################################################
//# $Id: GraphEventHandler.java,v 1.1 2008-03-07 04:11:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;


import net.sourceforge.waters.subject.module.IdentifierSubject;


interface GraphEventHandler
{

  public void addEvent(IdentifierSubject neo);

  public void removeEvent(IdentifierSubject victim);

  public void replaceEvent(IdentifierSubject old, IdentifierSubject neo);

}
