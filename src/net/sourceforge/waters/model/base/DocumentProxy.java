//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   Proxy
//###########################################################################
//# $Id: DocumentProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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

public interface DocumentProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Get the file name associated with this document.
   */
  public File getLocation();
  /**
   * Set the file name associated with this document to a new value.
   */
  public void setLocation(File location);

}
