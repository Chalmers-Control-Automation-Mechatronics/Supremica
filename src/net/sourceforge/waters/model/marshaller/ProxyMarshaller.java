//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProxyMarshaller
//###########################################################################
//# $Id: ProxyMarshaller.java,v 1.3 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.IOException;

import net.sourceforge.waters.model.base.DocumentProxy;

/**
 * <P>A helper class to write Waters documents to files.</P>
 *
 * <P>The ProxyMarshaller is a simple wrapper that provides an easy way
 * to save a document in some file format.</P>
 *
 * @author Robi Malik
 */

public interface ProxyMarshaller<D extends DocumentProxy>
{

  //#########################################################################
  //# Access Methods
  /**
   * Writes a document to a file.
   * @param  filename The name of the file to be written.
   * @param  docproxy The document to be written.
   * @throws WatersMarshalException to indicate a failure while writing the
   *                  data structures.
   * @throws IOException to indicate that the output file could not be
   *                  opened or written.
   */
  public void marshal(final D docproxy, final File filename)
    throws WatersMarshalException, IOException;


  //#########################################################################
  //# Type Information
  /**
   * Gets the class of documents handled by this marshaller.
   */
  public Class<D> getDocumentClass();

  /**
   * Gets a default extension for the files written by this marshaller.
   */
  public String getDefaultExtension();

}
