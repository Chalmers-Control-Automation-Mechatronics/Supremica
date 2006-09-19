//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   DocumentProxy
//###########################################################################
//# $Id: DocumentProxy.java,v 1.4 2006-09-19 15:53:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;


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
   * Gets the comment associated with this document.
   * The comment is a human-readable description of the document's contents
   * that may extend over several lines.
   */
  public String getComment();
 
  /**
   * Gets the URL associated with this document.
   */
  public URI getLocation();

  /**
   * Gets the file name associated with this document.
   * @return The file name used for reading and writing this document.
   * @throws MalformedURLException to indicate that this document is not
   *                            associated with a writable file.
   */
  public File getFileLocation() throws MalformedURLException;

  /**
   * Sets the file name associated with this document to a new value.
   */
  public void setLocation(URI location);

}
