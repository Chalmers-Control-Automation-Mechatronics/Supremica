//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProxyMarshaller
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>
 * The basic marshaller interface.
 * </P>
 * 
 * <P>
 * The ProxyMarshaller provides the basic means for the {@link DocumentManager}
 * to marshal, i.e., save documents in various file formats.
 * </P>
 * 
 * @author Robi Malik
 */

public interface ProxyMarshaller<D extends DocumentProxy>
{

  // #########################################################################
  // # Access Methods
  /**
   * Writes a document to a file.
   * 
   * @param filename
   *          The name of the file to be written.
   * @param docproxy
   *          The document to be written.
   * @throws WatersMarshalException
   *           to indicate a failure while writing the data structures.
   * @throws IOException
   *           to indicate that the output file could not be opened or written.
   */
  public void marshal(final D docproxy, final File filename)
      throws WatersMarshalException, IOException;

  // #########################################################################
  // # Type Information
  /**
   * Gets the class of documents handled by this marshaller.
   */
  public Class<D> getDocumentClass();

  /**
   * Gets a default extension for the files written by this marshaller.
   */
  public String getDefaultExtension();

  /**
   * Gets a default file filter that accepts the files with default extension.
   * Only one file filter is created, and this method ensures that the same
   * object is returned by each call.
   */
  public FileFilter getDefaultFileFilter();

}
