//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProxyMarshaller
//###########################################################################
//# $Id: ProxyUnmarshaller.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.sourceforge.waters.model.base.DocumentProxy;

/**
 * <P>A helper class to write Waters documents to files.</P>
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
   * @param  filename The name of the file to be loaded.
   * @return The loaded document.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                  has failed for some reason.
   * @throws IOException to indicate that the input file could not be
   *                  opened or read.
   */
  public D unmarshal(File filename)
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

}
