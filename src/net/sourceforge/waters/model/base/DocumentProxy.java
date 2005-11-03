//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Proxy
//###########################################################################
//# $Id: DocumentProxy.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.File;


/**
 * <P>The interface characterising top-level objects in Waters.</P>
 *
 * <P>A <I>document</I> is an object that has its own file representation.
 * Every loaded document should have an associated file name
 * identifying the location from where it has been read, and where it
 * can be written to.</P>
 *
 * @author Robi Malik
 */

public interface DocumentProxy
  extends NamedProxy
{

  //#########################################################################
  //# Cloning
  public DocumentProxy clone();


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the file name associated with this document.
   */
  public File getLocation();
  /**
   * Sets the file name associated with this document to a new value.
   */
  public void setLocation(File location);

}
