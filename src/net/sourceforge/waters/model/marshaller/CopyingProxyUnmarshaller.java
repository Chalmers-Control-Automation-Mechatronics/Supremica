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
import java.net.URI;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>
 * An unmarshalling interface to support importing of multiple documents from a
 * single external source.
 * </P>
 * 
 * <P>
 * A copying unmarshaller reads a document, typically from an external file
 * format, and while doing so, it may create several documents that are written
 * to a specified output directory.
 * </P>
 * 
 * @author Robi Malik
 */

public interface CopyingProxyUnmarshaller<D extends DocumentProxy> extends
    ProxyUnmarshaller<D>
{

  // #########################################################################
  // # Access Methods
  /**
   * Gets the output directory of this copying unmarshaller.
   */
  public File getOutputDirectory();

  /**
   * Sets the output directory for this copying unmarshaller. The output
   * directory is the location where all files created while unmarshalling are
   * saved.
   */
  public void setOutputDirectory(final File outputdir);

  /**
   * Loads a document from a file.
   * 
   * @param uri
   *          A URI specifying the location of the document to be retrieved.
   * @return The loaded document.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws WatersMarshalException
   *           to indicate that some output file could not be written.
   * @throws IOException
   *           to indicate that the input file could not be opened or read or
   *           written.
   */
  public D unmarshalCopying(URI uri) throws IOException,
      WatersMarshalException, WatersUnmarshalException;

}
