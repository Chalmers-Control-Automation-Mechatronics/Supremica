//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProxyMarshaller
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;

/**
 * <P>A helper class to read Waters documents from files.</P>
 *
 * <P>The ProxyUnmarshaller is a simple wrapper that provides an easy way
 * to load a document from a file.</P> 
 *
 * @author Robi Malik
 */

public interface ProxyUnmarshaller<D extends DocumentProxy>
{

  //#########################################################################
  //# Access Methods
  /**
   * Loads a document from a file.
   * @param  uri      A URI specifiying the location of the document
   *                  to be retrieved.
   * @return The loaded document.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                  has failed for some reason.
   * @throws IOException to indicate that the input file could not be
   *                  opened or read.
   */
  public D unmarshal(URI uri)
    throws WatersUnmarshalException, IOException;


  //#########################################################################
  //# Type Information
  /**
   * Gets the class of documents handled by this unmarshaller.
   */
  public Class<D> getDocumentClass();

  /**
   * Gets a default extension for the files read by this unmarshaller.
   */
  public String getDefaultExtension();

  /**
   * Gets a list of file name extensions for files that may be handled
   * by this marshaller.
   */
  public Collection<String> getSupportedExtensions();
  
  /**
   * Gets a list of file filters that may be handled
   * by this marshaller.
   */
  public Collection<FileFilter> getSupportedFileFilters();


  //#########################################################################
  //# Entity Resolving
  /**
   * Gets the document manager used by this unmarshaller to resolve
   * references to other files.
   */
  public DocumentManager getDocumentManager();

  /**
   * Sets a document manager to used by this unmarshaller to resolve
   * references to other files.
   */
  public void setDocumentManager(DocumentManager manager);

}
